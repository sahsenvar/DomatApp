package com.domatapp.core.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/** iOS has no wallpaper-derived palette, so the static brand scheme always wins. */
@Composable
actual fun dynamicDomatColorScheme(darkTheme: Boolean): ColorScheme? = null
