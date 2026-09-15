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

                // com.android.kotlin.multiplatform.library keeps Android resource processing off
                // by default, so without this a module's src/androidMain/res is ignored and no R
                // class is generated for it. Until now Moko Resources' Gradle plugin turned this on
                // as a side effect (dev.icerock.gradle.utils.enableAndroidResources), which is why
                // com.domatapp.core.resource.R resolved while Moko was applied. Moko is gone, so
                // the project has to ask for it itself - :core:resource's colors.xml, and Compose
                // Resources' Android asset packaging, both depend on it.
                androidResources.enable = true
            }
        }

        // JVM 21, not 17: kmapper-core 2.2.2's published classes are compiled targeting JVM 21
        // bytecode (verified directly - major version 65 in the class file header). Its
        // KSP-generated mapper calls an inline function from that runtime, and Kotlin refuses to
        // inline JVM-21 bytecode into a lower-targeted compilation. Raising the floor to 21 is the
        // fix; a jvmToolchain() pin was tried first and made no difference; a plain compiler-option
        // target does.
        tasks.withType(KotlinJvmCompile::class.java).configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        }

        tasks.withType(JavaCompile::class.java).configureEach {
            sourceCompatibility = JavaVersion.VERSION_21.toString()
            targetCompatibility = JavaVersion.VERSION_21.toString()
        }
    }
}