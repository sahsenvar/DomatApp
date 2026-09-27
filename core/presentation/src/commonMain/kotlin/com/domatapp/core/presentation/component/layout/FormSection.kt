package com.domatapp.core.presentation.component.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing

/**
 * Titled content group on a card surface ("Kişisel Bilgiler", "Ödeme Yöntemi", ...).
 *
 * Figma: a `FormSection · <region-id>` **frame**, not a component instance - its first child is
 * the `title-large` title. Style: `surface` background + `outline-variant` 1 dp border (`card`
 * token), `md` corners, `sp4` padding, `sp3` between items.
 */
@Composable
fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .padding(MaterialTheme.spacing.sp4),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun FormSectionPreview() {
    DomatTheme {
        FormSection(title = "Kişisel Bilgiler", description = "İsteğe bağlı açıklama") {
            Text(text = "İçerik")
        }
    }
}
