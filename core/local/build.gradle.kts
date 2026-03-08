plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("DomatAppDatabase") {
            packageName.set("com.domatapp.core.local.database")
        }
    }
}

kotlin {

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                // Projects ========================================================================
                implementation(projects.core.serialization)
                api(projects.core.resulting)

                // Libraries =======================================================================
                implementation(libs.kotlin.stdlib)

                // Multiplatform Settings
                implementation(libs.multiplatform.settings)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kvStore.datastore)
                implementation(libs.kvStore.datastore.preferences)

                // SQLDelight
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.sqldelight.android.driver)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.sqldelight.native.driver)
            }
        }
    }

}
