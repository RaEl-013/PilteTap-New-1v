import az.plite.tap.R
package az.plite.tap // Bu sətr mütləq olmalıdır vəAndroidManifest.xml-dəki namespace ilə eyni olmalıdır

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import az.plite.tap.R // Bəzən R sinfini əllə import etmək tələb olunur

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true

        webView.loadUrl("file:///android_asset/index.html")
    }
}
