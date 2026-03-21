package com.domatapp.core.presentation.component.tag

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

enum class DomatTagVariant { New, Sale, Discount, Limited }

@Composable
fun DomatTag(
    text: String,
    variant: DomatTagVariant,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = when (variant) {
        DomatTagVariant.New -> colorResource(R.color.malachite) to colorResource(R.color.slate_900)
        DomatTagVariant.Sale -> colorResource(R.color.orange_400) to colorResource(R.color.white)
        DomatTagVariant.Discount -> colorResource(R.color.red_500) to colorResource(R.color.white)
        DomatTagVariant.Limited -> colorResource(R.color.slate_900) to colorResource(R.color.white)
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

@Preview(showBackground = true)
@Composable
private fun DomatTagPreview() {
    DomatTheme {
        DomatTag(text = "YENİ", variant = DomatTagVariant.New)
    }
}
