plugins {
    `kotlin-dsl`
}

group = "com.domatapp.buildlogic"

// `kotlin-dsl` pins org.jetbrains:annotations to `strictly` its embedded Kotlin's own version (to
// protect Gradle's bundled Kotlin runtime), while AGP - a real runtime dependency below, needed for
// pluginManager.apply("com.android.kotlin.multiplatform.library") to resolve at all, not just to
// compile against - transitively wants a newer one via ddmlib/repository/layoutlib-api. Because this
// project's `runtimeElements` variant is what the root build substitutes in for the
// `domatapp.kmp.library` etc. plugin IDs, an unresolved conflict here surfaces as a "root (classpath)"
// failure in the *consuming* build, which is the wrong place to fix it: the constraint is added to
// this project's own configurations by `kotlin-dsl`, so `force` has to be set here to actually change
// what gets published. org.jetbrains:annotations has never had a breaking release - it only adds
// annotations - so forcing the newer version is low-risk.
configurations.all {
    resolutionStrategy {
        force("org.jetbrains:annotations:23.0.0")
    }
}

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