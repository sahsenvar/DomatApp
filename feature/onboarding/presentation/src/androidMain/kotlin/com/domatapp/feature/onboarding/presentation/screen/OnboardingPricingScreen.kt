package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.MR
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

enum class SupplyChainRowVariant { Producer, Inactive, Consumer }

data class SupplyChainRowUiModel(
    val icon: ImageResource,
    val variant: SupplyChainRowVariant,
    val title: String,
    val subtitle: String,
    val showConnector: Boolean = true,
)

@Composable
internal fun OnboardingPricingPageContent(modifier: Modifier = Modifier) {
    val surfaceDefault = colorResource(DomatColors.surfaceDefault)

    val rows = listOf(
        SupplyChainRowUiModel(
            icon = MR.images.ic_pricing_producer,
            variant = SupplyChainRowVariant.Producer,
            title = stringResource(MR.strings.onboarding_pricing_producer_title),
            subtitle = stringResource(MR.strings.onboarding_pricing_producer_subtitle),
        ),
        SupplyChainRowUiModel(
            icon = MR.images.ic_pricing_wholesaler,
            variant = SupplyChainRowVariant.Inactive,
            title = stringResource(MR.strings.onboarding_pricing_wholesaler_title),
            subtitle = stringResource(MR.strings.onboarding_pricing_wholesaler_subtitle),
        ),
        SupplyChainRowUiModel(
            icon = MR.images.ic_pricing_retail,
            variant = SupplyChainRowVariant.Inactive,
            title = stringResource(MR.strings.onboarding_pricing_retail_title),
            subtitle = stringResource(MR.strings.onboarding_pricing_retail_subtitle),
        ),
        SupplyChainRowUiModel(
            icon = MR.images.ic_pricing_consumer,
            variant = SupplyChainRowVariant.Consumer,
            title = stringResource(MR.strings.onboarding_pricing_consumer_title),
            subtitle = stringResource(MR.strings.onboarding_pricing_consumer_subtitle),
            showConnector = false,
        ),
    )

    Column(
        modifier = modifier
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
                    text = stringResource(MR.strings.onboarding_pricing_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = colorResource(DomatColors.textPrimary),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(MR.strings.onboarding_pricing_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(DomatColors.textSecondary),
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 3.dp),
            ) {
                rows.forEach { row -> SupplyChainRow(uiModel = row) }
            }
        }
    }
}

@Composable
private fun SupplyChainRow(uiModel: SupplyChainRowUiModel) {
    val primary = colorResource(DomatColors.primary)
    val primary20 = colorResource(DomatColors.primary20)
    val primary30 = colorResource(DomatColors.primary30)
    val textPrimary = colorResource(DomatColors.textPrimary)
    val textTertiary = colorResource(DomatColors.textTertiary)
    val textMuted = colorResource(DomatColors.textMuted)
    val borderDefault = colorResource(DomatColors.borderDefault)
    val borderLight = colorResource(DomatColors.borderLight)
    val error = colorResource(DomatColors.error)

    val isInactive = uiModel.variant == SupplyChainRowVariant.Inactive
    val isConsumer = uiModel.variant == SupplyChainRowVariant.Consumer

    val iconSize = when (uiModel.variant) {
        SupplyChainRowVariant.Producer -> 22.5f
        SupplyChainRowVariant.Inactive -> 25f
        SupplyChainRowVariant.Consumer -> 20f
    }
    val iconBgColor = when (uiModel.variant) {
        SupplyChainRowVariant.Producer -> primary20
        SupplyChainRowVariant.Inactive -> borderDefault
        SupplyChainRowVariant.Consumer -> primary
    }
    val titleColor = if (isInactive) textTertiary else textPrimary
    val titleFontWeight = if (isInactive) FontWeight.Medium else FontWeight.Bold
    val subtitleColor = if (isInactive) textMuted else primary
    val subtitleFontWeight = if (isConsumer) FontWeight.SemiBold else FontWeight.Normal
    val dividerColor = when (uiModel.variant) {
        SupplyChainRowVariant.Producer -> primary30
        SupplyChainRowVariant.Inactive -> borderDefault
        SupplyChainRowVariant.Consumer -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isInactive) 0.4f else 1f),
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
                        if (isConsumer) Modifier.shadow(
                            10.dp, CircleShape,
                            ambientColor = primary30,
                            spotColor = primary30,
                        ) else Modifier,
                    )
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(uiModel.icon),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize.dp),
                )
                if (isInactive) {
                    Text(
                        text = "✕",
                        color = error,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (uiModel.showConnector) {
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
                text = uiModel.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = titleFontWeight,
                    textDecoration = if (isInactive) TextDecoration.LineThrough else TextDecoration.None,
                ),
                color = titleColor,
            )
            Text(
                text = uiModel.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = subtitleFontWeight),
                color = subtitleColor,
            )
        }
    }

    if (uiModel.showConnector) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 64.dp),
            color = borderLight,
            thickness = 1.dp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingPricingPageContentPreview() {
    DomatTheme {
        OnboardingPricingPageContent()
    }
}
