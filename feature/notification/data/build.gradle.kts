plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.notification.domain)
            implementation(projects.core.data)
        }
    }
}