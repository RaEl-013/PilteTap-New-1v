plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    // ... android ayarları ...
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.webkit:webkit:1.9.0")
}
