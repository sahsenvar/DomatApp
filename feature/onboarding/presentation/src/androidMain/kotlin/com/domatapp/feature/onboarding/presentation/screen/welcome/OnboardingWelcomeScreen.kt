package com.domatapp.feature.onboarding.presentation.screen.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.core.resource.R
import com.domatapp.feature.onboarding.presentation.welcome.OnboardingPage
import com.domatapp.feature.onboarding.presentation.welcome.OnboardingWelcomeIntent
import com.domatapp.feature.onboarding.presentation.welcome.OnboardingWelcomeUiState
import com.domatapp.feature.onboarding.presentation.ui.OnboardingBottomBar
import com.domatapp.feature.onboarding.presentation.ui.OnboardingBottomBarUiModel
import com.domatapp.feature.onboarding.presentation.screen.pricing.OnboardingPricingPageContent
import com.domatapp.feature.onboarding.presentation.screen.community.OnboardingCommunityPageContent
import com.domatapp.feature.onboarding.presentation.screen.trust.OnboardingTrustPageContent
import com.domatapp.feature.onboarding.presentation.screen.effortless.OnboardingEffortlessPageContent
import com.domatapp.feature.onboarding.presentation.ui.OnboardingWelcomePageContent

@NavigationScreen(Route.OnboardingRoute.Welcome::class)
@Composable
fun ColumnScope.OnboardingWelcomeScreen(
    uiState: OnboardingWelcomeUiState,
    onIntent: (OnboardingWelcomeIntent) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 5 })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
            .filter { (isScrolling, _) -> !isScrolling }
            .map { (_, page) -> page }
            .distinctUntilChanged()
            .collect { page ->
                onIntent(OnboardingWelcomeIntent.OnPageChanged(page))
            }
    }

    LaunchedEffect(uiState.targetPage) {
        uiState.targetPage?.let { page ->
            pagerState.animateScrollToPage(page.index)
            onIntent(OnboardingWelcomeIntent.OnScrollConsumed)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.white)),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            userScrollEnabled = true,
        ) { page ->
            when (OnboardingPage.fromIndex(page)) {
                OnboardingPage.WELCOME -> OnboardingWelcomePageContent()
                OnboardingPage.PRICING -> OnboardingPricingPageContent()
                OnboardingPage.COMMUNITY -> OnboardingCommunityPageContent()
                OnboardingPage.TRUST -> OnboardingTrustPageContent()
                OnboardingPage.EFFORTLESS -> OnboardingEffortlessPageContent()
            }
        }

        val buttonText = when (uiState.currentPage) {
            OnboardingPage.WELCOME -> stringResource(R.string.onboarding_btn_welcome)
            OnboardingPage.PRICING -> stringResource(R.string.onboarding_btn_pricing)
            OnboardingPage.COMMUNITY -> stringResource(R.string.onboarding_btn_community)
            OnboardingPage.TRUST -> stringResource(R.string.onboarding_btn_trust)
            OnboardingPage.EFFORTLESS -> stringResource(R.string.onboarding_btn_effortless)
        }
        OnboardingBottomBar(
            uiModel = OnboardingBottomBarUiModel(
                buttonText = buttonText,
                totalDots = OnboardingPage.entries.size,
                activeDotIndex = uiState.currentPage.index,
            ),
            onContinue = { onIntent(OnboardingWelcomeIntent.OnContinueClicked) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingWelcomeScreenPreview() {
    DomatTheme {
        Column {
            OnboardingWelcomeScreen(
                uiState = OnboardingWelcomeUiState(),
                onIntent = {},
            )
        }
    }
}
