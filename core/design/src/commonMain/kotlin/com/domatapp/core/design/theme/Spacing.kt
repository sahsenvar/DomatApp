package com.domatapp.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.spacing.DomatSpacing

data class Spacing(
    val sp1: Dp = DomatSpacing.sp1.dp,
    val sp2: Dp = DomatSpacing.sp2.dp,
    val sp3: Dp = DomatSpacing.sp3.dp,
    val sp4: Dp = DomatSpacing.sp4.dp,
    val sp5: Dp = DomatSpacing.sp5.dp,
    val sp6: Dp = DomatSpacing.sp6.dp,
    val sp8: Dp = DomatSpacing.sp8.dp,
    val sp10: Dp = DomatSpacing.sp10.dp,
    val sp12: Dp = DomatSpacing.sp12.dp,
    val sp16: Dp = DomatSpacing.sp16.dp,
    val sp20: Dp = DomatSpacing.sp20.dp,
)

internal val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
