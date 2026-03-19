package com.domatapp.core.presentation.component.badge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.design.theme.DomatTheme
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatHeroBadge(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(9999.dp),
        color = colorResource(DomatColors.primary20),
        border = BorderStroke(1.dp, colorResource(DomatColors.primary30)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
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
                color = colorResource(DomatColors.primary),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatHeroBadgePreview() {
    DomatTheme {
        DomatHeroBadge(
            text = "Taze & Yerel",
            iconPainter = ColorPainter(Color.Green),
        )
    }
}
