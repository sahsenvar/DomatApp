plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                api(libs.kotlinx.coroutines.core)

                // Koin for dependency injection
                implementation(libs.koin.core)
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
