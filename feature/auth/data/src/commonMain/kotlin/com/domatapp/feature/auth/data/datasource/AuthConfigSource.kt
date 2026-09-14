package com.domatapp.feature.auth.data.datasource

import io.github.semenciuccosmin.preferences.annotations.Clear
import io.github.semenciuccosmin.preferences.annotations.ConstructedBy
import io.github.semenciuccosmin.preferences.annotations.Get
import io.github.semenciuccosmin.preferences.annotations.GetFlow
import io.github.semenciuccosmin.preferences.annotations.Preferences
import io.github.semenciuccosmin.preferences.annotations.Set
import io.github.semenciuccosmin.preferences.annotations.StringPreference
import kotlinx.coroutines.flow.Flow

/** DataStore file name; the file lands at `<platform files dir>/datastore/auth.preferences_pb`. */
private const val PREFERENCES_NAME = "auth"

private const val KEY_ACCESS_TOKEN = "access_token"

/**
 * Local configuration Source for the auth feature, backed by DataStore.
 *
 * KSP (KspPreferences) generates `AuthConfigSourceImpl` and the `actual` half of
 * [AuthConfigSourceConstructor] for every target; obtain an instance through
 * `PreferencesFactory.create(AuthConfigSourceConstructor, preferencesContext())` - see
 * `AuthDataModule`. The binding must be a singleton: two instances would open two DataStores over
 * the same file.
 *
 * Absence is represented by [DEFAULT_ACCESS_TOKEN] rather than `null`. KspPreferences models every
 * primitive preference as non-nullable with a declared default, so [retrieveToken] returns the
 * empty string when no token has been written.
 */
@Preferences(name = PREFERENCES_NAME)
@ConstructedBy(AuthConfigSourceConstructor::class)
interface AuthConfigSource {

    /**
     * Persists the Supabase access token.
     *
     * The parameter must be named `value`: the generated override declares it that way, and a
     * renamed override parameter is a Kotlin warning, which this build treats as an error.
     */
    @Set
    @StringPreference(key = KEY_ACCESS_TOKEN, defaultValue = DEFAULT_ACCESS_TOKEN)
    suspend fun saveToken(value: String)

    /** Reads the stored access token, or [DEFAULT_ACCESS_TOKEN] when none has been written. */
    @Get
    @StringPreference(key = KEY_ACCESS_TOKEN, defaultValue = DEFAULT_ACCESS_TOKEN)
    suspend fun retrieveToken(): String

    /** Emits the access token on every change, starting with the current value. */
    @GetFlow
    @StringPreference(key = KEY_ACCESS_TOKEN, defaultValue = DEFAULT_ACCESS_TOKEN)
    fun observeToken(): Flow<String>

    /**
     * Clears the whole auth preferences store.
     *
     * `@Clear` is all-or-nothing in KspPreferences (`dataStore.edit { it.clear() }`); it has no
     * single-key variant. That is exactly the semantics logout wants, and this store holds only
     * the access token anyway.
     */
    @Clear
    suspend fun clearAll()

    companion object {
        /** Value returned by [retrieveToken] and [observeToken] while no token is stored. */
        const val DEFAULT_ACCESS_TOKEN = ""
    }
}
