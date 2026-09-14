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
            // Pins the actual JDK used to compile Kotlin/Java to 17, independent of whatever JVM
            // runs the Gradle daemon itself. Without this, a KSP-generated source (e.g. KMapper's
            // metadata-compilation output) can pick up the daemon's own JVM as its target - CI
            // hit this for real once its Gradle process moved to JDK 21 for KtorfitX's plugin:
            // "Cannot inline bytecode built with JVM target 21 into bytecode that is being built
            // with JVM target 17."
            jvmToolchain(17)

            applyDefaultHierarchyTemplate()
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
                compileSdk = 37
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