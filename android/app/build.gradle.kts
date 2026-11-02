plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Optional local signing; no keystore or password is stored in this repository.
val localStore = providers.environmentVariable("MI_TURNO_KEYSTORE").orNull
android {
    namespace = "app.miturno"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.miturno"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    signingConfigs {
        if (localStore != null) {
            create("localSigning") {
                storeFile = rootProject.file(localStore)
                storePassword = providers.environmentVariable("MI_TURNO_STORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("MI_TURNO_KEY_ALIAS").orElse("miturno").get()
                keyPassword = providers.environmentVariable("MI_TURNO_KEY_PASSWORD").get()
            }
        }
    }
    buildTypes {
        getByName("debug") {
            if (localStore != null) signingConfig = signingConfigs.getByName("localSigning")
        }
        getByName("release") {
            if (localStore != null) signingConfig = signingConfigs.getByName("localSigning")
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21") }
