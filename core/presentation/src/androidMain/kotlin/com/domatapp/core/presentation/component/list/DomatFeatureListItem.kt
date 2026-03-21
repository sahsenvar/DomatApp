package com.domatapp.core.presentation.component.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
fun DomatFeatureListItem(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colorResource(R.color.slate_50),
        border = BorderStroke(1.dp, colorResource(R.color.slate_100)),
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(R.color.slate_900),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatFeatureListItemPreview() {
    DomatTheme {
        DomatFeatureListItem(
            text = "Üretici adı görünür",
            iconPainter = ColorPainter(Color.Green),
        )
    }
}
