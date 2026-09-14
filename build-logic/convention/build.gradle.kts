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
    // `kotlin-dsl` (applied above) pins org.jetbrains:annotations to `strictly` its embedded
    // Kotlin's own version, to protect Gradle's bundled Kotlin runtime. AGP transitively wants a
    // newer one, but only via ddmlib/repository/layoutlib-api - Studio/IDE-integration tooling
    // (device communication, SDK manager, layout preview rendering) that a headless plugin
    // application never touches. Excluding those three modules removes the only paths that
    // conflict with the strict pin; two `force()` attempts at resolving the version instead (one on
    // the root's buildscript classpath, one here via configurations.all) both left the published
    // `runtimeElements` variant - what the root build substitutes in for this plugin - completely
    // unchanged, confirmed by two identical CI failures. Excluding the modules changes the declared
    // dependency graph itself, which does propagate to what gets published.
    implementation(libs.buildlogic.android.gradlePlugin) {
        exclude(group = "com.android.tools.ddms", module = "ddmlib")
        exclude(group = "com.android.tools", module = "repository")
        exclude(group = "com.android.tools.layoutlib", module = "layoutlib-api")
    }
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