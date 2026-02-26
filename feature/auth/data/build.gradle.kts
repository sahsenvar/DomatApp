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
                
                // Data modülü, kendi domain modülündeki Interface'leri implement eder
                implementation(projects.feature.auth.domain)
                
                // İhtiyaca göre altyapı servisleri çekilebilir
                implementation(projects.core.remote)
                implementation(projects.core.local)
                implementation(projects.core.data)
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
