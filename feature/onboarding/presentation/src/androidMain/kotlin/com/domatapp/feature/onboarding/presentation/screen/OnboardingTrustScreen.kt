package com.domatapp.feature.onboarding.presentation.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.presentation.component.list.DomatFeatureListItem
import com.domatapp.core.resource.R

data class TrustFeatureUiModel(
    @DrawableRes val icon: Int,
    val text: String,
)

@Composable
internal fun OnboardingTrustPageContent(modifier: Modifier = Modifier) {
    val features = listOf(
        TrustFeatureUiModel(
            icon = R.drawable.ic_feature_producer,
            text = stringResource(R.string.onboarding_trust_feature_producer),
        ),
        TrustFeatureUiModel(
            icon = R.drawable.ic_feature_location,
            text = stringResource(R.string.onboarding_trust_feature_location),
        ),
        TrustFeatureUiModel(
            icon = R.drawable.ic_feature_origin,
            text = stringResource(R.string.onboarding_trust_feature_guarantee),
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.white))
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        val dottedBorderColor = colorResource(R.color.malachite_20)
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.malachite_10))
                .drawBehind {
                    val insetPx = 16.dp.toPx()
                    val strokeWidthPx = 2.dp.toPx()
                    val radius = (size.minDimension / 2f) - insetPx - (strokeWidthPx / 2f)
                    drawCircle(
                        color = dottedBorderColor,
                        radius = radius,
                        style = Stroke(
                            width = strokeWidthPx,
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(6f, 6f),
                                phase = 0f,
                            ),
                        ),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.ic_shield_large),
                    contentDescription = null,
                    modifier = Modifier.size(width = 70.dp, height = 90.dp),
                )
                Image(
                    painter = painterResource(R.drawable.ic_trust_wallet_badge),
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
                text = stringResource(R.string.onboarding_trust_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colorResource(R.color.slate_900),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.onboarding_trust_body),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.slate_600),
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
            features.forEach { feature ->
                DomatFeatureListItem(
                    iconPainter = painterResource(feature.icon),
                    text = feature.text,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingTrustPageContentPreview() {
    DomatTheme {
        OnboardingTrustPageContent()
    }
}
