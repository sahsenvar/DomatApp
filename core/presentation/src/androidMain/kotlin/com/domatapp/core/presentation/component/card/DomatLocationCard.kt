package com.domatapp.core.presentation.component.card

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
fun DomatLocationCard(
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
            .background(colorResource(R.color.slate_50))
            .padding(17.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colorResource(R.color.malachite_20)),
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
                color = colorResource(R.color.slate_500),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = colorResource(R.color.slate_900),
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

@Preview(showBackground = true)
@Composable
private fun DomatLocationCardPreview() {
    DomatTheme {
        DomatLocationCard(
            label = "Mahalle",
            value = "Aydınlı Mh.",
            checkmarkPainter = ColorPainter(Color.Green),
        )
    }
}
