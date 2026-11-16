plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// The sing-box core (libbox.aar) is optional at build time: without it the app builds and runs (UI, subscriptions, servers)
// but "Connect" reports that the core is missing. Put libbox.aar into app/libs/ to get the real VPN (see README).
val hasCore = file("libs/libbox.aar").exists()

android {
    namespace = "app.amnezia.client"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.amnezia.client"
        minSdk = 26
        targetSdk = 35
        // CI passes the git tag / run number: -PversionName=1.2.3 -PversionCode=42
        versionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
        versionName = (project.findProperty("versionName") as String?) ?: "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")   // personal builds: installable without a keystore. Use your own keystore for distribution.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    packaging { jniLibs { useLegacyPackaging = true } }
    sourceSets {
        getByName("main") { java.srcDir(if (hasCore) "src/withCore/kotlin" else "src/noCore/kotlin") }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.yaml:snakeyaml:2.2:android")   // Clash YAML import (the android classifier avoids java.beans)
    if (hasCore) implementation(files("libs/libbox.aar"))

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")   // android.jar's org.json is a stub in JVM unit tests
}
