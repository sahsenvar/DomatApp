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
import androidx.compose.ui.Modifier
import com.domatapp.core.design.theme.DomatColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.navigation.OnboardingGraph
import dev.gezgin.core.annotation.Screen
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.onboarding_btn_community
import com.domatapp.core.resource.generated.resources.onboarding_btn_effortless
import com.domatapp.core.resource.generated.resources.onboarding_btn_pricing
import com.domatapp.core.resource.generated.resources.onboarding_btn_trust
import com.domatapp.core.resource.generated.resources.onboarding_btn_welcome
import com.domatapp.feature.onboarding.presentation.screen.community.OnboardingCommunityPageContent
import com.domatapp.feature.onboarding.presentation.screen.effortless.OnboardingEffortlessPageContent
import com.domatapp.feature.onboarding.presentation.screen.pricing.OnboardingPricingPageContent
import com.domatapp.feature.onboarding.presentation.screen.trust.OnboardingTrustPageContent
import com.domatapp.feature.onboarding.presentation.ui.OnboardingBottomBar
import com.domatapp.feature.onboarding.presentation.ui.OnboardingBottomBarUiModel
import com.domatapp.feature.onboarding.presentation.ui.OnboardingWelcomePageContent
import com.domatapp.feature.onboarding.presentation.welcome.OnboardingPage
import com.domatapp.feature.onboarding.presentation.welcome.OnboardingWelcomeIntent
import com.domatapp.feature.onboarding.presentation.welcome.OnboardingWelcomeUiState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource

@Screen(OnboardingGraph.OnboardingWelcomeRoute::class)
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
            .background(DomatColors.White),
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
            OnboardingPage.WELCOME -> stringResource(Res.string.onboarding_btn_welcome)
            OnboardingPage.PRICING -> stringResource(Res.string.onboarding_btn_pricing)
            OnboardingPage.COMMUNITY -> stringResource(Res.string.onboarding_btn_community)
            OnboardingPage.TRUST -> stringResource(Res.string.onboarding_btn_trust)
            OnboardingPage.EFFORTLESS -> stringResource(Res.string.onboarding_btn_effortless)
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

@Preview
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
