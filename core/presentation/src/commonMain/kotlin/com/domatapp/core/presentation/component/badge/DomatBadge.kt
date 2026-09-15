package com.domatapp.core.presentation.component.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.domatapp.core.design.theme.DomatColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme

enum class BadgeVariant { Primary, Warning, Error, Dark, Info, Success }

@Composable
fun DomatBadge(
    text: String,
    variant: BadgeVariant = BadgeVariant.Primary,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = when (variant) {
        BadgeVariant.Primary -> DomatColors.Malachite to DomatColors.Slate900
        BadgeVariant.Warning -> DomatColors.Orange400 to DomatColors.White
        BadgeVariant.Error -> DomatColors.Red500 to DomatColors.White
        BadgeVariant.Dark -> DomatColors.Slate900 to DomatColors.White
        BadgeVariant.Info -> DomatColors.Blue100 to DomatColors.Blue900
        BadgeVariant.Success -> DomatColors.Emerald100 to DomatColors.Emerald600
    }

    Text(
        text = text,
        color = contentColor,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Preview
@Composable
private fun DomatBadgePreview() {
    DomatTheme {
        DomatBadge(text = "Yeni", variant = BadgeVariant.Primary)
    }
}
