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
include(":core:processor")
include(":core:local")
include(":core:resource")
include(":core:locale")
include(":core:common")
include(":core:navigation")
include(":core:domain")
include(":core:data")
include(":core:resulting")
include(":core:serialization")
include(":core:presentation")

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
