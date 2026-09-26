package com.domatapp.feature.auth.presentation.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.ic_check
import com.domatapp.core.resource.generated.resources.ic_lock
import org.jetbrains.compose.resources.painterResource

/**
 * Önceden belirlenmiş (kilitli) konum satırı. Feature-yerel; components.yaml'da değil.
 * İkonlar Material Symbols (check, lock), renkler token.
 */
@Composable
fun LocationCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isLocked: Boolean = true,
    cardAlpha: Float = 1f,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(MaterialTheme.spacing.sp4),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isLocked) {
            Icon(
                painter = painterResource(Res.drawable.ic_lock),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationCardPreview() {
    DomatTheme {
        LocationCard(label = "Mahalle", value = "Aydınlı Mh.")
    }
}
