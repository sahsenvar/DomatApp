package com.domatapp.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Material You wallpaper extraction, where the platform has it.
 *
 * Android 12+ returns a scheme derived from the user's wallpaper; every other platform - and older
 * Android - returns `null`, and [DomatTheme] falls back to the static brand scheme. It is an
 * `expect` rather than an `if` because `dynamicLightColorScheme` takes an Android `Context`, which
 * does not exist in `commonMain`.
 */
@Composable
expect fun dynamicDomatColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
fun DomatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dynamicScheme = if (dynamicColor) dynamicDomatColorScheme(darkTheme) else null
    val colorScheme = dynamicScheme
        ?: if (darkTheme) domatDarkColorScheme() else domatLightColorScheme()

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalElevation provides Elevation(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = domatTypography(),
            shapes = DomatShapes,
            content = content,
        )
    }
}
