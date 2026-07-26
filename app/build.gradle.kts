import java.io.File
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val mapsApiKey: String = (localProperties.getProperty("MAPS_API_KEY") ?: "").ifBlank {
    "MISSING_MAPS_API_KEY"
}

// manifestPlaceholders potrafi się nie podstawić w niektórych środowiskach (obserwowane:
// zdekompilowany manifest w APK dalej miał dosłowne "${MAPS_API_KEY}"). Zamiast tego
// generujemy zasób @string/google_maps_key - dokładnie tak jak robi to oficjalny szablon
// "Google Maps Activity" w Android Studio - i manifest odwołuje się do niego bezpośrednio.
val generateMapsApiKeyRes = tasks.register("generateMapsApiKeyRes") {
    val outputDir = layout.buildDirectory.dir("generated/mapsApiKeyRes/values")
    inputs.property("mapsApiKey", mapsApiKey)
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        File(dir, "google_maps_api.xml").writeText(
            """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_maps_key" translatable="false">$mapsApiKey</string>
</resources>
""",
        )
    }
}

android {
    namespace = "com.rodzina.wyjazdy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rodzina.wyjazdy"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets {
        getByName("main") {
            res.srcDir(layout.buildDirectory.dir("generated/mapsApiKeyRes"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        // Duża część powierzchni Material3 (TopAppBar, DatePicker, ExposedDropdownMenuBox...)
        // jest wciąż oznaczona jako eksperymentalna - opt-in globalnie, żeby nie oznaczać
        // @OptIn w kilkunastu plikach z każdym ekranem.
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.navigation.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    implementation(libs.play.services.auth)
    implementation(libs.play.services.maps)
    implementation(libs.coroutines.play.services)

    implementation(libs.maps.compose)
    implementation(libs.calendar.compose)
    implementation(libs.work.runtime.ktx)
}

tasks.named("preBuild") {
    dependsOn(generateMapsApiKeyRes)
}
