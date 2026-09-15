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
    // No `implementation(libs.buildlogic.android.gradlePlugin)` here (dropped on main,
    // 41d071f): `compileOnly` above is enough for pluginManager.apply(...) to resolve AGP's
    // plugin classes at apply time (the root build's own `plugins {}` block already loads the
    // real AGP plugin classes), and NOT re-declaring it as `implementation` here is what stops
    // AGP's own transitive org.jetbrains:annotations:23.0.0 requirement from leaking into the
    // root build's plugin classpath, where it conflicted with kotlin-dsl's `strictly 13.0` pin.
    // (This module tried two other fixes for that same conflict first - forcing a version, then
    // excluding org.jetbrains:annotations from an `implementation` declaration - both worked
    // around the symptom; this is the simpler fix that removes the cause.)
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