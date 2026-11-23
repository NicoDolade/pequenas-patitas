plugins {
    id("com.android.application") version "8.13.0"
    id("com.google.gms.google-services") version "4.6.0" apply false
}

if (file("google-services.json").isFile) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "ar.edu.upso.pequenaspatitas"
    compileSdk = 36

    defaultConfig {
        applicationId = "ar.edu.upso.pequenaspatitas"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity:1.11.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.4")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // Esta versión funciona con el lint de AGP 8.13.
    implementation("com.google.firebase:firebase-auth:24.0.1")
}
