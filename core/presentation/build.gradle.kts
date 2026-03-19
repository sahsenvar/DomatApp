plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.domain)
                implementation(projects.core.common)
                implementation(projects.core.navigation)

                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)

                // Lifecycle ViewModel for KMP
                api(libs.androidx.lifecycle.viewmodel)

                // Compose Runtime (For ViewModels/State)
                api(libs.compose.runtime)
            }
        }
        androidMain {
            dependencies {
                implementation(projects.core.design)
                implementation(libs.compose.material3)
            }
        }
        iosMain {
            dependencies {}
        }
    }
}
