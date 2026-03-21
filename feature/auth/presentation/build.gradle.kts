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

                // Presentation layer only depends on domain
                implementation(projects.feature.auth.domain)

                // Core presentation (for BaseViewModel)
                implementation(projects.core.presentation)

                // Core common
                implementation(projects.core.common)
                implementation(projects.core.resulting)

                // Navigation
                implementation(projects.core.navigation)

                // Resources (Moko Resources for i18n)
                implementation(projects.core.resource)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // Koin ViewModel (for @KoinViewModel generated code)
                implementation(libs.koin.core.viewmodel)


            }
        }
        androidMain {
            dependencies {
                // Design system
                implementation(projects.core.design)

                // Compose
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.uiTooling)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.koin.compose)

                // Navigation3 (for generated entries extension)
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

// Ensure kspAndroidMain runs after kspCommonMainKotlinMetadata (Koin KSP)
tasks.matching { it.name == "kspAndroidMain" }.configureEach {
    dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}

compose.resources {
    generateResClass = never
}
