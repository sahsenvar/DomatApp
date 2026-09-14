import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Bundle compose resources from KMP library modules that use com.android.kotlin.multiplatform.library.
// That plugin doesn't integrate with CopyResourcesToAndroidAssetsTask's outputDirectory, so we do it manually.
val onboardingAssetsDir = layout.buildDirectory.dir(
    "compose-feature-assets/composeResources/domatapp.feature.onboarding.presentation.generated.resources"
)
val authAssetsDir = layout.buildDirectory.dir(
    "compose-feature-assets/composeResources/domatapp.feature.auth.presentation.generated.resources"
)

val copyOnboardingResources = tasks.register<Copy>("copyOnboardingComposeResources") {
    dependsOn(":feature:onboarding:presentation:prepareComposeResourcesTaskForCommonMain")
    from(project(":feature:onboarding:presentation").layout.buildDirectory.dir(
        "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
    ))
    into(onboardingAssetsDir)
}

val copyAuthResources = tasks.register<Copy>("copyAuthComposeResources") {
    dependsOn(":feature:auth:presentation:prepareComposeResourcesTaskForCommonMain")
    from(project(":feature:auth:presentation").layout.buildDirectory.dir(
        "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
    ))
    into(authAssetsDir)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        named("main") {
            assets.srcDirs("${layout.buildDirectory.get().asFile}/compose-feature-assets")
        }
    }
}

afterEvaluate {
    listOf("Debug", "Release").forEach { variant ->
        tasks.findByName("merge${variant}Assets")
            ?.dependsOn(copyOnboardingResources, copyAuthResources)
    }
}

dependencies {
    implementation(projects.shared)

    // Core modules
    implementation(projects.core.design)
    implementation(projects.core.navigation)
    implementation(projects.core.presentation)
    implementation(projects.core.serialization)
    implementation(projects.core.remote)
    implementation(projects.core.config)

    // Feature modules
    implementation(projects.feature.auth.domain)
    implementation(projects.feature.auth.data)
    implementation(projects.feature.auth.presentation)
    implementation(projects.feature.onboarding.presentation)
    implementation(projects.feature.home.presentation)

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

    // Navigation 3
    implementation(libs.navigation.nav3.runtime)
    implementation(libs.navigation.nav3.ui)

    api(libs.ui.compose.uiTooling)
}
