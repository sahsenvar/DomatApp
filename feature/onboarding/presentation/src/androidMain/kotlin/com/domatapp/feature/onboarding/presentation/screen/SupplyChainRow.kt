package com.domatapp.feature.onboarding.presentation.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

enum class SupplyChainRowVariant { Producer, Inactive, Consumer }

data class SupplyChainRowUiModel(
    @DrawableRes val icon: Int,
    val variant: SupplyChainRowVariant,
    val title: String,
    val subtitle: String,
    val showConnector: Boolean = true,
)

@Composable
internal fun SupplyChainRow(uiModel: SupplyChainRowUiModel) {
    val isInactive = uiModel.variant == SupplyChainRowVariant.Inactive
    val isConsumer = uiModel.variant == SupplyChainRowVariant.Consumer

    val iconSize = when (uiModel.variant) {
        SupplyChainRowVariant.Producer -> 22.5f
        SupplyChainRowVariant.Inactive -> 25f
        SupplyChainRowVariant.Consumer -> 20f
    }
    val iconBgColor = when (uiModel.variant) {
        SupplyChainRowVariant.Producer -> colorResource(R.color.malachite_20)
        SupplyChainRowVariant.Inactive -> colorResource(R.color.slate_200)
        SupplyChainRowVariant.Consumer -> colorResource(R.color.malachite)
    }
    val titleColor = if (isInactive) colorResource(R.color.slate_500) else colorResource(R.color.slate_900)
    val titleFontWeight = if (isInactive) FontWeight.Medium else FontWeight.Bold
    val subtitleColor = if (isInactive) colorResource(R.color.slate_400) else colorResource(R.color.malachite)
    val subtitleFontWeight = if (isConsumer) FontWeight.SemiBold else FontWeight.Normal
    val dividerColor = when (uiModel.variant) {
        SupplyChainRowVariant.Producer -> colorResource(R.color.malachite_30)
        SupplyChainRowVariant.Inactive -> colorResource(R.color.slate_200)
        SupplyChainRowVariant.Consumer -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isInactive) 0.4f else 1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.width(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (isConsumer) Modifier.shadow(
                            10.dp, CircleShape,
                            ambientColor = colorResource(R.color.malachite_30),
                            spotColor = colorResource(R.color.malachite_30),
                        ) else Modifier,
                    )
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(uiModel.icon),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize.dp),
                )
                if (isInactive) {
                    Text(
                        text = "✕",
                        color = colorResource(R.color.red_500),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (uiModel.showConnector) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(dividerColor),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),
        ) {
            Text(
                text = uiModel.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = titleFontWeight,
                    textDecoration = if (isInactive) TextDecoration.LineThrough else TextDecoration.None,
                ),
                color = titleColor,
            )
            Text(
                text = uiModel.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = subtitleFontWeight),
                color = subtitleColor,
            )
        }
    }

    if (uiModel.showConnector) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 64.dp),
            color = colorResource(R.color.slate_100),
            thickness = 1.dp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SupplyChainRowPreview() {
    DomatTheme {
        SupplyChainRow(
            uiModel = SupplyChainRowUiModel(
                icon = R.drawable.ic_pricing_producer,
                variant = SupplyChainRowVariant.Producer,
                title = stringResource(R.string.onboarding_pricing_producer_title),
                subtitle = stringResource(R.string.onboarding_pricing_producer_subtitle),
            )
        )
    }
}
