plugins {
    kotlin("multiplatform")
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.ksp)
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
            export(projects.core.navigation)
            export(projects.core.common)
            export(projects.core.resource)
            export(projects.core.presentation)
            export(projects.feature.auth.domain)
            export(projects.feature.auth.presentation)

            export(libs.kotlinx.coroutines.core)
            export(libs.moko.resources)
            export(libs.moko.graphics)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.navigation)
            api(projects.core.local)
            api(projects.core.remote)
            api(projects.core.common)
            api(projects.core.resource)
            api(projects.core.resulting)
            api(projects.core.serialization)
            api(projects.core.presentation)

            api(projects.feature.auth.domain)
            api(projects.feature.auth.data)
            api(projects.feature.auth.presentation)

            api(libs.koin.core)
            api(libs.koin.annotations)
            api(libs.kotlinx.coroutines.core)

            api(libs.moko.resources)
            api(libs.moko.graphics)
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.domatapp.shared")
}

dependencies {
    kspCommonMainMetadata(libs.koin.ksp.compiler)
}

ksp {
    arg("KOIN_DEFAULT_MODULE", "false")
    arg("KOIN_CONFIG_CHECK", "true")
    arg("KOIN_LOG_TIMES", "true")
}
