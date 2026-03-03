plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
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

                // Core common (for BaseViewModel)
                implementation(projects.core.common)
                implementation(projects.core.resulting)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

            }
        }
        androidMain {
            dependencies {
                // Moko MVVM Compose integration for Android
                implementation(libs.moko.mvvm.compose)
            }
        }
        iosMain {
            dependencies {}
        }
    }
}