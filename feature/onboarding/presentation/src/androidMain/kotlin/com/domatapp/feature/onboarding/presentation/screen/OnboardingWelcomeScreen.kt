package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.core.resource.MR
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingPage
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeEffect
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeIntent
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeUiState
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@NavigationScreen(Route.OnboardingRoute.Welcome::class)
@Composable
fun ColumnScope.OnboardingWelcomeScreen(
    uiState: OnboardingWelcomeUiState,
    onIntent: (OnboardingWelcomeIntent) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 5 })

    LaunchedEffect(pagerState.currentPage) {
        onIntent(OnboardingWelcomeIntent.OnPageChanged(pagerState.currentPage))
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
            .background(colorResource(DomatColors.surfaceDefault)),
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
            OnboardingPage.WELCOME -> stringResource(MR.strings.onboarding_btn_welcome)
            OnboardingPage.PRICING -> stringResource(MR.strings.onboarding_btn_pricing)
            OnboardingPage.COMMUNITY -> stringResource(MR.strings.onboarding_btn_community)
            OnboardingPage.TRUST -> stringResource(MR.strings.onboarding_btn_trust)
            OnboardingPage.EFFORTLESS -> stringResource(MR.strings.onboarding_btn_effortless)
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

@Composable
private fun OnboardingWelcomePageContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(DomatColors.primary10)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(MR.images.img_welcome_neighborhood),
                contentDescription = stringResource(MR.strings.onboarding_image_neighborhood_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = buildAnnotatedString {
                append(stringResource(MR.strings.onboarding_welcome_title_line1))
                append("\n")
                withStyle(SpanStyle(color = colorResource(DomatColors.primary))) {
                    append(stringResource(MR.strings.onboarding_welcome_title_highlight))
                }
                append("\n")
                append(stringResource(MR.strings.onboarding_welcome_title_line3))
            },
            style = MaterialTheme.typography.displayMedium,
            color = colorResource(DomatColors.textPrimary),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(MR.strings.onboarding_welcome_body),
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(DomatColors.textSecondary),
        )
    }
}

@NavigationEffectHandler(Route.OnboardingRoute.Welcome::class)
@Composable
fun OnboardingWelcomeEffectHandler(effectFlow: Flow<OnboardingWelcomeEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                OnboardingWelcomeEffect.NavigateToLogin ->
                    navigator.navigate(Route.AuthRoute.Login)
            }
        }
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
