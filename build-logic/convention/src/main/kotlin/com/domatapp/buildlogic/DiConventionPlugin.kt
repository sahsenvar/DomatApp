package com.domatapp.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.koin.compiler.plugin.KoinGradleExtension

class DiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {

        pluginManager.withPlugin(KMP_PLUGIN_ID) {
            // Koin Annotations 4.2+ is processed by a Kotlin compiler plugin, not KSP. It is a
            // KotlinCompilerPluginSupportPlugin, so applying it here covers every compilation of
            // the module - commonMain metadata as well as each Android/iOS target.
            pluginManager.apply(KOIN_COMPILER_PLUGIN_ID)
            configureKoinCompiler()

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation("io.insert-koin:koin-core:$KOIN_VERSION")
                            implementation("io.insert-koin:koin-annotations:$KOIN_ANNOTATIONS_VERSION")
                        }
                    }
                    androidMain {
                        dependencies {
                            implementation("io.insert-koin:koin-android:$KOIN_VERSION")
                        }
                    }
                }
            }
        }

        // KSP no longer has anything to do with DI, but modules that run other processors
        // (core:processor, ktorfit-ksp) still need their commonMain metadata output on the source
        // path and their compile tasks ordered after it. Guarded by the KSP plugin so a module
        // that drops KSP entirely does not end up depending on a task that does not exist.
        pluginManager.withPlugin(KSP_PLUGIN_ID) {
            pluginManager.withPlugin(KMP_PLUGIN_ID) {
                extensions.configure<KotlinMultiplatformExtension> {
                    sourceSets {
                        commonMain {
                            // Must name the exact KSP output directory, not an ancestor of it:
                            // Gradle only collapses srcDirs that resolve to the very same File, so
                            // registering a parent here would make every generated file reachable
                            // through two distinct roots at once (the Ktorfit Gradle plugin
                            // registers this same path).
                            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
                        }
                    }
                }

                tasks.withType(KotlinCompilationTask::class.java).configureEach {
                    if (name != "kspCommonMainKotlinMetadata") {
                        dependsOn("kspCommonMainKotlinMetadata")
                    }
                }
            }
        }
    }

    companion object {
        const val KSP_PLUGIN_ID = "com.google.devtools.ksp"
        const val KMP_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"
        const val KOIN_COMPILER_PLUGIN_ID = "io.insert-koin.compiler.plugin"
        const val KOIN_VERSION = "4.2.2"
        const val KOIN_ANNOTATIONS_VERSION = "4.2.2"
    }
}

/**
 * Shared `koinCompiler { }` settings for every module that applies this convention plugin.
 *
 * `:shared` is not one of them - it applies the Koin plugin directly and configures its own
 * `koinCompiler { }` block, where `compileSafety` is turned back on.
 */
internal fun Project.configureKoinCompiler() {
    extensions.configure<KoinGradleExtension> {
        // This project builds with kotlin.compiler.allWarningsAsErrors=true. The Koin plugin emits
        // its informational output and its "unverified Kotlin version" notice at WARNING severity
        // by default, which would fail such a build outright. Both are downgraded to info.
        logSeverity.set("info")
        versionCheckSeverity.set("info")
        // Cross-module DI: each Gradle module's @Module declares only its own definitions and
        // pulls the rest in through @Module(includes = [...]), so per-module graph validation
        // reports dependencies it cannot see. This is the direct replacement for the
        // KOIN_CONFIG_CHECK=false KSP argument it supersedes.
        compileSafety.set(false)
    }
}
