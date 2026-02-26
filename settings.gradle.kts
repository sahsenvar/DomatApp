@file:Suppress("UnstableApiUsage", "UnstableApiUsage", "UnstableApiUsage", "UnstableApiUsage",
    "UnstableApiUsage", "UnstableApiUsage", "UnstableApiUsage"
)

rootProject.name = "DomatApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
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

include(":composeApp")
include(":shared")
include(":core:remote")
include(":core:local")
include(":core:resource")
include(":core:localization")
include(":core:common")
include(":core:navigation")
include(":core:domain")
include(":core:data")
include(":feature:auth:domain")
include(":feature:auth:data")
include(":feature:auth:presentation")
