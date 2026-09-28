plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.test)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.wallet.domain)
            implementation(projects.core.presentation)
        }
    }
}