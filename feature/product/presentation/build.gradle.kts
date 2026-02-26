plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.product.domain)
            implementation(projects.core.common)
            implementation(projects.core.navigation)
        }
    }
}