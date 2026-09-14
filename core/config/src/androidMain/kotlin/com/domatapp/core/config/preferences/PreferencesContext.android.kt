package com.domatapp.core.config.preferences

import android.annotation.SuppressLint
import android.content.Context

/**
 * Holds the application `Context` that KspPreferences needs to resolve the DataStore file path.
 *
 * Must be assigned from `Application.onCreate` before Koin resolves any `@Preferences` Source.
 * Holding the *application* context is deliberate and leak-free; the lint suppression exists only
 * because the detector cannot distinguish it from an Activity context.
 */
@SuppressLint("StaticFieldLeak")
object PreferencesContextHolder {
    lateinit var context: Context
}

actual fun preferencesContext(): Any? = PreferencesContextHolder.context
