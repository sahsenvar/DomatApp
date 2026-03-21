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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

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
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.white)),
        border = BorderStroke(1.dp, colorResource(R.color.slate_200)),
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
                    color = colorResource(R.color.slate_900),
                )
                Text(
                    text = cardInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.slate_600),
                )
            }
        }
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
