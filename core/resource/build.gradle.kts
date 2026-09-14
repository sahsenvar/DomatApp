plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.mokoResources)
}

dependencies {
    commonMainApi(libs.resource.moko.core)
    commonMainApi(libs.resource.moko.compose)
}

multiplatformResources {
    resourcesPackage.set("com.domatapp.core.resource")
    resourcesClassName.set("MR")
}
