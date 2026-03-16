plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

kotlin {

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.core.resulting)
            }
        }
    }

}
