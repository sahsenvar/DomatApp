package com.domatapp.buildlogic

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class DiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {

        // Configure KSP for Koin
        pluginManager.withPlugin(KSP_PLUGIN_ID) {
            extensions.configure<KspExtension> {
                arg("KOIN_DEFAULT_MODULE", "false")
                arg("KOIN_CONFIG_CHECK", "false") // Disabled: doesn't support cross-module validation in multi-module KMP
                arg("KOIN_LOG_TIMES", "true")
            }

            // Add KSP compiler for commonMain metadata processing
            dependencies {
                add("kspCommonMainMetadata", "io.insert-koin:koin-ksp-compiler:2.3.1")
            }
        }

        pluginManager.withPlugin(KMP_PLUGIN_ID) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets{
                    commonMain {
                        dependencies {
                            implementation("io.insert-koin:koin-core:4.1.1")
                            implementation("io.insert-koin:koin-annotations:2.3.1")
                        }
                        // Include KSP-generated code from commonMain metadata in all targets
                        kotlin.srcDir("build/generated/ksp/metadata")
                    }
                    androidMain {
                        dependencies {
                            implementation("io.insert-koin:koin-android:4.1.1")
                        }
                    }
                }
            }

            // Ensure all compilation tasks depend on KSP metadata generation
            project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
                if (name != "kspCommonMainKotlinMetadata") {
                    dependsOn("kspCommonMainKotlinMetadata")
                }
            }
        }
    }

    companion object {
        const val KSP_PLUGIN_ID = "com.google.devtools.ksp"
        const val KMP_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"

    }
}
