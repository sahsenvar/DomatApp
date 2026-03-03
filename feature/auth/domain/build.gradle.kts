plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                // Projects ========================================================================
                implementation(projects.core.domain)
                api(projects.core.resulting)
                api(projects.core.common)

                // Libraries =======================================================================
                // Core
                implementation(libs.kotlin.stdlib)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
            }
        }
        androidMain {
            dependencies {}
        }
        iosMain {
            dependencies {}
        }
    }
}