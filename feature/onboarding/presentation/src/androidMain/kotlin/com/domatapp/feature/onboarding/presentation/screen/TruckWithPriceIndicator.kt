package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
internal fun TruckWithPriceIndicator(
    uiModel: CommunityHeroCardUiModel,
    primary: Color,
    borderLight: Color,
) {
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
                painter = painterResource(R.drawable.ic_delivery_truck_green),
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
                    painter = painterResource(R.drawable.ic_trending_down),
                    contentDescription = null,
                    modifier = Modifier.size(width = 12.dp, height = 7.dp),
                )
                Text(
                    text = uiModel.currentPrice,
                    color = primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                )
            }
            Text(
                text = uiModel.originalPrice,
                color = colorResource(R.color.slate_400),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.LineThrough,
                lineHeight = 16.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TruckWithPriceIndicatorPreview() {
    DomatTheme {
        TruckWithPriceIndicator(
            uiModel = CommunityHeroCardUiModel(
                currentPrice = "₺45",
                originalPrice = "₺80",
            ),
            primary = colorResource(R.color.malachite),
            borderLight = colorResource(R.color.slate_100),
        )
    }
}
