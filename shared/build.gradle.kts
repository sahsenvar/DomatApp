plugins {
    kotlin("multiplatform")
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.koinCompiler)
    alias(libs.plugins.skie)
    alias(libs.plugins.mokoResources)
}

kotlin {
    android {
        namespace = "com.domatapp.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true

            // Exports
            export(projects.core.common)
            export(projects.core.resource)
            export(projects.core.presentation)
            export(projects.feature.auth.domain)
            export(projects.feature.auth.presentation)

            export(libs.concurrency.coroutine.core)
            export(libs.resource.moko.core)
            export(libs.resource.moko.graphics)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.local)
            api(projects.core.config)
            api(projects.core.remote)
            api(projects.core.common)
            api(projects.core.resource)
            api(projects.core.resulting)
            api(projects.core.presentation)

            api(projects.feature.auth.domain)
            api(projects.feature.auth.data)
            api(projects.feature.auth.presentation)
            api(projects.feature.onboarding.presentation)

            api(libs.di.koin.core)
            api(libs.di.koin.annotations)
            api(libs.concurrency.coroutine.core)

            api(libs.resource.moko.core)
            api(libs.resource.moko.graphics)

        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.domatapp.shared")
}

koinCompiler {
    // :shared owns startKoin and aggregates every feature module, so it is the one compilation
    // that sees the whole dependency graph - full compile-time validation belongs here. This
    // replaces the KOIN_CONFIG_CHECK=true KSP argument.
    compileSafety.set(true)
    // Required because this build runs with kotlin.compiler.allWarningsAsErrors=true; the plugin's
    // informational and Kotlin-version-check output defaults to WARNING severity.
    logSeverity.set("info")
    versionCheckSeverity.set("info")
}
