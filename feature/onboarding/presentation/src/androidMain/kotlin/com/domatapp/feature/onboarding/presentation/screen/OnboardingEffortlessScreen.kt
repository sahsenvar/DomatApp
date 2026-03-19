package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.core.presentation.component.button.DomatPrimaryButton
import com.domatapp.core.presentation.component.indicator.DomatProgressDots
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.feature.onboarding.presentation.model.effortless.OnboardingEffortlessEffect
import com.domatapp.feature.onboarding.presentation.model.effortless.OnboardingEffortlessIntent
import com.domatapp.feature.onboarding.presentation.model.effortless.OnboardingEffortlessUiState
import dev.icerock.moko.resources.compose.colorResource
import domatapp.feature.onboarding.presentation.generated.resources.Res
import domatapp.feature.onboarding.presentation.generated.resources.ic_arrow_forward
import domatapp.feature.onboarding.presentation.generated.resources.img_effortless_illustration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

@NavigationScreen(Route.OnboardingRoute.Effortless::class)
@Composable
fun ColumnScope.OnboardingEffortlessScreen(
    uiState: OnboardingEffortlessUiState,
    onIntent: (OnboardingEffortlessIntent) -> Unit,
) {
    val glowColorBottomLeft = colorResource(DomatColors.primary5)
    val glowColorTopRight = colorResource(DomatColors.primary10)
    val bgColor = colorResource(DomatColors.surfaceDefault)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
    ) {
        Box(
            modifier = Modifier
                .size(256.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-96).dp, y = 96.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColorBottomLeft, Color.Transparent),
                    ),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(256.dp)
                .align(Alignment.TopEnd)
                .offset(x = 96.dp, y = (-96).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColorTopRight, Color.Transparent),
                    ),
                    shape = CircleShape,
                ),
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.img_effortless_illustration),
                    contentDescription = null,
                    modifier = Modifier.size(320.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Haftalık alışverişi\nzahmetsiz hale\ngetiriyoruz",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colorResource(DomatColors.textPrimary),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Her hafta pazara gitmeye son. Bir kez\n" +
                        "sipariş verin, teslimat günü apartman önüne\n" +
                        "gelin, QR kodunuzu okutun ve\n" +
                        "domateslerinizi alın. İşte bu kadar!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(DomatColors.textPrimary),
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                DomatProgressDots(totalDots = 3, activeIndex = 1)
                DomatPrimaryButton(
                    text = "Süpermiş! Hadi başlayalım",
                    onClick = { onIntent(OnboardingEffortlessIntent.GoNext) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingContent = {
                        Image(
                            painter = painterResource(Res.drawable.ic_arrow_forward),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }
    }
}

@NavigationEffectHandler(Route.OnboardingRoute.Effortless::class)
@Composable
fun OnboardingEffortlessEffectHandler(effectFlow: Flow<OnboardingEffortlessEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                OnboardingEffortlessEffect.NavigateToPricing ->
                    navigator.navigate(Route.OnboardingRoute.Pricing)
                OnboardingEffortlessEffect.NavigateBack ->
                    navigator.popBack()
            }
        }
    }
}
