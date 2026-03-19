package com.domatapp.core.presentation.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.design.theme.DomatTheme
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatProductCard(
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
        colors = CardDefaults.cardColors(containerColor = colorResource(DomatColors.surfaceDefault)),
        border = BorderStroke(1.dp, colorResource(DomatColors.borderDefault)),
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
                color = colorResource(DomatColors.textPrimary),
                maxLines = 2,
            )
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(DomatColors.textPrimary),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = colorResource(DomatColors.textSecondary),
                maxLines = 1,
            )
        }
    }
}

@Composable
fun DomatSelectionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) colorResource(DomatColors.primary) else colorResource(DomatColors.borderDefault)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        onClick = onClick,
        modifier = modifier.size(width = 84.dp, height = 56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(DomatColors.surfaceDefault)),
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = colorResource(DomatColors.textPrimary),
            )
        }
    }
}

@Composable
fun DomatPaymentCard(
    cardNumber: String,
    cardInfo: String,
    logo: @Composable BoxScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(DomatColors.surfaceDefault)),
        border = BorderStroke(1.dp, colorResource(DomatColors.borderDefault)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.size(40.dp), content = logo)
            Column {
                Text(
                    text = cardNumber,
                    style = MaterialTheme.typography.labelLarge,
                    color = colorResource(DomatColors.textPrimary),
                )
                Text(
                    text = cardInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(DomatColors.textSecondary),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatProductCardPreview() {
    DomatTheme {
        DomatProductCard(
            name = "Domates",
            price = "₺45,00",
            description = "1 kg",
            imageContent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatSelectionCardPreview() {
    DomatTheme {
        DomatSelectionCard(
            text = "A1",
            isSelected = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatPaymentCardPreview() {
    DomatTheme {
        DomatPaymentCard(
            cardNumber = "**** **** **** 1234",
            cardInfo = "Visa",
        )
    }
}
