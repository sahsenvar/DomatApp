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
                implementation(projects.core.local)
                implementation(projects.core.data)

                // Libraries =======================================================================
                // Core
                implementation(libs.kotlin.stdlib)

                // Serialization
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", projects.core.processor)
}
