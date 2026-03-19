package com.domatapp.core.presentation.component.tag

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource

enum class DomatTagVariant { New, Sale, Discount, Limited }

@Composable
fun DomatTag(
    text: String,
    variant: DomatTagVariant,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = tagColors(variant)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = containerColor,
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun tagColors(variant: DomatTagVariant): Pair<Color, Color> = when (variant) {
    DomatTagVariant.New -> colorResource(DomatColors.primary) to colorResource(DomatColors.textPrimary)
    DomatTagVariant.Sale -> colorResource(DomatColors.warning) to colorResource(DomatColors.textInverse)
    DomatTagVariant.Discount -> colorResource(DomatColors.error) to colorResource(DomatColors.textInverse)
    DomatTagVariant.Limited -> colorResource(DomatColors.surfaceDark) to colorResource(DomatColors.textInverse)
}
