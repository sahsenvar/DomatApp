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
    // newer one through more paths than any short list can chase down one at a time - ddmlib,
    // repository and layoutlib-api (excluding those three alone still left a fourth path open,
    // through com.android.tools.analytics-library:shared -> kotlinx-coroutines-core:1.9.0, per a
    // real CI run). None of AGP's own use of org.jetbrains:annotations matters to a headless
    // `pluginManager.apply("com.android.kotlin.multiplatform.library")` call, so exclude the group
    // entirely instead of excluding modules as they turn up. Two `force()` attempts at resolving
    // the version instead (root buildscript classpath; configurations.all here) both left the
    // published `runtimeElements` variant - what the root build substitutes in for this plugin -
    // completely unchanged; excluding changes the declared dependency graph itself, which does
    // propagate to what gets published.
    implementation(libs.buildlogic.android.gradlePlugin) {
        exclude(group = "org.jetbrains", module = "annotations")
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