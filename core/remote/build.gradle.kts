plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    // Core modules
    commonMainApi(projects.core.common)
    commonMainApi(projects.core.resulting)
    commonMainApi(projects.core.serialization)

    // Ktor Client
    commonMainApi(libs.network.ktorClient.core)
    commonMainApi(libs.network.ktor.utils)
    commonMainImplementation(libs.network.ktorClient.contentNegotiation)
    commonMainImplementation(libs.network.ktorClient.kxSerializationJson)
    commonMainImplementation(libs.network.ktorClient.logging)
    commonMainImplementation(libs.network.ktorClient.websockets)

    // Supabase
    commonMainApi(libs.network.supabase.postgrest)
    commonMainApi(libs.network.supabase.storage)
    commonMainApi(libs.network.supabase.auth)

    // Coroutines
    commonMainImplementation(libs.concurrency.coroutine.core)

    // Firebase
    commonMainImplementation(libs.backend.firebase.firestore)

    // Koin
    commonMainImplementation(libs.di.koin.core)
    commonMainImplementation(libs.di.koin.annotations)

    // Android
    androidMainImplementation(project.dependencies.platform(libs.backend.firebase.bom))
    androidMainApi(libs.network.ktorClient.android)
    androidMainImplementation(libs.network.ktorClient.okhttp)

    // iOS
    iosMainImplementation(libs.network.ktorClient.darwin)
}
