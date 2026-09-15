plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    // Applied directly rather than through domatapp.cmp.library: this module owns resources, not
    // UI, so it wants the Compose *resource generator* without cmp.library's foundation/material3/
    // ui dependency set. Applying the plugin here (instead of via a convention plugin) is also what
    // makes the type-safe `compose.resources { }` accessor below available to this build script.
    alias(libs.plugins.composeMultiplatform)
    // org.jetbrains.compose refuses to apply on Kotlin 2.x without the Compose compiler plugin.
    alias(libs.plugins.composeCompiler)
}

dependencies {
    // `api`, not `implementation`: the generated `Res` object is this module's public surface, and
    // every consumer of Res.string/Res.drawable/Res.font needs org.jetbrains.compose.resources on
    // its own compile classpath to use it.
    commonMainApi(libs.ui.compose.componentsResources)
    // Declared explicitly even though components-resources needs it too. On Android,
    // components-resources-android's *api* variant (releaseApiElements) exposes only kotlin-stdlib
    // - compose runtime/foundation are in releaseRuntimeElements, i.e. runtime classpath only. The
    // Compose compiler plugin checks the *compile* classpath, so without this the build fails with
    // "The Compose Compiler requires the Compose Runtime to be on the class path, but none could be
    // found" on :core:resource:compileAndroidMain. domatapp.cmp.library declares the same thing for
    // the modules that use it.
    commonMainApi(libs.ui.compose.runtime)
}

compose.resources {
    // The whole app reads these accessors - :core:design, :core:presentation and every feature
    // presentation module - so the generated class must not be internal to :core:resource.
    publicResClass = true
    packageOfResClass = "com.domatapp.core.resource.generated.resources"
    generateResClass = always
}
