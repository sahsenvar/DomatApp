package com.domatapp.feature.auth.data.datasource

import io.github.semenciuccosmin.preferences.factory.PreferencesConstructor

/**
 * Constructor handle for [AuthConfigSource], declared `expect` so KSP can emit the matching
 * `actual object` per target.
 *
 * This is the only supported way to instantiate a `@Preferences` Source in a KMP module: the
 * reflection-based `PreferencesFactory.create<T>()` overload is Android/JVM-only and its iOS
 * `actual` throws at runtime.
 */
expect object AuthConfigSourceConstructor : PreferencesConstructor<AuthConfigSource>
