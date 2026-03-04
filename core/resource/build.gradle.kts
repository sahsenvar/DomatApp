plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.mokoResources)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
            }
        }
        androidMain {
            dependencies {}
        }
        iosMain {
            dependencies {}
        }
    }
}

multiplatformResources {
    resourcesPackage.set("com.domatapp.core.resource")
    resourcesClassName.set("MR")
}
