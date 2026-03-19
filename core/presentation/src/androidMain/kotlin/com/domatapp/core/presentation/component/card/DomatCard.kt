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
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors

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
        colors = CardDefaults.cardColors(containerColor = DomatColors.SurfaceDefault),
        border = BorderStroke(1.dp, DomatColors.BorderDefault),
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
                color = DomatColors.TextPrimary,
                maxLines = 2,
            )
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                color = DomatColors.TextPrimary,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = DomatColors.TextSecondary,
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
    val borderColor = if (isSelected) DomatColors.Primary else DomatColors.BorderDefault
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        onClick = onClick,
        modifier = modifier.size(width = 84.dp, height = 56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DomatColors.SurfaceDefault),
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = DomatColors.TextPrimary,
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
        colors = CardDefaults.cardColors(containerColor = DomatColors.SurfaceDefault),
        border = BorderStroke(1.dp, DomatColors.BorderDefault),
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
                    color = DomatColors.TextPrimary,
                )
                Text(
                    text = cardInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = DomatColors.TextSecondary,
                )
            }
        }
    }
}
