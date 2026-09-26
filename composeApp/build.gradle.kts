import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.roborazzi)
}

composeCompiler {
    // Stability reports için
    if (project.findProperty("compose.compiler.metrics") == "true") {
        metricsDestination = layout.buildDirectory.dir("compose-metrics")
        reportsDestination = layout.buildDirectory.dir("compose-reports")
    }
}

kotlin {
    compilerOptions {
        // 21, not 17: kmapper-core 2.2.2's published classes target JVM 21 bytecode - see
        // KmpLibraryConventionPlugin's comment. composeApp isn't built by that convention plugin
        // (it's the Android application module), so the same bump is repeated here.
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

android {
    namespace = "com.domatapp.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.domatapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // Screenshot tests: Robolectric renders every @Preview in the app's classpath (all feature and
    // core modules) and Roborazzi records it as a PNG. The only unit tests in the repo.
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
                // Robolectric downloads its android-all jars at runtime; honour a mirror if given.
                (project.findProperty("robolectric.dependency.repo.url") as String?)?.let { url ->
                    it.systemProperty("robolectric.dependency.repo.url", url)
                }
                it.maxHeapSize = "2g"
                // Robolectric (SDK 36 android-all) reaches into JDK internals on JDK 21.
                it.jvmArgs(
                    "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
                    "--add-opens=java.base/java.io=ALL-UNNAMED",
                    "--add-opens=java.base/java.lang=ALL-UNNAMED",
                )
            }
        }
    }
}

// Generates one Robolectric test per @Preview (private ones included) under com.domatapp.
//   record:  ./gradlew :composeApp:recordRoborazziDebug   -> composeApp/build/outputs/roborazzi/*.png
//   verify:  ./gradlew :composeApp:verifyRoborazziDebug   (compares against recorded images)
// The images of a screen are compared with design/screens/<ID>/states/*.png by
// ai/design/scripts/compare_design_package.py.
roborazzi {
    generateComposePreviewRobolectricTests {
        enable = true
        packages = listOf("com.domatapp")
        includePrivatePreviews = true
        robolectricConfig = mapOf(
            "sdk" to "[36]",
            // Plain Application: DomatApplication starts Koin, and previews must not need DI.
            "application" to "android.app.Application::class",
            "qualifiers" to "\"w390dp-h844dp-xhdpi\"",
        )
    }
}

dependencies {
    implementation(projects.shared)

    // Core modules
    implementation(projects.core.design)
    implementation(projects.core.navigation)
    implementation(projects.core.presentation)
    implementation(projects.core.remote)
    implementation(projects.core.config)

    // Feature modules
    implementation(projects.feature.auth.domain)
    implementation(projects.feature.auth.data)
    implementation(projects.feature.auth.presentation)
    implementation(projects.feature.onboarding.presentation)
    implementation(projects.feature.home.presentation)
    implementation(projects.feature.checkout.presentation)

    // UI & Compose
    implementation(libs.ui.compose.runtime)
    implementation(libs.ui.compose.foundation)
    implementation(libs.ui.compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.ui.compose.ui)

    implementation(libs.ui.compose.uiToolingPreview)
    implementation(libs.ui.compose.componentsResources)
    implementation(libs.ui.activity.compose)
    implementation(libs.core.lifecycle.viewmodelCompose)
    implementation(libs.core.lifecycle.runtimeCompose)

    // Koin for Compose
    api(libs.di.koin.core)
    api(libs.di.koin.android)
    api(libs.di.koin.compose)
    implementation(libs.core.androidx.ktx)

    // Ktor (needed for HttpClient configuration)
    implementation(libs.network.ktorClient.core)
    implementation(libs.network.ktorClient.okhttp)
    implementation(libs.network.ktorClient.contentNegotiation)
    implementation(libs.network.ktorClient.logging)
    implementation(libs.network.ktorClient.kxSerializationJson)

    // Credential Manager (Google Sign-In)
    implementation(libs.auth.credentials.core)
    implementation(libs.auth.credentials.playServices)
    implementation(libs.auth.googleId.core)

    // Kotlinx Serialization
    implementation(libs.serialization.kxSerialization.json)

    // Navigation: Gezgin (rememberNavigator / GezginDisplay) + the generated topology in
    // :core:navigation. Gezgin's own api dependency is what brings androidx.navigation3 in, so
    // this module declares no Navigation 3 coordinate of its own.
    implementation(libs.navigation.gezgin.core)

    api(libs.ui.compose.uiTooling)

    // Screenshot tests (see roborazzi { } above)
    testImplementation(libs.test.junit4)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.roborazzi)
    testImplementation(libs.test.roborazzi.compose)
    testImplementation(libs.test.roborazzi.previewScannerSupport)
    testImplementation(libs.test.composablePreviewScanner)
    testImplementation(libs.test.compose.uiTestJunit4)
}
