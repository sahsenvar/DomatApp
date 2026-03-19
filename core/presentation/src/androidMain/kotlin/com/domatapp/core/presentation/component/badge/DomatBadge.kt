package com.domatapp.core.presentation.component.badge

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors

enum class DomatBadgeVariant { Primary, Warning, Error, Dark, Info, Success }

@Composable
fun DomatBadge(
    text: String,
    variant: DomatBadgeVariant = DomatBadgeVariant.Primary,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = badgeColors(variant)

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = containerColor,
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun badgeColors(variant: DomatBadgeVariant): Pair<Color, Color> = when (variant) {
    DomatBadgeVariant.Primary -> DomatColors.Primary to DomatColors.TextPrimary
    DomatBadgeVariant.Warning -> DomatColors.Warning to DomatColors.TextInverse
    DomatBadgeVariant.Error -> DomatColors.Error to DomatColors.TextInverse
    DomatBadgeVariant.Dark -> DomatColors.SurfaceDark to DomatColors.TextInverse
    DomatBadgeVariant.Info -> DomatColors.InfoLight to DomatColors.Info
    DomatBadgeVariant.Success -> DomatColors.SuccessLight to DomatColors.Success
}
