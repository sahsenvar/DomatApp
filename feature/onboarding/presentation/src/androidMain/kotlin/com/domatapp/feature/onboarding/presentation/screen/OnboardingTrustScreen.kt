package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.core.presentation.component.button.DomatPrimaryButton
import com.domatapp.core.presentation.component.indicator.DomatProgressDots
import com.domatapp.core.presentation.component.list.DomatFeatureListItem
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.feature.onboarding.presentation.model.trust.OnboardingTrustEffect
import com.domatapp.feature.onboarding.presentation.model.trust.OnboardingTrustIntent
import com.domatapp.feature.onboarding.presentation.model.trust.OnboardingTrustUiState
import dev.icerock.moko.resources.compose.colorResource
import domatapp.feature.onboarding.presentation.generated.resources.Res
import domatapp.feature.onboarding.presentation.generated.resources.ic_feature_location
import domatapp.feature.onboarding.presentation.generated.resources.ic_feature_origin
import domatapp.feature.onboarding.presentation.generated.resources.ic_feature_producer
import domatapp.feature.onboarding.presentation.generated.resources.ic_shield_large
import domatapp.feature.onboarding.presentation.generated.resources.ic_trust_wallet_badge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

@NavigationScreen(Route.OnboardingRoute.Trust::class)
@Composable
fun ColumnScope.OnboardingTrustScreen(
    uiState: OnboardingTrustUiState,
    onIntent: (OnboardingTrustIntent) -> Unit,
) {
    val primary10 = colorResource(DomatColors.primary10)
    val primary20 = colorResource(DomatColors.primary20)
    val textPrimary = colorResource(DomatColors.textPrimary)
    val surfaceDefault = colorResource(DomatColors.surfaceDefault)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceDefault)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(primary10),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val insetPx = 16.dp.toPx()
                val strokeWidthPx = 2.dp.toPx()
                val radius = (size.minDimension / 2f) - insetPx - (strokeWidthPx / 2f)
                drawCircle(
                    color = primary20,
                    radius = radius,
                    style = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(6f, 6f),
                            phase = 0f,
                        ),
                    ),
                )
            }

            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(Res.drawable.ic_shield_large),
                    contentDescription = null,
                    modifier = Modifier.size(width = 70.dp, height = 90.dp),
                )
                Image(
                    painter = painterResource(Res.drawable.ic_trust_wallet_badge),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 42.dp, height = 41.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(
                text = "Basit. Adil. Şeffaf.",
                style = MaterialTheme.typography.headlineLarge,
                color = textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Siparişinizde eksik veya hasarlı bir ürün varsa,\ntutarı anında uygulama içi cüzdanınıza iade\nedilir.",
                style = MaterialTheme.typography.bodyMedium,
                color = textPrimary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DomatFeatureListItem(
                iconPainter = painterResource(Res.drawable.ic_feature_producer),
                text = "Üretici adı görünür",
            )
            DomatFeatureListItem(
                iconPainter = painterResource(Res.drawable.ic_feature_location),
                text = "Üretici şehri görünür",
            )
            DomatFeatureListItem(
                iconPainter = painterResource(Res.drawable.ic_feature_origin),
                text = "Memnun kalmazsanız paranız iade!",
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            DomatProgressDots(totalDots = 5, activeIndex = 4)
            DomatPrimaryButton(
                text = "Ben pazardan alıyorum \uD83D\uDE2C",
                onClick = { onIntent(OnboardingTrustIntent.GoNext) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@NavigationEffectHandler(Route.OnboardingRoute.Trust::class)
@Composable
fun OnboardingTrustEffectHandler(effectFlow: Flow<OnboardingTrustEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                OnboardingTrustEffect.NavigateToLogin ->
                    navigator.navigate(Route.OnboardingRoute.Login)
                OnboardingTrustEffect.NavigateBack ->
                    navigator.popBack()
            }
        }
    }
}
