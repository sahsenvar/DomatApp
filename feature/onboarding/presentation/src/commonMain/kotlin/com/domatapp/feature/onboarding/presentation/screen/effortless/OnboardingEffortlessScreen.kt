package com.domatapp.feature.onboarding.presentation.screen.effortless

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
import com.domatapp.core.design.theme.DomatColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.img_effortless_illustration
import com.domatapp.core.resource.generated.resources.onboarding_effortless_body
import com.domatapp.core.resource.generated.resources.onboarding_effortless_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingEffortlessPageContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DomatColors.White),
    ) {
        Box(
            modifier = Modifier
                .size(256.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-96).dp, y = 96.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(DomatColors.Malachite5, Color.Transparent),
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
                        colors = listOf(DomatColors.Malachite10, Color.Transparent),
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
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.onboarding_effortless_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = DomatColors.Slate900,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.onboarding_effortless_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DomatColors.Slate600,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingEffortlessPageContentPreview() {
    DomatTheme {
        OnboardingEffortlessPageContent()
    }
}
