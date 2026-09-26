package com.domatapp.core.presentation.component.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.presentation.component.button.TextLink
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.consent_hide_text
import com.domatapp.core.resource.generated.resources.consent_read_text
import org.jetbrains.compose.resources.stringResource

/** Visual width of the Material 3 checkbox box without its interactive padding. */
private val CheckboxBoxSize = 20.dp

/**
 * Checkbox + label for consents (marketing, KVKK, distance-sales contract). Legal consents are
 * **unchecked by default** - the caller owns [checked].
 *
 * Figma: `Input/ConsentCheckbox`. The whole row is the touch target (≥ 48 dp) and is exposed to
 * accessibility as one checkbox. When [legalText] is set, a "Metni oku" [TextLink] under the label
 * expands the text in place ([expanded] / [onExpandToggle], Figma `expanded` variant).
 *
 * @param legalText `null` = not expandable (Figma `expandable = false`).
 * @param isError required-but-unchecked on submit (Figma `required`).
 */
@Composable
fun ConsentCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    legalText: String? = null,
    expanded: Boolean = false,
    onExpandToggle: () -> Unit = {},
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = checked,
                // null: the row handles the toggle, so the checkbox is not a second focus target.
                onCheckedChange = null,
                enabled = enabled,
                modifier = Modifier.size(CheckboxBoxSize),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedColor = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                ),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f),
            )
        }
        if (legalText != null) {
            val indent = Modifier.padding(start = CheckboxBoxSize + MaterialTheme.spacing.sp3)
            TextLink(
                text = stringResource(if (expanded) Res.string.consent_hide_text else Res.string.consent_read_text),
                onClick = onExpandToggle,
                enabled = enabled,
                modifier = indent,
            )
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = legalText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = indent
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        .padding(MaterialTheme.spacing.sp3),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConsentCheckboxPreview() {
    DomatTheme {
        ConsentCheckbox(
            label = "Kampanya ve duyurulardan e-postayla haberdar olmak istiyorum.",
            checked = false,
            onCheckedChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConsentCheckboxExpandablePreview() {
    DomatTheme {
        ConsentCheckbox(
            label = "Mesafeli Satış Sözleşmesi'ni ve Ön Bilgilendirme Formu'nu okudum, onaylıyorum.",
            checked = true,
            onCheckedChange = {},
            legalText = "Sözleşme metni",
            expanded = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConsentCheckboxErrorPreview() {
    DomatTheme {
        ConsentCheckbox(
            label = "Mesafeli Satış Sözleşmesi'ni ve Ön Bilgilendirme Formu'nu okudum, onaylıyorum.",
            checked = false,
            onCheckedChange = {},
            legalText = "Sözleşme metni",
            isError = true,
        )
    }
}
