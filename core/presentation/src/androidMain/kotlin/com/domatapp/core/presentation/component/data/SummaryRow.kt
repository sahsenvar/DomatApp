package com.domatapp.core.presentation.component.data

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing

/**
 * Order-summary line: label (+ optional detail under it) on the left, amount on the right.
 *
 * Figma: `Data/SummaryRow`. `emphasis = true` is the total line (`title-large` on both sides).
 * [amount] is already formatted (`₺96,00`) - formatting is the caller's job.
 */
@Composable
fun SummaryRow(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    emphasis: Boolean = false,
) {
    val textStyle = if (emphasis) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = textStyle, color = MaterialTheme.colorScheme.onSurface)
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(text = amount, style = textStyle, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryRowPreview() {
    DomatTheme {
        SummaryRow(label = "Domates", detail = "3 kg", amount = "₺96,00")
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryRowEmphasisPreview() {
    DomatTheme {
        SummaryRow(label = "Toplam (en fazla)", amount = "₺188,00", emphasis = true)
    }
}
