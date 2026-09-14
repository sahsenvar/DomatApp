plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
}

dependencies {
    commonMainImplementation(projects.feature.home.domain)
    commonMainImplementation(projects.core.common)
    commonMainImplementation(projects.core.navigation)
}
