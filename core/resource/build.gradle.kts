plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.mokoResources)
}

dependencies {
    commonMainApi(libs.moko.resources)
    commonMainApi(libs.moko.resources.compose)
}

multiplatformResources {
    resourcesPackage.set("com.domatapp.core.resource")
    resourcesClassName.set("MR")
}
