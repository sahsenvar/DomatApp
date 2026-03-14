plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
}

room {
    schemaDirectory("$projectDir/schemas")
    generateKotlin = true
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
                api(libs.kvStore.datastore)
                api(libs.kvStore.datastore.preferences)

                // Room
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
            }
        }

        androidMain {
            dependencies {
            }
        }

        iosMain {
            dependencies {
            }
        }
    }

}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

// Workaround for KSP implicit dependency error
tasks.configureEach {
    if (name.startsWith("ksp") && name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
