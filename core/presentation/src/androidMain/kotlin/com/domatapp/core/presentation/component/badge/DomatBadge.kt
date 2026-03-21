package com.domatapp.core.presentation.component.badge

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

enum class DomatBadgeVariant { Primary, Warning, Error, Dark, Info, Success }

@Composable
fun DomatBadge(
    text: String,
    variant: DomatBadgeVariant = DomatBadgeVariant.Primary,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = when (variant) {
        DomatBadgeVariant.Primary -> colorResource(R.color.malachite) to colorResource(R.color.slate_900)
        DomatBadgeVariant.Warning -> colorResource(R.color.orange_400) to colorResource(R.color.white)
        DomatBadgeVariant.Error -> colorResource(R.color.red_500) to colorResource(R.color.white)
        DomatBadgeVariant.Dark -> colorResource(R.color.slate_900) to colorResource(R.color.white)
        DomatBadgeVariant.Info -> colorResource(R.color.blue_100) to colorResource(R.color.blue_900)
        DomatBadgeVariant.Success -> colorResource(R.color.emerald_100) to colorResource(R.color.emerald_600)
    }

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

@Preview(showBackground = true)
@Composable
private fun DomatBadgePreview() {
    DomatTheme {
        DomatBadge(text = "Yeni", variant = DomatBadgeVariant.Primary)
    }
}
