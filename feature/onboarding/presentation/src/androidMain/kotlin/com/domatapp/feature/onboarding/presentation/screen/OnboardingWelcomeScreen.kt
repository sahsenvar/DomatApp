package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.domatapp.core.presentation.component.button.DomatGoogleSignInButton
import com.domatapp.core.presentation.component.indicator.DomatProgressDots
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.core.resource.MR
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeEffect
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeIntent
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeUiState
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.stringResource
import domatapp.feature.onboarding.presentation.generated.resources.Res
import domatapp.feature.onboarding.presentation.generated.resources.ic_google
import domatapp.feature.onboarding.presentation.generated.resources.img_welcome_neighborhood
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

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
            when (page) {
                0 -> OnboardingWelcomePageContent()
                1 -> OnboardingEffortlessPageContent()
                2 -> OnboardingPricingPageContent()
                3 -> OnboardingCommunityPageContent()
                4 -> OnboardingTrustPageContent()
                else -> Unit
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            DomatProgressDots(
                totalDots = 5,
                activeIndex = uiState.currentPage,
            )
            DomatGoogleSignInButton(
                onClick = { onIntent(OnboardingWelcomeIntent.GoogleSignInClicked) },
                iconPainter = painterResource(Res.drawable.ic_google),
                text = stringResource(MR.strings.google_sign_in_button_text),
            )
        }
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
                painter = painterResource(Res.drawable.img_welcome_neighborhood),
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
                    navigator.navigate(Route.OnboardingRoute.Login)
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
