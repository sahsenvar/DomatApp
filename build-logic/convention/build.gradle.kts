plugins {
    `kotlin-dsl`
}

group = "com.domatapp.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    implementation(libs.android.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "domatapp.kmp.library"
            implementationClass = "com.domatapp.buildlogic.KmpLibraryConventionPlugin"
        }
    }
}