import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.koinCompiler)
    alias(libs.plugins.skie)
    // New: :shared now carries the shared Compose root - `DomatApp` and the iOS
    // `MainViewController` - so it needs the Compose compiler and the Compose dependency set
    // (foundation / material3 / ui reach it as `implementation` from :core:presentation, which does
    // not re-export them). Using the convention plugin rather than applying org.jetbrains.compose
    // by hand keeps that set identical to every other UI module's.
    //
    // It also closes the gap CLAUDE.md flagged: without the Compose Gradle plugin on :shared,
    // Compose Resources' iOS resource-sync task never ran for Shared.framework, so reading a string
    // through StringResourceApi on iOS would have failed at runtime.
    alias(libs.plugins.domatapp.cmp.library)
}

// The Apple framework / XCFramework / Swift package all carry this one name.
val xcframeworkName = "Shared"

kotlin {
    android {
        namespace = "com.domatapp.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    // One XCFramework over both Apple targets. It is what `Package.swift` in
    // `iosApp/Packages/Shared` wraps as a `.binaryTarget`, replacing the hardcoded
    // `../shared/build/bin/iosSimulatorArm64/debugFramework/Shared.framework` path the Xcode
    // project used to link against.
    val xcf = XCFramework(xcframeworkName)

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = xcframeworkName
            // Static, as Swift package `.binaryTarget`s are consumed; also what the framework was
            // before.
            isStatic = true
            xcf.add(this)

            // Exports
            export(projects.core.common)
            export(projects.core.navigation)
            export(projects.core.resource)
            export(projects.core.presentation)
            export(projects.feature.auth.domain)
            export(projects.feature.auth.presentation)

            export(libs.concurrency.coroutine.core)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.core.local)
            api(projects.core.config)
            api(projects.core.remote)
            api(projects.core.common)
            api(projects.core.resource)
            api(projects.core.resulting)
            api(projects.core.navigation)
            api(projects.core.presentation)

            api(projects.feature.auth.domain)
            api(projects.feature.auth.data)
            api(projects.feature.auth.presentation)
            api(projects.feature.onboarding.presentation)
            // New: `DomatApp` registers all three entry bundles, so the host's graph is complete.
            api(projects.feature.home.presentation)

            api(libs.di.koin.core)
            api(libs.di.koin.annotations)
            api(libs.concurrency.coroutine.core)
        }
    }
}

koinCompiler {
    // :shared owns startKoin and aggregates every feature module, so it is the one compilation
    // that sees the whole dependency graph - full compile-time validation belongs here. This
    // replaces the KOIN_CONFIG_CHECK=true KSP argument.
    compileSafety.set(true)
    // Required because this build runs with kotlin.compiler.allWarningsAsErrors=true; the plugin's
    // informational and Kotlin-version-check output defaults to WARNING severity.
    logSeverity.set("info")
    versionCheckSeverity.set("info")
}

// --- Swift Package Manager hand-off -------------------------------------------------------------
//
// SwiftPM requires a `.binaryTarget`'s path to resolve inside the package directory, so the
// assembled XCFramework is copied next to `Package.swift` rather than referenced in `build/`.
// The copy is gitignored; run one of these before opening Xcode (and after any Kotlin change):
//
//     ./gradlew :shared:syncDebugSharedXCFramework      # simulator + device, debug
//     ./gradlew :shared:syncReleaseSharedXCFramework    # for archiving
//
// Apple targets only link on macOS, so these tasks are a no-op path for the Linux CI job, which
// builds `:composeApp:assembleDebug` and nothing Apple.
val swiftPackageDir = rootProject.layout.projectDirectory.dir("iosApp/Packages/Shared")

listOf("Debug", "Release").forEach { configuration ->
    tasks.register<Sync>("sync${configuration}SharedXCFramework") {
        group = "ios"
        description =
            "Assembles the $configuration $xcframeworkName.xcframework and copies it next to Package.swift."
        dependsOn("assemble${xcframeworkName.replaceFirstChar(Char::uppercase)}${configuration}XCFramework")
        from(
            layout.buildDirectory.dir(
                "XCFrameworks/${configuration.lowercase()}/$xcframeworkName.xcframework"
            )
        )
        into(swiftPackageDir.dir("$xcframeworkName.xcframework"))
    }
}
