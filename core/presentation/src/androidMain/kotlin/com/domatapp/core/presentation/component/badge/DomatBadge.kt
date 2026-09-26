package com.domatapp.core.presentation.component.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.domatColors
import com.domatapp.core.design.theme.spacing

enum class BadgeVariant { Primary, Warning, Error, Dark, Info, Success }

@Composable
fun DomatBadge(
    text: String,
    variant: BadgeVariant = BadgeVariant.Primary,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = MaterialTheme.domatColors
    val (containerColor, contentColor) = when (variant) {
        BadgeVariant.Primary -> colors.primary to colors.onPrimary
        BadgeVariant.Warning -> extended.warningContainer to extended.onWarningContainer
        BadgeVariant.Error -> colors.error to colors.onError
        BadgeVariant.Dark -> colors.inverseSurface to colors.inverseOnSurface
        BadgeVariant.Info -> colors.tertiaryContainer to colors.onTertiaryContainer
        BadgeVariant.Success -> extended.successContainer to extended.onSuccessContainer
    }

    Text(
        text = text,
        color = contentColor,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = MaterialTheme.spacing.sp2, vertical = MaterialTheme.spacing.sp1),
    )
}

@Preview(showBackground = true)
@Composable
private fun DomatBadgePreview() {
    DomatTheme {
        DomatBadge(text = "Yeni", variant = BadgeVariant.Primary)
    }
}
