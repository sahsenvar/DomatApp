package com.domatapp.core.config.preferences

/**
 * The platform handle KspPreferences needs in order to locate the DataStore file.
 *
 * `PreferencesFactory.create(constructor, context)` takes an untyped `Any?` because the value is
 * only meaningful per platform: on Android it must be an `android.content.Context` (KspPreferences
 * resolves `filesDir/datastore/<name>.preferences_pb` from it), while on iOS it is ignored entirely
 * and the path is derived from `NSDocumentDirectory`.
 *
 * This `expect`/`actual` pair keeps that difference out of `commonMain` DI code, so a Koin provider
 * in a feature's data module can stay platform-agnostic:
 *
 * ```kotlin
 * @Single
 * fun provideAuthConfigSource(): AuthConfigSource =
 *     PreferencesFactory.create(AuthConfigSourceConstructor, preferencesContext())
 * ```
 */
expect fun preferencesContext(): Any?
