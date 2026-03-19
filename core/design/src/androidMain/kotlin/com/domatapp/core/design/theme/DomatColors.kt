package com.domatapp.core.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.domatapp.core.resource.MR
import dev.icerock.moko.resources.compose.colorResource

object DomatColors {
    // Primary
    val Primary: Color @Composable get() = colorResource(MR.colors.primary_default)
    val PrimaryDark: Color @Composable get() = colorResource(MR.colors.primary_dark)
    val Primary20: Color @Composable get() = colorResource(MR.colors.primary_20)
    val Primary10: Color @Composable get() = colorResource(MR.colors.primary_10)
    val Primary5: Color @Composable get() = colorResource(MR.colors.primary_5)

    // Text
    val TextPrimary: Color @Composable get() = colorResource(MR.colors.text_primary)
    val TextSecondary: Color @Composable get() = colorResource(MR.colors.text_secondary)
    val TextTertiary: Color @Composable get() = colorResource(MR.colors.text_tertiary)
    val TextMuted: Color @Composable get() = colorResource(MR.colors.text_muted)
    val TextDisabled: Color @Composable get() = colorResource(MR.colors.text_disabled)
    val TextInverse: Color @Composable get() = colorResource(MR.colors.text_inverse)

    // Surface
    val SurfaceDefault: Color @Composable get() = colorResource(MR.colors.surface_default)
    val SurfaceSubtle: Color @Composable get() = colorResource(MR.colors.surface_subtle)
    val SurfaceBase: Color @Composable get() = colorResource(MR.colors.surface_base)
    val SurfaceMuted: Color @Composable get() = colorResource(MR.colors.surface_muted)
    val SurfaceDark: Color @Composable get() = colorResource(MR.colors.surface_dark)

    // Border
    val BorderDefault: Color @Composable get() = colorResource(MR.colors.border_default)
    val BorderLight: Color @Composable get() = colorResource(MR.colors.border_light)
    val BorderMedium: Color @Composable get() = colorResource(MR.colors.border_medium)
    val BorderStrong: Color @Composable get() = colorResource(MR.colors.border_strong)

    // Semantic
    val Error: Color @Composable get() = colorResource(MR.colors.semantic_error)
    val Success: Color @Composable get() = colorResource(MR.colors.semantic_success)
    val SuccessLight: Color @Composable get() = colorResource(MR.colors.semantic_success_light)
    val Warning: Color @Composable get() = colorResource(MR.colors.semantic_warning)
    val Info: Color @Composable get() = colorResource(MR.colors.semantic_info)
    val InfoLight: Color @Composable get() = colorResource(MR.colors.semantic_info_light)

    // Overlay
    val OverlayWhite80: Color @Composable get() = colorResource(MR.colors.overlay_white_80)
    val OverlayWhite90: Color @Composable get() = colorResource(MR.colors.overlay_white_90)
    val OverlayBlack: Color @Composable get() = colorResource(MR.colors.overlay_black)
    val OverlayDark: Color @Composable get() = colorResource(MR.colors.overlay_dark)
    val OverlayBlack55: Color @Composable get() = colorResource(MR.colors.overlay_black_55)

    // Hero
    val HeroGradientStart: Color @Composable get() = colorResource(MR.colors.hero_gradient_start)
    val HeroGradientEnd: Color @Composable get() = colorResource(MR.colors.hero_gradient_end)
}
