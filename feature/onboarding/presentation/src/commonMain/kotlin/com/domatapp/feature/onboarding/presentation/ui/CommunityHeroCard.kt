package com.domatapp.feature.onboarding.presentation.ui

import com.domatapp.core.design.theme.spacing
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme

data class CommunityHeroCardUiModel(
    val currentPrice: String,
    val originalPrice: String,
)

@Composable
internal fun CommunityHeroCard(uiModel: CommunityHeroCardUiModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f),
            )
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = (-40).dp)
                .blur(32.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .blur(32.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
        )

        Column(
            modifier = Modifier.padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp6),
        ) {
            OverlappingAvatars()
            TruckWithPriceIndicator(
                uiModel = uiModel,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityHeroCardPreview() {
    DomatTheme {
        CommunityHeroCard(
            uiModel = CommunityHeroCardUiModel(
                currentPrice = "₺45",
                originalPrice = "₺80",
            )
        )
    }
}
