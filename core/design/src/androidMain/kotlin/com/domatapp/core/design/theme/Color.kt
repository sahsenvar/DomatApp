package com.domatapp.core.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Green brand colors
private val Malachite = Color(0xFF13EC49)
private val MidnightGreen = Color(0xFF102215)
private val Malachite5 = Color(0x0D13EC49)
private val Malachite10 = Color(0x1A13EC49)
private val Malachite20 = Color(0x3313EC49)
private val Malachite30 = Color(0x4D13EC49)

// Slate scale
private val Slate50 = Color(0xFFF8FAFC)
private val Slate100 = Color(0xFFF1F5F9)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate300 = Color(0xFFCBD5E1)
private val Slate400 = Color(0xFF94A3B8)
private val Slate600 = Color(0xFF475569)
private val Slate700 = Color(0xFF334155)
private val Slate800 = Color(0xFF1E293B)
private val Slate900 = Color(0xFF0F172A)

// Neutrals
private val White = Color(0xFFFFFFFF)

// Red scale
private val Red100 = Color(0xFFFEE2E2)
private val Red500 = Color(0xFFEF4444)
private val Red800 = Color(0xFF991B1B)
private val Red900 = Color(0xFF7F1D1D)

// Blue scale
private val Blue100 = Color(0xFFDBEAFE)
private val Blue900 = Color(0xFF1E3A8A)

internal fun domatLightColorScheme(): ColorScheme = lightColorScheme(
    primary = Malachite,
    onPrimary = Slate900,
    primaryContainer = Malachite20,
    onPrimaryContainer = MidnightGreen,
    secondary = Slate600,
    onSecondary = White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    tertiary = Blue900,
    onTertiary = White,
    tertiaryContainer = Blue100,
    onTertiaryContainer = Blue900,
    background = White,
    onBackground = Slate900,
    surface = White,
    onSurface = Slate900,
    surfaceVariant = Slate50,
    onSurfaceVariant = Slate600,
    error = Red500,
    onError = White,
    errorContainer = Red100,
    onErrorContainer = Red800,
    outline = Slate300,
    outlineVariant = Slate200,
)

internal fun domatDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = Malachite,
    onPrimary = MidnightGreen,
    primaryContainer = MidnightGreen,
    onPrimaryContainer = Malachite,
    secondary = Slate400,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = White,
    tertiary = Blue100,
    onTertiary = Blue900,
    tertiaryContainer = Blue900,
    onTertiaryContainer = Blue100,
    background = Slate900,
    onBackground = White,
    surface = Slate900,
    onSurface = White,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    error = Red500,
    onError = White,
    errorContainer = Red900,
    onErrorContainer = Red100,
    outline = Slate400,
    outlineVariant = Slate700,
)
