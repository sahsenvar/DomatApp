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
                api(project(":core:domain"))
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)

                // Koin for dependency injection
                implementation(libs.koin.core)

                // Genel data sınıfları, base sınıflar ve Mapper'lar burada
                // core:remote ve core:local'i buraya implementation ile bağlayabilirsiniz
            }
        }
        androidMain {
            dependencies {

            }
        }
        iosMain {
            dependencies {}
        }
    }
}
