plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.ksp)
}

dependencies {
    // Gezgin owns the `Route` supertype, the graph/edge annotations, and the Navigation 3 runtime
    // its generated code compiles against - so this module declares no androidx.navigation3
    // coordinate of its own.
    //
    // androidMain only, deliberately: gezgin-core publishes `android` and `jvm` targets and has no
    // iOS klib. The graph, its generated topology/navigators and every screen that renders it are
    // Android-only anyway - iOS drives its own SwiftUI NavigationStack from
    // iosApp/.../NavigationRouter.swift and never consumed the old Kotlin `Route` type.
    //
    // `api`, not `implementation`: :core:presentation and every feature presentation module needs
    // the same annotations and the generated navigators, and gets them through this module.
    androidMainApi(libs.navigation.gezgin.core)

    // Emits GezginGenerated.kt (gezginTopology), GezginSerializers.kt (+ gezginJson) and one
    // `<X>Navigator` per route into com.domatapp.core.navigation.
    kspAndroid(libs.navigation.gezgin.processor)
}
