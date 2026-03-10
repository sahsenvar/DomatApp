package com.domatapp.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.spacing.DomatSpacing

data class Spacing(
    val xxs: Dp = DomatSpacing.xxs.dp,
    val xs: Dp = DomatSpacing.xs.dp,
    val sm: Dp = DomatSpacing.sm.dp,
    val md: Dp = DomatSpacing.md.dp,
    val lg: Dp = DomatSpacing.lg.dp,
    val xl: Dp = DomatSpacing.xl.dp,
    val xxl: Dp = DomatSpacing.xxl.dp,
)

internal val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
