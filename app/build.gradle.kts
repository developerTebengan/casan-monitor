import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

if (layout.projectDirectory.file("google-services.json").asFile.isFile) {
    pluginManager.apply("com.google.gms.google-services")
}

val configuredBackendUrl = providers.gradleProperty("casanBackendUrl").orElse("").get()
val debugBackendUrl = providers.gradleProperty("casanDebugBackendUrl")
    .orElse("http://127.0.0.1:8080/").get()

fun validateBackendUrl(value: String, allowLoopbackHttp: Boolean = false) {
    if (value.isEmpty() && !allowLoopbackHttp) return
    val url = URI(value)
    val allowedScheme = url.scheme == "https" ||
        (allowLoopbackHttp && url.scheme == "http" && url.host == "127.0.0.1")
    require(allowedScheme && url.host != null && url.userInfo == null &&
        url.query == null && url.fragment == null && value.endsWith("/") &&
        value.none { it == '"' || it == '\\' }) {
        "Backend URL must end in / and use HTTPS, or HTTP on 127.0.0.1 for debug."
    }
}

validateBackendUrl(configuredBackendUrl)
validateBackendUrl(debugBackendUrl, allowLoopbackHttp = true)

val releaseSigningValues = listOf(
    "CASAN_RELEASE_KEYSTORE",
    "CASAN_RELEASE_STORE_PASSWORD",
    "CASAN_RELEASE_KEY_ALIAS",
    "CASAN_RELEASE_KEY_PASSWORD",
).associateWith { providers.environmentVariable(it).orNull }
val configureReleaseSigning = releaseSigningValues.values.any { it != null }

android {
    namespace = "id.casan.monitor"
    compileSdk {
        version = release(37) { minorApiLevel = 1 }
    }

    defaultConfig {
        applicationId = "id.casan.monitor"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "0.1.0"
    }

    signingConfigs {
        if (configureReleaseSigning) {
            require(releaseSigningValues.values.all { !it.isNullOrBlank() }) {
                "Set all four CASAN_RELEASE signing environment variables."
            }
            require(configuredBackendUrl.isNotBlank()) { "Set the HTTPS CASAN release backend URL." }
            require(layout.projectDirectory.file("google-services.json").asFile.isFile) {
                "Install the approved Firebase configuration before signing a release."
            }
            create("internalRelease") {
                storeFile = file(releaseSigningValues.getValue("CASAN_RELEASE_KEYSTORE")!!)
                storePassword = releaseSigningValues.getValue("CASAN_RELEASE_STORE_PASSWORD")
                keyAlias = releaseSigningValues.getValue("CASAN_RELEASE_KEY_ALIAS")
                keyPassword = releaseSigningValues.getValue("CASAN_RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "BACKEND_URL", "\"$debugBackendUrl\"")
        }
        release {
            if (configureReleaseSigning) signingConfig = signingConfigs.getByName("internalRelease")
            buildConfigField("String", "BACKEND_URL", "\"$configuredBackendUrl\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material)
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.datastore.preferences)
    testImplementation(libs.junit)
}
