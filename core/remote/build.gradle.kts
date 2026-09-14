plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
}

dependencies {
    // Core modules
    commonMainApi(projects.core.common)
    commonMainApi(projects.core.resulting)

    // Ktor Client
    commonMainApi(libs.network.ktorClient.core)
    commonMainApi(libs.network.ktor.utils)
    commonMainImplementation(libs.network.ktorClient.contentNegotiation)
    commonMainImplementation(libs.network.ktorClient.kxSerializationJson)
    commonMainImplementation(libs.network.ktorClient.logging)
    commonMainImplementation(libs.network.ktorClient.websockets)

    // KtorfitX runtime: core:remote builds the Ktorfitx instance, feature modules declare the
    // @Api interfaces. The KtorfitX Gradle plugin is applied in those feature modules, not here,
    // because that is where the KSP processor has to run.
    commonMainApi(libs.network.ktorfitx.core)

    // Supabase. Declared but not yet used by any Kotlin source - Supabase is the live backend
    // (see supabaseHost / publicKey in provideHttpClient), so these are kept for the SDK work.
    commonMainApi(libs.network.supabase.postgrest)
    commonMainApi(libs.network.supabase.storage)
    commonMainApi(libs.network.supabase.auth)

    // Coroutines
    commonMainImplementation(libs.concurrency.coroutine.core)

    // Android
    androidMainApi(libs.network.ktorClient.android)
    androidMainImplementation(libs.network.ktorClient.okhttp)

    // iOS
    iosMainImplementation(libs.network.ktorClient.darwin)
}
