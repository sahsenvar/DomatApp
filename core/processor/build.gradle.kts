plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.codegen.ksp.core)
    implementation(libs.codegen.kotlinpoet.core)
    implementation(libs.codegen.kotlinpoet.ksp)
}
