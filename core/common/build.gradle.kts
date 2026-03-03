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

                // Moko MVVM for KMP ViewModel support
                implementation(libs.moko.mvvm.core)
                implementation(libs.moko.mvvm.flow)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
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
