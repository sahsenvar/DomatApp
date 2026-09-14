package com.domatapp.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
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

            // The Koin runtime belongs here, not in every consumer's build file: a module that
            // applies this plugin gets @Module/@Single processed, so it always needs koin-core and
            // koin-annotations. Module-specific artifacts (koin-core-viewmodel, koin-compose) stay
            // declared per module. Versions come from the catalog so they cannot drift from it.
            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation(libs.findLibrary("di-koin-core").get())
                            implementation(libs.findLibrary("di-koin-annotations").get())
                        }
                    }
                    androidMain {
                        dependencies {
                            implementation(libs.findLibrary("di-koin-android").get())
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
