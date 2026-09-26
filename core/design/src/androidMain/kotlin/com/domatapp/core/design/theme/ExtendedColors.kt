package com.domatapp.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic roles Material 3 does not have (success, warning). Values mirror the
 * `success*` / `warning*` tokens in design/tokens/DESIGN.md — change the token file first.
 *
 * Access: `MaterialTheme.domatColors.successContainer`
 */
@Immutable
data class DomatExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

internal fun domatLightExtendedColors() = DomatExtendedColors(
    success = Color(0xFF047857),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD1FAE5),
    onSuccessContainer = Color(0xFF064E3B),
    warningContainer = Color(0xFFFFEDD5),
    onWarningContainer = Color(0xFF9A3412),
)

internal fun domatDarkExtendedColors() = DomatExtendedColors(
    success = Color(0xFF34D399),
    onSuccess = Color(0xFF064E3B),
    successContainer = Color(0xFF064E3B),
    onSuccessContainer = Color(0xFFD1FAE5),
    warningContainer = Color(0xFF7C2D12),
    onWarningContainer = Color(0xFFFFEDD5),
)

internal val LocalExtendedColors = staticCompositionLocalOf { domatLightExtendedColors() }

val MaterialTheme.domatColors: DomatExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
