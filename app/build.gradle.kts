import java.util.Properties
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Baca URL API dari local.properties (file ini tidak ter-push ke Git)
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}
val apiRelease = localProps.getProperty("API_BASE_URL_RELEASE", "https://your-domain.com/api/")
val apiDebug = localProps.getProperty("API_BASE_URL_DEBUG", "http://your-local-ip:8000/api/")
val isReleaseTask = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

if (isReleaseTask) {
    val normalizedReleaseUrl = apiRelease.trim()
    val invalidReleaseHost = listOf("localhost", "127.0.0.1", "10.0.2.2", "your-domain.com")
        .any { normalizedReleaseUrl.contains(it, ignoreCase = true) }
    if (!normalizedReleaseUrl.startsWith("https://", ignoreCase = true) || invalidReleaseHost) {
        throw GradleException(
            "API_BASE_URL_RELEASE harus memakai HTTPS production domain, bukan localhost/debug/staging placeholder."
        )
    }
}

android {
    namespace = "com.sipos.kebabsk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sipos.kebabsk"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            manifestPlaceholders["usesCleartextTraffic"] = false
            buildConfigField("String", "API_BASE_URL", "\"$apiRelease\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            manifestPlaceholders["usesCleartextTraffic"] = true
            buildConfigField("String", "API_BASE_URL", "\"$apiDebug\"")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "DebugProbesKt.bin"
            excludes += "kotlin-tooling-metadata.json"
            excludes += "kotlin/**"
            excludes += "**/*.kotlin_module"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation & Dependency Injection (Koin)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.androidx.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Performance
    implementation(libs.androidx.metrics.performance)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

base {
    archivesName.set("kebab-sk")
}
