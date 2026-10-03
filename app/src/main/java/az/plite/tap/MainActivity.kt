package az.plite.tap

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.webkit.WebViewAssetLoader
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    /** JS-dən çağırılır: Plite.saveFile(base64, "ad.json", "application/json") */
    inner class PliteBridge {
        @JavascriptInterface
        fun saveFile(base64: String, name: String, mime: String) {
            runOnUiThread {
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    if (Build.VERSION.SDK_INT >= 29) {
                        val values = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, name)
                            put(MediaStore.Downloads.MIME_TYPE, mime)
                            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        uri?.let { contentResolver.openOutputStream(it)?.use { os -> os.write(bytes) } }
                    } else {
                        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        dir.mkdirs()
                        FileOutputStream(File(dir, name)).use { it.write(bytes) }
                    }
                    Toast.makeText(this@MainActivity, "Yadda saxlanıldı: Download/$name", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Xəta: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private lateinit var web: WebView
    private var callback: ValueCallback<Array<Uri>>? = null
    private var camUri: Uri? = null
    private var pendingPermissionRequest: PermissionRequest? = null

    private val cameraPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val req = pendingPermissionRequest
        pendingPermissionRequest = null
        if (req == null) return@registerForActivityResult
        if (granted) req.grant(req.resources) else req.deny()
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val cb = callback ?: return@registerForActivityResult
        callback = null
        var result: Array<Uri>? = null
        if (r.resultCode == RESULT_OK) {
            val d = r.data
            val clip = d?.clipData
            result = when {
                clip != null -> Array(clip.itemCount) { clip.getItemAt(it).uri }
                d?.data != null -> arrayOf(d.data!!)
                camUri != null -> arrayOf(camUri!!)
                else -> null
            }
        }
        cb.onReceiveValue(result)
    }

    private fun cameraIntent(): Intent? = try {
        val dir = File(cacheDir, "cam").apply { mkdirs() }
        val f = File.createTempFile("cam_", ".jpg", dir)
        camUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, camUri)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (e: Exception) { null }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true   // IndexedDB üçün
        web.addJavascriptInterface(PliteBridge(), "Plite")
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this)).build()
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                loader.shouldInterceptRequest(request.url)
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(w: WebView, cb: ValueCallback<Array<Uri>>, p: FileChooserParams): Boolean {
                callback?.onReceiveValue(null)
                callback = cb
                val intent = (if (p.isCaptureEnabled) cameraIntent() else null) ?: p.createIntent()
                return try { launcher.launch(intent); true } catch (e: Exception) {
                    callback = null; cb.onReceiveValue(null); false
                }
            }
            override fun onPermissionRequest(request: PermissionRequest) {
                val wantsCamera = request.resources.any { it == PermissionRequest.RESOURCE_VIDEO_CAPTURE }
                if (!wantsCamera) { request.deny(); return }
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    request.grant(request.resources)
                } else {
                    pendingPermissionRequest = request
                    cameraPermLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
