plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.skie)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(projects.core.navigation)
            export(libs.moko.resources)
            export(libs.moko.graphics)
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // Core modules
            api(projects.core.navigation)
            api(projects.core.local)
            api(projects.core.remote)
            api(projects.core.common)
            api(projects.core.resource)
            api(projects.core.resulting)
            api(projects.core.serialization)

            // Feature modules
            api(projects.feature.auth.domain)
            api(projects.feature.auth.data)
            api(projects.feature.auth.presentation)

            // Koin for dependency injection
            api(libs.koin.core)
            api(libs.koin.annotations)

            // Coroutines
            api(libs.kotlinx.coroutines.core)
        }
    }
}

dependencies {
    kspCommonMainMetadata(libs.koin.ksp.compiler)
}

ksp {
    arg("KOIN_DEFAULT_MODULE", "false")
    arg("KOIN_CONFIG_CHECK", "true") // Compile-time validation of Koin DI
    arg("KOIN_LOG_TIMES", "true")
}
