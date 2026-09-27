plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    commonMainImplementation(projects.core.domain)
}
