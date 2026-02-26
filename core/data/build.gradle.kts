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
                // Genel data sınıfları, base sınıflar ve Mapper'lar burada
                // core:remote ve core:local'i buraya implementation ile bağlayabilirsiniz
                implementation(projects.core.domain)
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
