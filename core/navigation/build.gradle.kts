import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.ksp)
}

dependencies {
    // Gezgin owns the `Route` supertype, the graph/edge annotations, and the Navigation 3 runtime
    // its generated code compiles against - so this module declares no androidx.navigation3
    // coordinate of its own.
    //
    // `commonMain`, not `androidMain`: gezgin-core now publishes `iosArm64` and `iosSimulatorArm64`
    // klibs alongside `android`/`jvm`, so the graph, its generated topology/navigators and the
    // screens that render it are shared by both platforms. There is still no `iosX64` variant -
    // JetBrains' `navigation3-ui` does not publish one - which is why `KmpLibraryConventionPlugin`
    // declares exactly those two Apple targets and Intel Mac simulators are unsupported.
    //
    // `api`, not `implementation`: :core:presentation and every feature presentation module needs
    // the same annotations and the generated navigators, and gets them through this module.
    commonMainApi(libs.navigation.gezgin.core)

    // Emits GezginGenerated.kt (gezginTopology), GezginSerializers.kt (+ gezginJson) and one
    // `<X>Navigator` per route into com.domatapp.core.navigation.
    //
    // Registered on `kspCommonMainMetadata` rather than per target: what Gezgin generates from the
    // graph is platform-independent, so the processor runs once over the common metadata and its
    // output is added to commonMain below. (Contrast KspPreferences, which emits an `actual object`
    // and therefore *must* be registered per target.)
    add("kspCommonMainMetadata", libs.navigation.gezgin.processor)
}

// This module runs KSP but does not apply `domatapp.kmp.di`, which is where that wiring normally
// comes from - so it is repeated here. Name the exact output directory, not an ancestor: Gradle
// only collapses srcDirs resolving to the very same File.
kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}

tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
}

// The per-target KSP tasks are not KotlinCompilationTask, yet they read the same commonMain source
// set the srcDir above just pointed at the metadata processor's output. Gradle 9.7 fails on that
// undeclared producer/consumer edge rather than warning.
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }
    .configureEach { dependsOn("kspCommonMainKotlinMetadata") }
