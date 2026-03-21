package com.domatapp.feature.onboarding.presentation.screen.pricing

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R
import com.domatapp.feature.onboarding.presentation.ui.SupplyChainRow
import com.domatapp.feature.onboarding.presentation.ui.SupplyChainRowUiModel
import com.domatapp.feature.onboarding.presentation.ui.SupplyChainRowVariant

@Composable
internal fun OnboardingPricingPageContent(modifier: Modifier = Modifier) {
    val rows = listOf(
        SupplyChainRowUiModel(
            icon = R.drawable.ic_pricing_producer,
            variant = SupplyChainRowVariant.Producer,
            title = stringResource(R.string.onboarding_pricing_producer_title),
            subtitle = stringResource(R.string.onboarding_pricing_producer_subtitle),
        ),
        SupplyChainRowUiModel(
            icon = R.drawable.ic_pricing_wholesaler,
            variant = SupplyChainRowVariant.Inactive,
            title = stringResource(R.string.onboarding_pricing_wholesaler_title),
            subtitle = stringResource(R.string.onboarding_pricing_wholesaler_subtitle),
        ),
        SupplyChainRowUiModel(
            icon = R.drawable.ic_pricing_retail,
            variant = SupplyChainRowVariant.Inactive,
            title = stringResource(R.string.onboarding_pricing_retail_title),
            subtitle = stringResource(R.string.onboarding_pricing_retail_subtitle),
        ),
        SupplyChainRowUiModel(
            icon = R.drawable.ic_pricing_consumer,
            variant = SupplyChainRowVariant.Consumer,
            title = stringResource(R.string.onboarding_pricing_consumer_title),
            subtitle = stringResource(R.string.onboarding_pricing_consumer_subtitle),
            showConnector = false,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.white)),
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
                    text = stringResource(R.string.onboarding_pricing_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = colorResource(R.color.slate_900),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.onboarding_pricing_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.slate_600),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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

@Preview(showBackground = true)
@Composable
private fun OnboardingPricingPageContentPreview() {
    DomatTheme {
        OnboardingPricingPageContent()
    }
}
