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
                implementation(projects.feature.auth.domain)
                implementation(projects.core.remote)
                implementation(projects.core.config)
                implementation(projects.core.data)
                implementation(projects.core.mapping)


                // Libraries =======================================================================
                // Core
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)

                // Room (Entity + DAO definitions)
                implementation(libs.androidx.room.runtime)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", projects.core.processor)
}
