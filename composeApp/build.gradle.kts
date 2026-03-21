import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
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
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.compose.ui)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.compose.components.resources)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    // Koin for Compose
    api(libs.koin.core)
    api(libs.koin.android)
    api(libs.koin.compose)
    implementation(libs.koin.annotations)
    implementation(libs.androidx.core.ktx)
    ksp(libs.koin.ksp.compiler)

    // Ktor (needed for HttpClient configuration)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Navigation 3
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)

    api(libs.compose.uiTooling)
}
