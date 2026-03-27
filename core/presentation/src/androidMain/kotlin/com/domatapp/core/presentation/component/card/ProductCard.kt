package com.domatapp.core.presentation.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
fun ProductCard(
    name: String,
    price: String,
    description: String,
    imageContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(163.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.white)),
        border = BorderStroke(1.dp, colorResource(R.color.slate_200)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            content = imageContent,
        )
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = colorResource(R.color.slate_900),
                maxLines = 2,
            )
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(R.color.slate_900),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = colorResource(R.color.slate_600),
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductCardPreview() {
    DomatTheme {
        ProductCard(
            name = "Domates",
            price = "₺45,00",
            description = "1 kg",
            imageContent = {},
        )
    }
}
