import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.test)
    alias(libs.plugins.domatapp.cmp.library)
    alias(libs.plugins.ksp)
}

dependencies {
    commonMainImplementation(projects.feature.home.domain)
    commonMainImplementation(projects.core.common)
    // :core:presentation re-exposes :core:domain, :core:common, :core:navigation, :core:resource,
    // :core:design, the lifecycle Compose artifacts and the Koin ViewModel/Compose artifacts as api.
    commonMainImplementation(projects.core.presentation)

    // Generates provideHomeEntry(), wired through :core:presentation's @ScreenWrapper.
    add("kspCommonMainMetadata", libs.navigation.gezgin.processor)
}

// Unlike the other presentation modules this one declares no Koin definitions, so it does not apply
// `domatapp.kmp.di` - which is where the KSP source-directory and task-ordering wiring normally
// comes from. Repeated here. Name the exact output directory, not an ancestor: Gradle only collapses
// srcDirs resolving to the very same File.
kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}

tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if (name != "kspCommonMainKotlinMetadata") dependsOn("kspCommonMainKotlinMetadata")
}

tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }
    .configureEach { dependsOn("kspCommonMainKotlinMetadata") }

// DomatScreenRoot is compiled into :core:presentation, and KSP cannot enumerate annotated
// declarations on the classpath - so this module names it. `wrapperDeclarations` (the wrapper's
// fully-qualified name) rather than `wrapperPackages` (its package): the latter enumerates the
// package, which returns nothing when the dependency arrives as Kotlin *metadata*, as it does in a
// kspCommonMainMetadata round. The @ScreenSlot markers need no naming - Gezgin reaches them through
// the wrapper's @FilledBy parameters.
//
// Without this the entries here are generated UNWRAPPED (a KSP warning, not a build failure).
ksp {
    arg("gezgin.wrapperDeclarations", "com.domatapp.core.presentation.screen.DomatScreenRoot")
}
