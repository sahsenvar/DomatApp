package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.core.presentation.component.button.DomatPrimaryButton
import com.domatapp.core.presentation.component.indicator.DomatProgressDots
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.feature.onboarding.presentation.model.pricing.OnboardingPricingEffect
import com.domatapp.feature.onboarding.presentation.model.pricing.OnboardingPricingIntent
import com.domatapp.feature.onboarding.presentation.model.pricing.OnboardingPricingUiState
import dev.icerock.moko.resources.compose.colorResource
import domatapp.feature.onboarding.presentation.generated.resources.Res
import domatapp.feature.onboarding.presentation.generated.resources.ic_pricing_consumer
import domatapp.feature.onboarding.presentation.generated.resources.ic_pricing_producer
import domatapp.feature.onboarding.presentation.generated.resources.ic_pricing_retail
import domatapp.feature.onboarding.presentation.generated.resources.ic_pricing_wholesaler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@NavigationScreen(Route.OnboardingRoute.Pricing::class)
@Composable
fun ColumnScope.OnboardingPricingScreen(
    uiState: OnboardingPricingUiState,
    onIntent: (OnboardingPricingIntent) -> Unit,
) {
    val primary = colorResource(DomatColors.primary)
    val primary20 = colorResource(DomatColors.primary20)
    val primary30 = colorResource(DomatColors.primary30)
    val textPrimary = colorResource(DomatColors.textPrimary)
    val textSecondary = colorResource(DomatColors.textSecondary)
    val textTertiary = colorResource(DomatColors.textTertiary)
    val textMuted = colorResource(DomatColors.textMuted)
    val borderDefault = colorResource(DomatColors.borderDefault)
    val borderLight = colorResource(DomatColors.borderLight)
    val surfaceDefault = colorResource(DomatColors.surfaceDefault)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceDefault),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Neden bu kadar\nuygun fiyatlı?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = textPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Çünkü arada aracı yok. Domatesler\n" +
                        "doğrudan üreticiden size geliyor. Toptancı\n" +
                        "yok, depo yok, dükkan kirası yok, gereksiz\n" +
                        "maliyetler yok.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 3.dp),
            ) {
                SupplyChainRow(
                    icon = Res.drawable.ic_pricing_producer,
                    iconSize = 22.5f,
                    iconBgColor = primary20,
                    title = "Üretici",
                    titleColor = textPrimary,
                    titleFontWeight = FontWeight.Bold,
                    subtitle = "Doğrudan Kaynak",
                    subtitleColor = primary,
                    showDivider = true,
                    dividerColor = primary30,
                    showBottomBorder = true,
                    borderLightColor = borderLight,
                )
                SupplyChainRow(
                    icon = Res.drawable.ic_pricing_wholesaler,
                    iconSize = 25f,
                    iconBgColor = borderDefault,
                    title = "Toptancı",
                    titleColor = textTertiary,
                    titleFontWeight = FontWeight.Medium,
                    titleStrikethrough = true,
                    subtitle = "Kâr Marjı + Depolama",
                    subtitleColor = textMuted,
                    showCrossOut = true,
                    showDivider = true,
                    dividerColor = borderDefault,
                    showBottomBorder = true,
                    rowAlpha = 0.4f,
                    borderLightColor = borderLight,
                )
                SupplyChainRow(
                    icon = Res.drawable.ic_pricing_retail,
                    iconSize = 25f,
                    iconBgColor = borderDefault,
                    title = "Perakende Mağaza",
                    titleColor = textTertiary,
                    titleFontWeight = FontWeight.Medium,
                    titleStrikethrough = true,
                    subtitle = "Kira + Personel",
                    subtitleColor = textMuted,
                    showCrossOut = true,
                    showDivider = true,
                    dividerColor = borderDefault,
                    showBottomBorder = true,
                    rowAlpha = 0.4f,
                    borderLightColor = borderLight,
                )
                SupplyChainRow(
                    icon = Res.drawable.ic_pricing_consumer,
                    iconSize = 20f,
                    iconBgColor = primary,
                    iconShadow = true,
                    shadowColor = primary30,
                    title = "Siz",
                    titleColor = textPrimary,
                    titleFontWeight = FontWeight.Bold,
                    subtitle = "Ortalama %40 tasarruf edin",
                    subtitleColor = primary,
                    subtitleFontWeight = FontWeight.SemiBold,
                    showDivider = false,
                    showBottomBorder = false,
                    borderLightColor = borderLight,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            DomatProgressDots(totalDots = 3, activeIndex = 1)
            DomatPrimaryButton(
                text = "Hmm.. Güzelmiş. Başka?",
                onClick = { onIntent(OnboardingPricingIntent.GoNext) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SupplyChainRow(
    icon: DrawableResource,
    iconSize: Float,
    iconBgColor: Color,
    title: String,
    titleColor: Color,
    titleFontWeight: FontWeight,
    subtitle: String,
    subtitleColor: Color,
    showDivider: Boolean,
    showBottomBorder: Boolean,
    borderLightColor: Color,
    modifier: Modifier = Modifier,
    iconShadow: Boolean = false,
    shadowColor: Color = Color.Transparent,
    titleStrikethrough: Boolean = false,
    subtitleFontWeight: FontWeight = FontWeight.Normal,
    showCrossOut: Boolean = false,
    dividerColor: Color = Color.Transparent,
    rowAlpha: Float = 1f,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(rowAlpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.width(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (iconShadow) Modifier.shadow(
                            10.dp,
                            CircleShape,
                            ambientColor = shadowColor,
                            spotColor = shadowColor,
                        ) else Modifier,
                    )
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize.dp),
                )
                if (showCrossOut) {
                    Text(
                        text = "✕",
                        color = colorResource(DomatColors.error),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (showDivider) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(dividerColor),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = titleFontWeight,
                    textDecoration = if (titleStrikethrough) TextDecoration.LineThrough else TextDecoration.None,
                ),
                color = titleColor,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = subtitleFontWeight),
                color = subtitleColor,
            )
        }
    }

    if (showBottomBorder) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 64.dp),
            color = borderLightColor,
            thickness = 1.dp,
        )
    }
}

@NavigationEffectHandler(Route.OnboardingRoute.Pricing::class)
@Composable
fun OnboardingPricingEffectHandler(effectFlow: Flow<OnboardingPricingEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                OnboardingPricingEffect.NavigateToCommunity ->
                    navigator.navigate(Route.OnboardingRoute.Community)
                OnboardingPricingEffect.NavigateBack ->
                    navigator.popBack()
            }
        }
    }
}
