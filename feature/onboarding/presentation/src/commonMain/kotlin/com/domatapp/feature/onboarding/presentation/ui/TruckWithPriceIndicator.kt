package com.domatapp.feature.onboarding.presentation.ui

import com.domatapp.core.design.theme.spacing
import com.domatapp.core.resource.generated.resources.ic_local_shipping
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.ic_trending_down
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun TruckWithPriceIndicator(
    uiModel: CommunityHeroCardUiModel,
) {
    val primary = MaterialTheme.colorScheme.primary
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
                .background(MaterialTheme.colorScheme.secondaryContainer),
        )

        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 44.dp)
                .align(Alignment.BottomStart)
                .offset(y = (-24).dp)
                .shadow(
                    elevation = 1.dp,
                    shape = MaterialTheme.shapes.small,
                    ambientColor = Color.Black.copy(alpha = 0.05f),
                    spotColor = Color.Black.copy(alpha = 0.05f),
                )
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_local_shipping),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
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
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp1),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_trending_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        )
    }
}
