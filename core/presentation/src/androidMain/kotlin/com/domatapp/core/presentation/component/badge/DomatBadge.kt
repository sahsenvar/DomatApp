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
import dev.icerock.moko.resources.compose.colorResource

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
    DomatBadgeVariant.Primary -> colorResource(DomatColors.primary) to colorResource(DomatColors.textPrimary)
    DomatBadgeVariant.Warning -> colorResource(DomatColors.warning) to colorResource(DomatColors.textInverse)
    DomatBadgeVariant.Error -> colorResource(DomatColors.error) to colorResource(DomatColors.textInverse)
    DomatBadgeVariant.Dark -> colorResource(DomatColors.surfaceDark) to colorResource(DomatColors.textInverse)
    DomatBadgeVariant.Info -> colorResource(DomatColors.infoLight) to colorResource(DomatColors.info)
    DomatBadgeVariant.Success -> colorResource(DomatColors.successLight) to colorResource(DomatColors.success)
}
