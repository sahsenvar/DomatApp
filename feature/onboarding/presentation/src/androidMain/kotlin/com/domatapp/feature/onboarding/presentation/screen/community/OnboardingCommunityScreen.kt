package com.domatapp.feature.onboarding.presentation.screen.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R
import com.domatapp.feature.onboarding.presentation.ui.CommunityHeroCard
import com.domatapp.feature.onboarding.presentation.ui.CommunityHeroCardUiModel

@Composable
internal fun OnboardingCommunityPageContent(modifier: Modifier = Modifier) {
    val heroCard = CommunityHeroCardUiModel(
        currentPrice = stringResource(R.string.onboarding_community_price_current),
        originalPrice = stringResource(R.string.onboarding_community_price_original),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.white)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CommunityHeroCard(uiModel = heroCard)

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.onboarding_community_title_line1))
                        append("\n")
                        withStyle(SpanStyle(color = colorResource(R.color.malachite))) {
                            append(stringResource(R.string.onboarding_community_title_highlight))
                        }
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    color = colorResource(R.color.slate_900),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(R.string.onboarding_community_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.slate_600),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingCommunityPageContentPreview() {
    DomatTheme {
        OnboardingCommunityPageContent()
    }
}
