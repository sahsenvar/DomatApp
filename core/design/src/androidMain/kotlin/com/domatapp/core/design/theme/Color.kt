package com.domatapp.core.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.domatapp.core.resource.MR
import dev.icerock.moko.resources.compose.colorResource

@Composable
internal fun domatLightColorScheme(): ColorScheme = lightColorScheme(
    primary = colorResource(MR.colors.green_700),
    onPrimary = colorResource(MR.colors.white),
    primaryContainer = colorResource(MR.colors.green_200),
    onPrimaryContainer = colorResource(MR.colors.green_900),
    secondary = colorResource(MR.colors.sage_600),
    onSecondary = colorResource(MR.colors.white),
    secondaryContainer = colorResource(MR.colors.sage_200),
    onSecondaryContainer = colorResource(MR.colors.sage_900),
    tertiary = colorResource(MR.colors.teal_600),
    onTertiary = colorResource(MR.colors.white),
    tertiaryContainer = colorResource(MR.colors.teal_200),
    onTertiaryContainer = colorResource(MR.colors.teal_900),
    background = colorResource(MR.colors.neutral_50),
    onBackground = colorResource(MR.colors.neutral_900),
    surface = colorResource(MR.colors.neutral_50),
    onSurface = colorResource(MR.colors.neutral_900),
    surfaceVariant = colorResource(MR.colors.sage_100),
    onSurfaceVariant = colorResource(MR.colors.neutral_700),
    error = colorResource(MR.colors.red_600),
    onError = colorResource(MR.colors.white),
    errorContainer = colorResource(MR.colors.red_100),
    onErrorContainer = colorResource(MR.colors.red_900),
    outline = colorResource(MR.colors.sage_500),
    outlineVariant = colorResource(MR.colors.neutral_300),
)

@Composable
internal fun domatDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = colorResource(MR.colors.green_300),
    onPrimary = colorResource(MR.colors.green_800),
    primaryContainer = colorResource(MR.colors.green_700),
    onPrimaryContainer = colorResource(MR.colors.green_200),
    secondary = colorResource(MR.colors.sage_300),
    onSecondary = colorResource(MR.colors.sage_800),
    secondaryContainer = colorResource(MR.colors.sage_700),
    onSecondaryContainer = colorResource(MR.colors.sage_200),
    tertiary = colorResource(MR.colors.teal_300),
    onTertiary = colorResource(MR.colors.teal_800),
    tertiaryContainer = colorResource(MR.colors.teal_700),
    onTertiaryContainer = colorResource(MR.colors.teal_200),
    background = colorResource(MR.colors.neutral_900),
    onBackground = colorResource(MR.colors.neutral_100),
    surface = colorResource(MR.colors.neutral_900),
    onSurface = colorResource(MR.colors.neutral_100),
    surfaceVariant = colorResource(MR.colors.neutral_700),
    onSurfaceVariant = colorResource(MR.colors.neutral_300),
    error = colorResource(MR.colors.red_200),
    onError = colorResource(MR.colors.red_800),
    errorContainer = colorResource(MR.colors.red_700),
    onErrorContainer = colorResource(MR.colors.red_100),
    outline = colorResource(MR.colors.sage_400),
    outlineVariant = colorResource(MR.colors.neutral_700),
)
