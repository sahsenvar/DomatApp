package com.domatapp.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("unused")
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.kotlin.multiplatform.library")
        }

        // Temel KMP ayarlarını burada merkezi olarak yapabiliriz
        extensions.configure(KotlinMultiplatformExtension::class.java) {
            iosX64()
            iosArm64()
            iosSimulatorArm64()
        }

        // Android Library modülleri için genel config
        extensions.configure("kotlin", Action<KotlinMultiplatformExtension> {
            (this as ExtensionAware).extensions.configure(
                "androidLibrary",
                Action<KotlinMultiplatformAndroidLibraryExtension> {
                    namespace = "com.domatapp" + path.toString().replace(":", ".").replace("-", "_")
                    compileSdk = 36
                    minSdk = 30
                })
        })
    }
}