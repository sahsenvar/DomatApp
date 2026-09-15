package com.domatapp.core.presentation.component.tag

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.domatapp.core.design.theme.DomatColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme

enum class TagVariant { New, Sale, Discount, Limited }

@Composable
fun Tag(
    text: String,
    variant: TagVariant,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = when (variant) {
        TagVariant.New -> DomatColors.Malachite to DomatColors.Slate900
        TagVariant.Sale -> DomatColors.Orange400 to DomatColors.White
        TagVariant.Discount -> DomatColors.Red500 to DomatColors.White
        TagVariant.Limited -> DomatColors.Slate900 to DomatColors.White
    }

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

@Preview
@Composable
private fun TagPreview() {
    DomatTheme {
        Tag(text = "YENİ", variant = TagVariant.New)
    }
}
