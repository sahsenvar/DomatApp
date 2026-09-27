package com.domatapp.feature.onboarding.presentation.screen.pricing

import com.domatapp.core.design.theme.spacing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.ic_person
import com.domatapp.core.resource.generated.resources.ic_potted_plant
import com.domatapp.core.resource.generated.resources.ic_storefront
import com.domatapp.core.resource.generated.resources.ic_warehouse
import com.domatapp.core.resource.generated.resources.onboarding_pricing_body
import com.domatapp.core.resource.generated.resources.onboarding_pricing_consumer_subtitle
import com.domatapp.core.resource.generated.resources.onboarding_pricing_consumer_title
import com.domatapp.core.resource.generated.resources.onboarding_pricing_producer_subtitle
import com.domatapp.core.resource.generated.resources.onboarding_pricing_producer_title
import com.domatapp.core.resource.generated.resources.onboarding_pricing_retail_subtitle
import com.domatapp.core.resource.generated.resources.onboarding_pricing_retail_title
import com.domatapp.core.resource.generated.resources.onboarding_pricing_title
import com.domatapp.core.resource.generated.resources.onboarding_pricing_wholesaler_subtitle
import com.domatapp.core.resource.generated.resources.onboarding_pricing_wholesaler_title
import com.domatapp.feature.onboarding.presentation.ui.SupplyChainRow
import com.domatapp.feature.onboarding.presentation.ui.SupplyChainRowUiModel
import com.domatapp.feature.onboarding.presentation.ui.SupplyChainRowVariant
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingPricingPageContent(modifier: Modifier = Modifier) {
    val rows = listOf(
        SupplyChainRowUiModel(
            icon = Res.drawable.ic_potted_plant,
            variant = SupplyChainRowVariant.Producer,
            title = stringResource(Res.string.onboarding_pricing_producer_title),
            subtitle = stringResource(Res.string.onboarding_pricing_producer_subtitle),
        ),
        SupplyChainRowUiModel(
            icon = Res.drawable.ic_warehouse,
            variant = SupplyChainRowVariant.Inactive,
            title = stringResource(Res.string.onboarding_pricing_wholesaler_title),
            subtitle = stringResource(Res.string.onboarding_pricing_wholesaler_subtitle),
        ),
        SupplyChainRowUiModel(
            icon = Res.drawable.ic_storefront,
            variant = SupplyChainRowVariant.Inactive,
            title = stringResource(Res.string.onboarding_pricing_retail_title),
            subtitle = stringResource(Res.string.onboarding_pricing_retail_subtitle),
        ),
        SupplyChainRowUiModel(
            icon = Res.drawable.ic_person,
            variant = SupplyChainRowVariant.Consumer,
            title = stringResource(Res.string.onboarding_pricing_consumer_title),
            subtitle = stringResource(Res.string.onboarding_pricing_consumer_subtitle),
            showConnector = false,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.sp8),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.sp12, bottom = MaterialTheme.spacing.sp10),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp4),
            ) {
                Text(
                    text = stringResource(Res.string.onboarding_pricing_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.onboarding_pricing_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.sp1),
            ) {
                rows.forEach { row -> SupplyChainRow(uiModel = row) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingPricingPageContentPreview() {
    DomatTheme {
        OnboardingPricingPageContent()
    }
}
