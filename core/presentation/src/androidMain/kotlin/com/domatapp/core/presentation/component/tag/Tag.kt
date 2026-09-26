package com.domatapp.core.presentation.component.tag

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.domatColors
import com.domatapp.core.design.theme.spacing

enum class TagVariant { New, Sale, Discount, Limited }

@Composable
fun Tag(
    text: String,
    variant: TagVariant,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val extended = MaterialTheme.domatColors
    val (containerColor, contentColor) = when (variant) {
        TagVariant.New -> colors.primary to colors.onPrimary
        TagVariant.Sale -> extended.warningContainer to extended.onWarningContainer
        TagVariant.Discount -> colors.error to colors.onError
        TagVariant.Limited -> colors.inverseSurface to colors.inverseOnSurface
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = containerColor,
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.sp2, vertical = MaterialTheme.spacing.sp1),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TagPreview() {
    DomatTheme {
        Tag(text = "YENİ", variant = TagVariant.New)
    }
}
