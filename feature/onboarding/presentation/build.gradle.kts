plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
                implementation(projects.core.presentation)
                implementation(projects.core.common)
                implementation(projects.core.navigation)
                implementation(projects.core.resource)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.koin.core.viewmodel)
            }
        }
        androidMain {
            dependencies {
                implementation(projects.core.design)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.uiTooling)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.koin.compose)
                implementation(libs.navigation3.runtime)
            }
        }
        iosMain {
            dependencies {}
        }
    }
}

dependencies {
    add("kspAndroid", projects.core.processor)
}

tasks.matching { it.name == "kspAndroidMain" }.configureEach {
    dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}

compose.resources {
    generateResClass = never
}

