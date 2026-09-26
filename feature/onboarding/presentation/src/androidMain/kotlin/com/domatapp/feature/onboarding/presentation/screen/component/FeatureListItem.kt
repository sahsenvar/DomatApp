package com.domatapp.feature.onboarding.presentation.screen.component

import com.domatapp.core.design.theme.spacing
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme

private val BorderWidth = 1.dp

@Composable
fun FeatureListItem(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(BorderWidth, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
            .padding(MaterialTheme.spacing.sp3 + BorderWidth),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeatureListItemPreview() {
    DomatTheme {
        FeatureListItem(
            text = "Üretici adı görünür",
            iconPainter = ColorPainter(Color.Green),
        )
    }
}
