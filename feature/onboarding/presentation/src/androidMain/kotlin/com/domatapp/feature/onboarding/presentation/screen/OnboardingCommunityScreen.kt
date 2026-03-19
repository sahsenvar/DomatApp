package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.core.presentation.component.button.DomatPrimaryButton
import com.domatapp.core.presentation.component.indicator.DomatProgressDots
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.feature.onboarding.presentation.model.community.OnboardingCommunityEffect
import com.domatapp.feature.onboarding.presentation.model.community.OnboardingCommunityIntent
import com.domatapp.feature.onboarding.presentation.model.community.OnboardingCommunityUiState
import dev.icerock.moko.resources.compose.colorResource
import domatapp.feature.onboarding.presentation.generated.resources.Res
import domatapp.feature.onboarding.presentation.generated.resources.ic_arrow_forward
import domatapp.feature.onboarding.presentation.generated.resources.ic_delivery_truck_green
import domatapp.feature.onboarding.presentation.generated.resources.ic_person_community
import domatapp.feature.onboarding.presentation.generated.resources.ic_person_community_white
import domatapp.feature.onboarding.presentation.generated.resources.ic_trending_down
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@NavigationScreen(Route.OnboardingRoute.Community::class)
@Composable
fun ColumnScope.OnboardingCommunityScreen(
    uiState: OnboardingCommunityUiState,
    onIntent: (OnboardingCommunityIntent) -> Unit,
) {
    val primary = colorResource(DomatColors.primary)
    val surfaceDefault = colorResource(DomatColors.surfaceDefault)
    val textPrimary = colorResource(DomatColors.textPrimary)
    val textSecondary = colorResource(DomatColors.textSecondary)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceDefault),
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
                CommunityHeroCard()

                Text(
                    text = buildAnnotatedString {
                        append("Birlikte alalım,\n")
                        withStyle(SpanStyle(color = primary)) {
                            append("daha az ödeyelim")
                        }
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    color = textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = "Mahallenizde ne kadar çok kişi sipariş\n" +
                        "verirse, nakliye maliyeti o kadar düşer. Biz\n" +
                        "de bu tasarrufu doğrudan size yansıtırız.\n" +
                        "Birlikte sipariş verdiğimizde herkes kazanır.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                DomatProgressDots(totalDots = 3, activeIndex = 2)
                DomatPrimaryButton(
                    text = "Uygunsa kalitesi kötü müdür?",
                    onClick = { onIntent(OnboardingCommunityIntent.GoNext) },
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

@Composable
private fun CommunityHeroCard() {
    val primary5 = colorResource(DomatColors.primary5)
    val primary10 = colorResource(DomatColors.primary10)
    val borderLight = colorResource(DomatColors.borderLight)
    val primary = colorResource(DomatColors.primary)
    val primary20 = colorResource(DomatColors.primary20)
    val primary30 = colorResource(DomatColors.primary30)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f),
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(primary5, Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = (-40).dp)
                .blur(32.dp)
                .background(primary5, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .blur(32.dp)
                .background(primary10, CircleShape),
        )

        Column(
            modifier = Modifier.padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            OverlappingAvatars(primary20 = primary20, primary30 = primary30, primary = primary)
            TruckWithPriceIndicator(primary = primary, borderLight = borderLight)
        }
    }
}

@Composable
private fun OverlappingAvatars(primary20: Color, primary30: Color, primary: Color) {
    Box(
        modifier = Modifier
            .width(48.dp + 48.dp + 48.dp + 48.dp - 16.dp - 16.dp - 16.dp)
            .height(48.dp),
    ) {
        PersonAvatarCircle(icon = Res.drawable.ic_person_community, backgroundColor = primary20, offsetX = 0.dp)
        PersonAvatarCircle(icon = Res.drawable.ic_person_community, backgroundColor = primary30, offsetX = 32.dp)
        PersonAvatarCircle(icon = Res.drawable.ic_person_community, backgroundColor = primary30, offsetX = 64.dp)
        PersonAvatarCircle(icon = Res.drawable.ic_person_community_white, backgroundColor = primary, offsetX = 96.dp)
    }
}

@Composable
private fun PersonAvatarCircle(icon: DrawableResource, backgroundColor: Color, offsetX: Dp) {
    Box(
        modifier = Modifier
            .offset(x = offsetX)
            .size(48.dp)
            .border(4.dp, Color.White, CircleShape)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun TruckWithPriceIndicator(primary: Color, borderLight: Color) {
    Box(
        modifier = Modifier
            .width(256.dp)
            .height(96.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-16).dp)
                .clip(CircleShape)
                .background(borderLight),
        )

        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 44.dp)
                .align(Alignment.BottomStart)
                .offset(y = (-24).dp)
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    ambientColor = Color.Black.copy(alpha = 0.05f),
                    spotColor = Color.Black.copy(alpha = 0.05f),
                )
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, borderLight, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_delivery_truck_green),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16).dp),
            horizontalAlignment = Alignment.End,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_trending_down),
                    contentDescription = null,
                    modifier = Modifier.size(width = 12.dp, height = 7.dp),
                )
                Text(
                    text = "₺2.00",
                    color = primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                )
            }
            Text(
                text = "₺12.50",
                color = colorResource(DomatColors.textMuted),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.LineThrough,
                lineHeight = 16.sp,
            )
        }
    }
}

@NavigationEffectHandler(Route.OnboardingRoute.Community::class)
@Composable
fun OnboardingCommunityEffectHandler(effectFlow: Flow<OnboardingCommunityEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                OnboardingCommunityEffect.NavigateToTrust ->
                    navigator.navigate(Route.OnboardingRoute.Trust)
                OnboardingCommunityEffect.NavigateBack ->
                    navigator.popBack()
            }
        }
    }
}
