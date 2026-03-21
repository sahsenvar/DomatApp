package com.domatapp.feature.auth.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.domatapp.core.resource.R

@Composable
internal fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(442.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(colorResource(R.color.hunter_green), colorResource(R.color.phthalo_green)),
                    )
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, colorResource(R.color.black_55)),
                        startY = 150f,
                    )
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .background(color = colorResource(R.color.malachite).copy(alpha = 0.2f), shape = CircleShape)
                    .border(width = 1.dp, color = colorResource(R.color.malachite), shape = CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = "\uD83C\uDF31", style = MaterialTheme.typography.labelSmall)
                Text(
                    text = stringResource(R.string.onboarding_login_hero_badge),
                    color = colorResource(R.color.malachite),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Text(
                text = stringResource(R.string.app_name),
                color = colorResource(R.color.white),
                style = MaterialTheme.typography.displayMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HeroSectionPreview() {
    DomatTheme {
        HeroSection()
    }
}
