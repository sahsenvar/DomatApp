@file:Suppress("UnstableApiUsage")

rootProject.name = "DomatApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        // Opt-in escape hatch for validating an unreleased Gezgin against this app before it is
        // published: `./gradlew publishToMavenLocal` in the Gezgin checkout, then build here with
        // `-PgezginUseMavenLocal=true` and the `gezgin` version in libs.versions.toml pointing at
        // the locally published version. Off by default - no property, no mavenLocal.
        // `gezgin-(core|processor|test)` are KMP modules: each publishes per-target artifacts
        // under their own module names (e.g. `gezgin-core-android`, `gezgin-core-iosarm64`), not
        // just the three "root" names below. A real CI run proved this the hard way -
        // `includeModule("io.github.sahsenvar", "gezgin-core")` alone resolves the root module's
        // own metadata fine, but leaves `gezgin-core-android` unmatched, so Gradle falls through to
        // mavenCentral()/google() for it and fails there instead. `includeModuleByRegex` with an
        // optional `-<target>` suffix is what actually needs to be scoped.
        val gezginModuleNameRegex = "gezgin-(core|processor|test)(-.+)?"
        if (providers.gradleProperty("gezginUseMavenLocal").orNull.toBoolean()) {
            mavenLocal {
                content {
                    includeModuleByRegex("io\\.github\\.sahsenvar", gezginModuleNameRegex)
                }
            }
        }
        // `mavenCentral()` below only serves released coordinates - it 404s on any -SNAPSHOT.
        // `gezgin` is pinned to 0.3.0-SNAPSHOT (owner's instruction, 2026-09-15: 0.3.0 itself
        // isn't published yet, but the snapshot already carries @ScreenWrapper), so its actual
        // artifacts come from Central's separate snapshots repository instead. Scoped to just the
        // Gezgin modules (see gezginModuleNameRegex above), so this repository is never consulted
        // for anything else.
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
            content {
                includeModuleByRegex("io\\.github\\.sahsenvar", gezginModuleNameRegex)
            }
        }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

includeBuild("build-logic")

include(":composeApp")
include(":shared")

// Core Modules
include(":core:remote")
include(":core:local")
include(":core:config")
include(":core:resource")
include(":core:common")
include(":core:navigation")
include(":core:domain")
include(":core:data")
include(":core:resulting")
include(":core:presentation")
include(":core:design")
include(":core:analytics")

// Features Modules
include(":feature:auth:domain")
include(":feature:auth:data")
include(":feature:auth:presentation")
include(":feature:onboarding:domain")
include(":feature:onboarding:data")
include(":feature:onboarding:presentation")
include(":feature:home:domain")
include(":feature:home:data")
include(":feature:home:presentation")
include(":feature:profile:domain")
include(":feature:profile:data")
include(":feature:profile:presentation")
include(":feature:product:domain")
include(":feature:product:data")
include(":feature:product:presentation")
include(":feature:notification:domain")
include(":feature:notification:data")
include(":feature:notification:presentation")
include(":feature:wallet:domain")
include(":feature:wallet:data")
include(":feature:wallet:presentation")
