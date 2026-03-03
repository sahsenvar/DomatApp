package com.domatapp.buildlogic

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class DiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {

        // Configure KSP for Koin
        pluginManager.withPlugin(KSP_PLUGIN_ID) {
            extensions.configure<KspExtension> {
                arg("KOIN_DEFAULT_MODULE", "false")
                arg("KOIN_CONFIG_CHECK", "true") // Compile-time validation of Koin DI
                arg("KOIN_LOG_TIMES", "true")
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
                    }
                    androidMain {
                        dependencies {
                            implementation("io.insert-koin:koin-android:4.1.1")
                        }
                    }
                }
            }

            afterEvaluate {
                val kmpExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)
                val targetNames = kmpExtension.targets.names

                dependencies {
                    add("kspCommonMainMetadata", "io.insert-koin:koin-ksp-compiler:2.3.1")

                    if ("androidMain" in targetNames)
                        add("kspAndroidMain", "io.insert-koin:koin-ksp-compiler:2.3.1")

                    if ("iosX64" in targetNames)
                        add("kspIosX64", "io.insert-koin:koin-ksp-compiler:2.3.1")

                    if ("iosArm64" in targetNames)
                        add("kspIosArm64", "io.insert-koin:koin-ksp-compiler:2.3.1")

                    if ("iosSimulatorArm64" in targetNames)
                        add("kspIosSimulatorArm64", "io.insert-koin:koin-ksp-compiler:2.3.1")
                }
            }
        }
    }

    companion object {
        const val KSP_PLUGIN_ID = "com.google.devtools.ksp"
        const val KMP_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"

    }
}