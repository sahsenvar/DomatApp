plugins {
    alias(libs.plugins.domatapp.kmp.library)
}

dependencies {
    // Exposed as `api` on purpose, reaching feature data modules through :core:data.
    //
    // KspPreferences generates each `@Preferences` Source implementation *into the feature module
    // that declares the interface*, and that generated code references both the KspPreferences
    // runtime (`createDataStore`, `PreferencesFactory`, `PreferencesConstructor`) and DataStore
    // itself (`DataStore<Preferences>`, `stringPreferencesKey`, `edit`). Declaring the set here
    // once keeps every feature from re-declaring it, the same rationale as :core:data/:core:presentation.
    commonMainApi(libs.persistence.kspPreferences.annotations)
    commonMainApi(libs.persistence.dataStore.core)
    commonMainApi(libs.persistence.dataStore.preferences)
}
