package com.domatapp.feature.auth.presentation.screen.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import com.domatapp.core.design.theme.DomatColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme

@Composable
fun LocationCard(
    label: String,
    value: String,
    checkmarkPainter: Painter,
    modifier: Modifier = Modifier,
    isLocked: Boolean = true,
    lockPainter: Painter? = null,
    cardAlpha: Float = 1f,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clip(RoundedCornerShape(12.dp))
            .background(DomatColors.Slate50)
            .padding(17.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DomatColors.Malachite20),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = checkmarkPainter,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = DomatColors.Slate500,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = DomatColors.Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isLocked && lockPainter != null) {
            Image(
                painter = lockPainter,
                contentDescription = null,
                modifier = Modifier.size(width = 13.dp, height = 17.dp),
            )
        }
    }
}

@Preview
@Composable
private fun LocationCardPreview() {
    DomatTheme {
        LocationCard(
            label = "Mahalle",
            value = "Aydınlı Mh.",
            checkmarkPainter = ColorPainter(Color.Green),
        )
    }
}
