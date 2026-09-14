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
        if (providers.gradleProperty("gezginUseMavenLocal").orNull.toBoolean()) {
            mavenLocal {
                content {
                    includeModule("io.github.sahsenvar", "gezgin-core")
                    includeModule("io.github.sahsenvar", "gezgin-processor")
                    includeModule("io.github.sahsenvar", "gezgin-test")
                }
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
