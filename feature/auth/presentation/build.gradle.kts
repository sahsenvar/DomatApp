plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                // Presentation layer only depends on domain
                implementation(projects.feature.auth.domain)

                // Core presentation (for BaseViewModel)
                implementation(projects.core.presentation)

                // Core common
                implementation(projects.core.common)
                implementation(projects.core.resulting)

                // Navigation
                implementation(projects.core.navigation)

                // Resources (Moko Resources for i18n)
                implementation(projects.core.resource)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

            }
        }
        androidMain {
            dependencies {
                // Compose
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.koin.compose)
            }
        }
        iosMain {
            dependencies {}
        }
    }
}
