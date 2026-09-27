package com.domatapp.core.presentation.component.data

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.presentation.component.button.TextLink

/**
 * Read-only label + value row with an optional trailing text action. Figma: `Data/InfoRow`.
 * Spec: minHeight 48, padding sp2 vertical, gap sp3; label body-small on-surface-variant, value body-large.
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trailingAction: String? = null,
    onTrailingActionClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = MaterialTheme.spacing.sp2),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        if (trailingAction != null) {
            TextLink(text = trailingAction, onClick = onTrailingActionClick)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoRowPreview() {
    DomatTheme { InfoRow(label = "Teslimat kodu", value = "#DAL1") }
}

@Preview(showBackground = true)
@Composable
private fun InfoRowActionPreview() {
    DomatTheme { InfoRow(label = "Fatura", value = "Elif Yılmaz", trailingAction = "Düzenle") }
}
