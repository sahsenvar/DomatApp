package com.domatapp.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused")
class CmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

        // Configure Compose Compiler metrics (only when property is set)
        extensions.configure(ComposeCompilerGradlePluginExtension::class.java) {
            if (providers.gradleProperty("compose.compiler.metrics").map { it == "true" }
                    .getOrElse(false)) {
                metricsDestination.set(layout.buildDirectory.dir("compose-metrics"))
                reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
            }
        }

        // Add common Compose Multiplatform dependencies
        extensions.configure(KotlinMultiplatformExtension::class.java) {
            sourceSets.named("commonMain") {
                dependencies {
                    api(libs.findLibrary("compose-runtime").get())
                    implementation(libs.findLibrary("compose-foundation").get())
                    implementation(libs.findLibrary("compose-material3").get())
                    implementation(libs.findLibrary("compose-ui").get())
                    // @Preview annotation — CMP artifact, compiles for all targets
                    implementation(libs.findLibrary("compose-uiToolingPreview").get())
                }
            }
            sourceSets.named("androidMain") {
                dependencies {
                    // Actual preview rendering engine — Android-only
                    implementation(libs.findLibrary("compose-uiTooling").get())
                }
            }
        }
    }
}
