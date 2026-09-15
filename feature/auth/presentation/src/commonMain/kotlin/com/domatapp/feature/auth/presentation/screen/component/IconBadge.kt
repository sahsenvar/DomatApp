package com.domatapp.feature.auth.presentation.screen.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.domatapp.core.design.theme.DomatColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme

@Composable
fun IconBadge(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
    containerColor: Color = DomatColors.Malachite20,
    borderColor: Color = DomatColors.Malachite30,
    contentColor: Color = DomatColors.Malachite,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = 13.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = iconPainter,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

@Preview
@Composable
private fun IconBadgePreview() {
    DomatTheme {
        IconBadge(
            text = "Taze & Yerel",
            iconPainter = ColorPainter(Color.Green),
        )
    }
}
