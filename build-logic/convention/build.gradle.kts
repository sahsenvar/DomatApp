plugins {
    `kotlin-dsl`
}

group = "com.domatapp.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    implementation(libs.android.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = libs.plugins.domatapp.kmp.library.get().pluginId
            implementationClass = "com.domatapp.buildlogic.KmpLibraryConventionPlugin"
        }
        register("DiConventionPlugin") {
            id = libs.plugins.domatapp.kmp.di.get().pluginId
            implementationClass = "com.domatapp.buildlogic.DiConventionPlugin"
        }
    }
}