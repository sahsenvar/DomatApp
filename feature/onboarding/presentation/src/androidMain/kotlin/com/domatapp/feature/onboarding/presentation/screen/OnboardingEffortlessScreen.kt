package com.domatapp.feature.onboarding.presentation.screen

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.MR
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun OnboardingEffortlessPageContent(modifier: Modifier = Modifier) {
    val glowColorBottomLeft = colorResource(DomatColors.primary5)
    val glowColorTopRight = colorResource(DomatColors.primary10)
    val bgColor = colorResource(DomatColors.surfaceDefault)

    Box(
        modifier = modifier
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
                    painter = painterResource(MR.images.img_effortless_illustration),
                    contentDescription = null,
                    modifier = Modifier.size(320.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(MR.strings.onboarding_effortless_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = colorResource(DomatColors.textPrimary),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(MR.strings.onboarding_effortless_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(DomatColors.textSecondary),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingEffortlessPageContentPreview() {
    DomatTheme {
        OnboardingEffortlessPageContent()
    }
}
