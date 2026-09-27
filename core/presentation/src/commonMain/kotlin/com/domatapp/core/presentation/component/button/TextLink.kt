package com.domatapp.core.presentation.component.button

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme

/**
 * Inline text link ("Farklı kart kullan", "Metni oku", "Kodu Tekrar Gönder").
 *
 * Figma: `Button/TextLink`. Colour is `tertiary` on purpose - `primary` green text on a white
 * surface fails contrast (see design/tokens/DESIGN.md). The text sits flush with the surrounding
 * content (no horizontal padding) while the touch target is still at least 48 dp tall.
 */
@Composable
fun TextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextLinkPreview() {
    DomatTheme {
        TextLink(text = "Farklı kart kullan", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun TextLinkDisabledPreview() {
    DomatTheme {
        TextLink(text = "Kodu Tekrar Gönder", onClick = {}, enabled = false)
    }
}
