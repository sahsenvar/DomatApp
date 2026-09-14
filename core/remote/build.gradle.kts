plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    // Core modules
    commonMainApi(projects.core.common)
    commonMainApi(projects.core.resulting)
    commonMainApi(projects.core.serialization)

    // Ktor Client
    commonMainApi(libs.ktor.client.core)
    commonMainApi(libs.ktor.utils)
    commonMainImplementation(libs.ktor.client.content.negotiation)
    commonMainImplementation(libs.ktor.serialization.kotlinx.json)
    commonMainImplementation(libs.ktor.client.logging)
    commonMainImplementation(libs.ktor.client.websockets)

    // Supabase
    commonMainApi(libs.supabase.postgrest)
    commonMainApi(libs.supabase.storage)
    commonMainApi(libs.supabase.auth)

    // Coroutines
    commonMainImplementation(libs.kotlinx.coroutines.core)

    // Firebase
    commonMainImplementation(libs.firebase.firestore)

    // Koin
    commonMainImplementation(libs.koin.core)
    commonMainImplementation(libs.koin.annotations)

    // Android
    androidMainImplementation(project.dependencies.platform(libs.firebase.bom))
    androidMainApi(libs.ktor.client.android)
    androidMainImplementation(libs.ktor.client.okhttp)

    // iOS
    iosMainImplementation(libs.ktor.client.darwin)
}
