plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.notification.domain)
            implementation(projects.core.remote)
            implementation(projects.core.local)
            implementation(projects.core.data)
        }
    }
}