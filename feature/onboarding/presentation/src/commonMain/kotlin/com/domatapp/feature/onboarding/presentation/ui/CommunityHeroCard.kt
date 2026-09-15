package com.domatapp.feature.onboarding.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.domatapp.core.design.theme.DomatColors
import org.jetbrains.compose.ui.tooling.preview.Preview
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
                        colors = listOf(DomatColors.Malachite5, Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = (-40).dp)
                .blur(32.dp)
                .background(DomatColors.Malachite5, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .blur(32.dp)
                .background(DomatColors.Malachite10, CircleShape),
        )

        Column(
            modifier = Modifier.padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            OverlappingAvatars(
                primary20 = DomatColors.Malachite20,
                primary30 = DomatColors.Malachite30,
                primary = DomatColors.Malachite,
            )
            TruckWithPriceIndicator(
                uiModel = uiModel,
                primary = DomatColors.Malachite,
                borderLight = DomatColors.Slate100,
            )
        }
    }
}

@Preview
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
