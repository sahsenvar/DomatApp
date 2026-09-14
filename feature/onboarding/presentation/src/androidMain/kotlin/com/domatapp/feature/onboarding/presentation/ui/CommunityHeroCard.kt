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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

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
                        colors = listOf(colorResource(R.color.malachite_5), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = (-40).dp)
                .blur(32.dp)
                .background(colorResource(R.color.malachite_5), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .blur(32.dp)
                .background(colorResource(R.color.malachite_10), CircleShape),
        )

        Column(
            modifier = Modifier.padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            OverlappingAvatars(
                primary20 = colorResource(R.color.malachite_20),
                primary30 = colorResource(R.color.malachite_30),
                primary = colorResource(R.color.malachite),
            )
            TruckWithPriceIndicator(
                uiModel = uiModel,
                primary = colorResource(R.color.malachite),
                borderLight = colorResource(R.color.slate_100),
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
