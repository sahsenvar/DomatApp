package com.domatapp.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.domatapp.core.design.typography.DomatTypographyScale
import com.domatapp.core.resource.MR
import dev.icerock.moko.resources.compose.fontFamilyResource

@Composable
internal fun domatTypography(): Typography {
    val nunito = fontFamilyResource(MR.fonts.nunito_sans_regular)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.DisplayLargeSize.sp,
            lineHeight = DomatTypographyScale.DisplayLargeLineHeight.sp,
            fontWeight = FontWeight.Normal,
        ),
        displayMedium = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.DisplayMediumSize.sp,
            lineHeight = DomatTypographyScale.DisplayMediumLineHeight.sp,
            fontWeight = FontWeight.Normal,
        ),
        displaySmall = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.DisplaySmallSize.sp,
            lineHeight = DomatTypographyScale.DisplaySmallLineHeight.sp,
            fontWeight = FontWeight.Normal,
        ),
        headlineLarge = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.HeadlineLargeSize.sp,
            lineHeight = DomatTypographyScale.HeadlineLargeLineHeight.sp,
            fontWeight = FontWeight.Normal,
        ),
        headlineMedium = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.HeadlineMediumSize.sp,
            lineHeight = DomatTypographyScale.HeadlineMediumLineHeight.sp,
            fontWeight = FontWeight.Normal,
        ),
        headlineSmall = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.HeadlineSmallSize.sp,
            lineHeight = DomatTypographyScale.HeadlineSmallLineHeight.sp,
            fontWeight = FontWeight.Normal,
        ),
        titleLarge = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.TitleLargeSize.sp,
            lineHeight = DomatTypographyScale.TitleLargeLineHeight.sp,
            fontWeight = FontWeight.Medium,
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
            fontWeight = FontWeight.Medium,
        ),
        labelMedium = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.LabelMediumSize.sp,
            lineHeight = DomatTypographyScale.LabelMediumLineHeight.sp,
            fontWeight = FontWeight.Medium,
        ),
        labelSmall = TextStyle(
            fontFamily = nunito,
            fontSize = DomatTypographyScale.LabelSmallSize.sp,
            lineHeight = DomatTypographyScale.LabelSmallLineHeight.sp,
            fontWeight = FontWeight.Medium,
        ),
    )
}
