plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    commonMainApi(projects.core.resulting)
}
