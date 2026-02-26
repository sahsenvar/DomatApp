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
                
                // UI Logic (ViewModel / StateFlow) buradan domain UseCase'lerine ulaşır
                implementation(projects.feature.auth.domain)
                
                // String formatlama, UI Event yönetimi için ortak yapılar
                implementation(projects.core.common)
                implementation(projects.core.navigation)
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
