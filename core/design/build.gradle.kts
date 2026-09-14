plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.cmp.library)
}

dependencies {
    // api: consumers (core:presentation, feature presentations) resolve
    // com.domatapp.core.resource.R transitively through the design system.
    commonMainApi(projects.core.resource)
}
