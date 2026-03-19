package com.domatapp.core.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.domatapp.core.resource.MR
import dev.icerock.moko.resources.compose.colorResource

@Composable
internal fun domatLightColorScheme(): ColorScheme = lightColorScheme(
    primary = colorResource(MR.colors.primary_default),
    onPrimary = colorResource(MR.colors.text_primary),
    primaryContainer = colorResource(MR.colors.primary_20),
    onPrimaryContainer = colorResource(MR.colors.primary_dark),
    secondary = colorResource(MR.colors.text_secondary),
    onSecondary = colorResource(MR.colors.text_inverse),
    secondaryContainer = colorResource(MR.colors.surface_muted),
    onSecondaryContainer = colorResource(MR.colors.text_primary),
    tertiary = colorResource(MR.colors.semantic_info),
    onTertiary = colorResource(MR.colors.text_inverse),
    tertiaryContainer = colorResource(MR.colors.semantic_info_light),
    onTertiaryContainer = colorResource(MR.colors.semantic_info),
    background = colorResource(MR.colors.surface_default),
    onBackground = colorResource(MR.colors.text_primary),
    surface = colorResource(MR.colors.surface_default),
    onSurface = colorResource(MR.colors.text_primary),
    surfaceVariant = colorResource(MR.colors.surface_subtle),
    onSurfaceVariant = colorResource(MR.colors.text_secondary),
    error = colorResource(MR.colors.semantic_error),
    onError = colorResource(MR.colors.text_inverse),
    errorContainer = colorResource(MR.colors.error_container_light),
    onErrorContainer = colorResource(MR.colors.error_container_on_light),
    outline = colorResource(MR.colors.border_medium),
    outlineVariant = colorResource(MR.colors.border_default),
)

@Composable
internal fun domatDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = colorResource(MR.colors.primary_default),
    onPrimary = colorResource(MR.colors.primary_dark),
    primaryContainer = colorResource(MR.colors.primary_dark),
    onPrimaryContainer = colorResource(MR.colors.primary_default),
    secondary = colorResource(MR.colors.text_muted),
    onSecondary = colorResource(MR.colors.surface_dark),
    secondaryContainer = colorResource(MR.colors.surface_dark_variant),
    onSecondaryContainer = colorResource(MR.colors.text_inverse),
    tertiary = colorResource(MR.colors.semantic_info_light),
    onTertiary = colorResource(MR.colors.semantic_info),
    tertiaryContainer = colorResource(MR.colors.semantic_info),
    onTertiaryContainer = colorResource(MR.colors.semantic_info_light),
    background = colorResource(MR.colors.surface_dark),
    onBackground = colorResource(MR.colors.text_inverse),
    surface = colorResource(MR.colors.surface_dark),
    onSurface = colorResource(MR.colors.text_inverse),
    surfaceVariant = colorResource(MR.colors.surface_dark_variant),
    onSurfaceVariant = colorResource(MR.colors.text_muted),
    error = colorResource(MR.colors.semantic_error),
    onError = colorResource(MR.colors.text_inverse),
    errorContainer = colorResource(MR.colors.error_container_dark),
    onErrorContainer = colorResource(MR.colors.error_container_light),
    outline = colorResource(MR.colors.border_strong),
    outlineVariant = colorResource(MR.colors.surface_dark_outline),
)
