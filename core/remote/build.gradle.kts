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

                // Core modules
                api(projects.core.resulting)
                api(projects.core.serialization)

                // Ktor Client
                api(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.websockets)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // Firebase
                implementation(libs.firebase.config)
                implementation(libs.firebase.firestore)

                // Koin for KMP
                implementation(libs.koin.core)
                implementation(libs.koin.annotations)
            }
        }

        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                // Ktor Engine for Android
                implementation(libs.ktor.client.okhttp)
            }
        }

        iosMain {
            dependencies {
                // Ktor Engine for iOS
                implementation(libs.ktor.client.darwin)
            }
        }
    }

}
