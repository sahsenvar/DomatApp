package com.domatapp.feature.auth.presentation.screen.component

import com.domatapp.core.design.theme.spacing
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
fun IconBadge(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .border(BorderWidth, borderColor, CircleShape)
            .padding(horizontal = MaterialTheme.spacing.sp3 + BorderWidth, vertical = MaterialTheme.spacing.sp1 + BorderWidth),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IconBadgePreview() {
    DomatTheme {
        IconBadge(
            text = "Taze & Yerel",
            iconPainter = ColorPainter(Color.Green),
        )
    }
}
