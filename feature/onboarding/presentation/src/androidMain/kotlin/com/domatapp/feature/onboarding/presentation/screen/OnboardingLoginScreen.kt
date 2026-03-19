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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.core.presentation.component.badge.DomatHeroBadge
import com.domatapp.core.presentation.component.button.DomatGoogleSignInButton
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.core.resource.MR
import com.domatapp.feature.onboarding.presentation.model.login.OnboardingLoginEffect
import com.domatapp.feature.onboarding.presentation.model.login.OnboardingLoginIntent
import com.domatapp.feature.onboarding.presentation.model.login.OnboardingLoginUiState
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@NavigationScreen(Route.OnboardingRoute.Login::class)
@Composable
fun ColumnScope.OnboardingLoginScreen(
    uiState: OnboardingLoginUiState,
    onIntent: (OnboardingLoginIntent) -> Unit,
) {
    val heroShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    val primary = colorResource(DomatColors.primary)
    val textSecondary = colorResource(DomatColors.textSecondary)
    val textMuted = colorResource(DomatColors.textMuted)
    val surfaceDefault = colorResource(DomatColors.surfaceDefault)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceDefault)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(442.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = heroShape,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f),
                )
                .clip(heroShape),
        ) {
            Image(
                painter = painterResource(MR.images.img_hero_login),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.5f to Color.Black.copy(alpha = 0.2f),
                                1.0f to Color.Black.copy(alpha = 0.7f),
                            ),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DomatHeroBadge(
                    iconPainter = painterResource(MR.images.ic_leaf_badge),
                    text = stringResource(MR.strings.onboarding_login_hero_badge),
                )
                Text(
                    text = stringResource(MR.strings.app_name),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(MR.strings.onboarding_login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(124.dp))

            DomatGoogleSignInButton(
                onClick = { onIntent(OnboardingLoginIntent.OnGoogleSignInClicked) },
                iconPainter = painterResource(MR.images.ic_google),
                text = stringResource(MR.strings.google_sign_in_button_text),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = buildAnnotatedString {
                    append(stringResource(MR.strings.onboarding_login_tos_prefix))
                    withStyle(
                        SpanStyle(
                            color = primary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(stringResource(MR.strings.onboarding_login_tos_link1))
                    }
                    append(stringResource(MR.strings.onboarding_login_tos_connector))
                    withStyle(
                        SpanStyle(
                            color = primary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(stringResource(MR.strings.onboarding_login_tos_link2))
                    }
                    append(stringResource(MR.strings.onboarding_login_tos_suffix))
                },
                style = MaterialTheme.typography.labelMedium,
                color = textMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@NavigationEffectHandler(Route.OnboardingRoute.Login::class)
@Composable
fun OnboardingLoginEffectHandler(effectFlow: Flow<OnboardingLoginEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                OnboardingLoginEffect.NavigateToLocationSelection ->
                    navigator.navigate(Route.OnboardingRoute.LocationSelection)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingLoginScreenPreview() {
    DomatTheme {
        Column {
            OnboardingLoginScreen(
                uiState = OnboardingLoginUiState(),
                onIntent = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingLoginEffectHandlerPreview() {
    DomatTheme {
        OnboardingLoginEffectHandler(effectFlow = kotlinx.coroutines.flow.emptyFlow())
    }
}
