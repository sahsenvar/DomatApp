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
                // Projects ========================================================================
                api(projects.core.resulting)
                implementation(projects.core.serialization)

                // Libraries =======================================================================
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)

                // DataStore
                api(libs.kvStore.datastore)
                api(libs.kvStore.datastore.preferences)

                // Firebase RemoteConfig
                implementation(libs.firebase.config)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
            }
        }

        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
            }
        }

        iosMain {
            dependencies {
            }
        }
    }
}
