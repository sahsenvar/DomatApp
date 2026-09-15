package com.domatapp.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.elevation.DomatElevation

data class Elevation(
    val none: Dp = DomatElevation.none.dp,
    val xs: Dp = DomatElevation.xs.dp,
    val sm: Dp = DomatElevation.sm.dp,
    val md: Dp = DomatElevation.md.dp,
    val lg: Dp = DomatElevation.lg.dp,
    val xl: Dp = DomatElevation.xl.dp,
)

internal val LocalElevation = staticCompositionLocalOf { Elevation() }

val MaterialTheme.elevation: Elevation
    @Composable
    @ReadOnlyComposable
    get() = LocalElevation.current
