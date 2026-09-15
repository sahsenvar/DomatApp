package com.domatapp.core.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * The raw brand palette, and the single source of truth for it.
 *
 * These used to live twice: as private vals in this file (for the Material color schemes) and as
 * `<color>` entries in `core/resource/src/androidMain/res/values/colors.xml`, read with
 * `colorResource(R.color.…)` by screens that needed a token Material 3 does not model. That second
 * copy could not survive the move of the UI to `commonMain` - Compose Resources has no color
 * resource type and an Android `R` class does not exist on iOS - so `colors.xml` was deleted and
 * every former `colorResource(R.color.slate_600)`-style call site now reads [DomatColors.Slate600].
 *
 * Prefer `MaterialTheme.colorScheme` wherever it carries the right role; reach in here only for the
 * tokens the scheme does not express (the slate ramp, the overlay alphas, the hero gradient).
 */
object DomatColors {

    // Green brand colors
    val Malachite: Color = Color(0xFF13EC49)
    val MidnightGreen: Color = Color(0xFF102215)
    val Malachite5: Color = Color(0x0D13EC49)
    val Malachite10: Color = Color(0x1A13EC49)
    val Malachite20: Color = Color(0x3313EC49)
    val Malachite30: Color = Color(0x4D13EC49)

    // Slate scale
    val Slate50: Color = Color(0xFFF8FAFC)
    val Slate100: Color = Color(0xFFF1F5F9)
    val Slate200: Color = Color(0xFFE2E8F0)
    val Slate300: Color = Color(0xFFCBD5E1)
    val Slate400: Color = Color(0xFF94A3B8)
    val Slate500: Color = Color(0xFF64748B)
    val Slate600: Color = Color(0xFF475569)
    val Slate700: Color = Color(0xFF334155)
    val Slate800: Color = Color(0xFF1E293B)
    val Slate900: Color = Color(0xFF0F172A)

    // Gray scale
    val CoolGray400: Color = Color(0xFF9CA3AF)
    val Gray900: Color = Color(0xFF111827)

    // Neutrals
    val White: Color = Color(0xFFFFFFFF)
    val Black: Color = Color(0xFF000000)
    val MintWhite: Color = Color(0xFFF6F8F6)

    // Red scale
    val Red100: Color = Color(0xFFFEE2E2)
    val Red500: Color = Color(0xFFEF4444)
    val Red800: Color = Color(0xFF991B1B)
    val Red900: Color = Color(0xFF7F1D1D)

    // Emerald scale
    val Emerald100: Color = Color(0xFFD1FAE5)
    val Emerald600: Color = Color(0xFF059669)

    // Orange scale
    val Orange400: Color = Color(0xFFFB923C)

    // Blue scale
    val Blue100: Color = Color(0xFFDBEAFE)
    val Blue900: Color = Color(0xFF1E3A8A)

    // Overlays
    val White80: Color = Color(0xCCFFFFFF)
    val White90: Color = Color(0xE5FFFFFF)
    val Black55: Color = Color(0x8C000000)

    // Hero gradient
    val HunterGreen: Color = Color(0xFF1A4731)
    val PhthaloGreen: Color = Color(0xFF0D2418)
}

internal fun domatLightColorScheme(): ColorScheme = lightColorScheme(
    primary = DomatColors.Malachite,
    onPrimary = DomatColors.Slate900,
    primaryContainer = DomatColors.Malachite20,
    onPrimaryContainer = DomatColors.MidnightGreen,
    secondary = DomatColors.Slate600,
    onSecondary = DomatColors.White,
    secondaryContainer = DomatColors.Slate100,
    onSecondaryContainer = DomatColors.Slate900,
    tertiary = DomatColors.Blue900,
    onTertiary = DomatColors.White,
    tertiaryContainer = DomatColors.Blue100,
    onTertiaryContainer = DomatColors.Blue900,
    background = DomatColors.White,
    onBackground = DomatColors.Slate900,
    surface = DomatColors.White,
    onSurface = DomatColors.Slate900,
    surfaceVariant = DomatColors.Slate50,
    onSurfaceVariant = DomatColors.Slate600,
    error = DomatColors.Red500,
    onError = DomatColors.White,
    errorContainer = DomatColors.Red100,
    onErrorContainer = DomatColors.Red800,
    outline = DomatColors.Slate300,
    outlineVariant = DomatColors.Slate200,
)

internal fun domatDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = DomatColors.Malachite,
    onPrimary = DomatColors.MidnightGreen,
    primaryContainer = DomatColors.MidnightGreen,
    onPrimaryContainer = DomatColors.Malachite,
    secondary = DomatColors.Slate400,
    onSecondary = DomatColors.Slate900,
    secondaryContainer = DomatColors.Slate800,
    onSecondaryContainer = DomatColors.White,
    tertiary = DomatColors.Blue100,
    onTertiary = DomatColors.Blue900,
    tertiaryContainer = DomatColors.Blue900,
    onTertiaryContainer = DomatColors.Blue100,
    background = DomatColors.Slate900,
    onBackground = DomatColors.White,
    surface = DomatColors.Slate900,
    onSurface = DomatColors.White,
    surfaceVariant = DomatColors.Slate800,
    onSurfaceVariant = DomatColors.Slate400,
    error = DomatColors.Red500,
    onError = DomatColors.White,
    errorContainer = DomatColors.Red900,
    onErrorContainer = DomatColors.Red100,
    outline = DomatColors.Slate400,
    outlineVariant = DomatColors.Slate700,
)
