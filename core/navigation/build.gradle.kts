plugins {
    alias(libs.plugins.domatapp.kmp.library)
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
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
