package com.domatapp.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

@Suppress("unused")
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")


        extensions.configure(KotlinMultiplatformExtension::class.java) {
            applyDefaultHierarchyTemplate()
            iosX64()
            iosArm64()
            iosSimulatorArm64()

            compilerOptions {
                freeCompilerArgs.addAll(
                    "-Xexpect-actual-classes",
                )

                optIn.addAll(
                    "kotlin.time.ExperimentalTime",
                    "kotlin.ExperimentalStdlibApi",
                    "kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "kotlinx.serialization.ExperimentalSerializationApi"
                )

                extraWarnings.set(true)

            }
            (this as ExtensionAware).extensions.configure(KotlinMultiplatformAndroidLibraryExtension::class.java) {
                namespace = "com.domatapp" + path.replace(":", ".").replace("-", "_")
                compileSdk = 36
                minSdk = 30
            }
        }

        tasks.withType(KotlinJvmCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }

        tasks.withType(JavaCompile::class.java).configureEach {
            sourceCompatibility = JavaVersion.VERSION_17.toString()
            targetCompatibility = JavaVersion.VERSION_17.toString()
        }
    }
}