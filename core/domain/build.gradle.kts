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
                api(libs.arrow.core)
                api(libs.arrow.fx.coroutines)
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
