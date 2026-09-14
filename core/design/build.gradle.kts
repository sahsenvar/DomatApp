plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
}

dependencies {
    commonMainImplementation(projects.core.resource)
}
