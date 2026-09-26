package com.domatapp.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.domatapp.core.design.typography.DomatTypographyScale
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.nunito_sans_bold
import com.domatapp.core.resource.generated.resources.nunito_sans_extrabold
import com.domatapp.core.resource.generated.resources.nunito_sans_italic
import com.domatapp.core.resource.generated.resources.nunito_sans_medium
import com.domatapp.core.resource.generated.resources.nunito_sans_regular
import com.domatapp.core.resource.generated.resources.nunito_sans_semibold
import org.jetbrains.compose.resources.Font

// @Composable because Compose Resources' Font() loads the font through the resource reader
// rather than resolving an Android font resource id. Called from DomatTheme, which is itself
// composable, so this costs nothing at the call site.
@Composable
internal fun domatTypography(): Typography {
    // One static file per weight (instanced from the Google Fonts variable font at wdth=100,
    // opsz=12, YTLC=500). Static files instead of the variable font: a variable file loaded
    // without variation settings renders its default instance (wght=200) and the heavier
    // weights end up synthesized, which does not match Figma. Source of truth: design/tokens/DESIGN.md.
    val nunito = FontFamily(
        Font(Res.font.nunito_sans_regular, FontWeight.Normal),
        Font(Res.font.nunito_sans_medium, FontWeight.Medium),
        Font(Res.font.nunito_sans_semibold, FontWeight.SemiBold),
        Font(Res.font.nunito_sans_bold, FontWeight.Bold),
        Font(Res.font.nunito_sans_extrabold, FontWeight.ExtraBold),
        Font(Res.font.nunito_sans_italic, FontWeight.Normal, FontStyle.Italic),
    )

    return Typography(
        displayLarge = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.DisplayLargeSize.sp,
            lineHeight = DomatTypographyScale.DisplayLargeLineHeight.sp,
            fontWeight = FontWeight.ExtraBold,
        ),
        displayMedium = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.DisplayMediumSize.sp,
            lineHeight = DomatTypographyScale.DisplayMediumLineHeight.sp,
            fontWeight = FontWeight.ExtraBold,
        ),
        displaySmall = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.DisplaySmallSize.sp,
            lineHeight = DomatTypographyScale.DisplaySmallLineHeight.sp,
            fontWeight = FontWeight.Bold,
        ),
        headlineLarge = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.HeadlineLargeSize.sp,
            lineHeight = DomatTypographyScale.HeadlineLargeLineHeight.sp,
            fontWeight = FontWeight.ExtraBold,
        ),
        headlineMedium = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.HeadlineMediumSize.sp,
            lineHeight = DomatTypographyScale.HeadlineMediumLineHeight.sp,
            fontWeight = FontWeight.Bold,
        ),
        headlineSmall = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.HeadlineSmallSize.sp,
            lineHeight = DomatTypographyScale.HeadlineSmallLineHeight.sp,
            fontWeight = FontWeight.Bold,
        ),
        titleLarge = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.TitleLargeSize.sp,
            lineHeight = DomatTypographyScale.TitleLargeLineHeight.sp,
            fontWeight = FontWeight.Bold,
        ),
        titleMedium = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.TitleMediumSize.sp,
            lineHeight = DomatTypographyScale.TitleMediumLineHeight.sp,
            fontWeight = FontWeight.Medium,
        ),
        titleSmall = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.TitleSmallSize.sp,
            lineHeight = DomatTypographyScale.TitleSmallLineHeight.sp,
            fontWeight = FontWeight.Medium,
        ),
        bodyLarge = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.BodyLargeSize.sp,
            lineHeight = DomatTypographyScale.BodyLargeLineHeight.sp,
            fontWeight = FontWeight.Normal,
        ),
        bodyMedium = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.BodyMediumSize.sp,
            lineHeight = DomatTypographyScale.BodyMediumLineHeight.sp,
            fontWeight = FontWeight.Normal,
        ),
        bodySmall = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.BodySmallSize.sp,
            lineHeight = DomatTypographyScale.BodySmallLineHeight.sp,
            fontWeight = FontWeight.Normal,
        ),
        labelLarge = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.LabelLargeSize.sp,
            lineHeight = DomatTypographyScale.LabelLargeLineHeight.sp,
            fontWeight = FontWeight.Bold,
        ),
        labelMedium = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.LabelMediumSize.sp,
            lineHeight = DomatTypographyScale.LabelMediumLineHeight.sp,
            fontWeight = FontWeight.Bold,
        ),
        labelSmall = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.LabelSmallSize.sp,
            lineHeight = DomatTypographyScale.LabelSmallLineHeight.sp,
            fontWeight = FontWeight.Bold,
        ),
    )
}
