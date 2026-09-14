plugins {
    `kotlin-dsl`
}

group = "com.domatapp.buildlogic"

dependencies {
    compileOnly(libs.buildlogic.android.gradlePlugin)
    compileOnly(libs.buildlogic.kotlin.gradlePlugin)
    compileOnly(libs.buildlogic.ksp.gradlePlugin)
    compileOnly(libs.buildlogic.compose.gradlePlugin)
    compileOnly(libs.buildlogic.composeCompiler.gradlePlugin)
    implementation(libs.buildlogic.android.gradlePlugin)
    implementation(libs.buildlogic.ksp.gradlePlugin)
    implementation(libs.buildlogic.koinCompiler.gradlePlugin)
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
        register("cmpLibrary") {
            id = libs.plugins.domatapp.cmp.library.get().pluginId
            implementationClass = "com.domatapp.buildlogic.CmpLibraryConventionPlugin"
        }
    }
}