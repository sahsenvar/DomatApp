package com.domatapp.core.presentation.component.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing

/**
 * Card network shown on [PaymentCardOption].
 *
 * [badgeText] is the pilot's temporary text badge (Figma `brand` is a TEXT property: MC/VISA/TROY);
 * it is a trademark abbreviation, not translatable copy, so it does not live in strings.xml. Real
 * brand logos replace it once the assets exist (design/screens/C4/annotations.md).
 */
enum class CardBrand(val badgeText: String) {
    Mastercard("MC"),
    Visa("VISA"),
    Troy("TROY"),
    Unknown("•"),
}

/**
 * Saved-card row with radio selection: brand badge + `•••• 1234`.
 *
 * Figma: `Payment/CardOption`. Selected = `primary` 2 dp border, otherwise `outline-variant` 1 dp.
 * The whole row is the selectable target; the CVV field the design opens under a selected card is
 * the caller's content, not part of this component.
 */
@Composable
fun PaymentCardOption(
    brand: CardBrand,
    last4: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .border(if (selected) 2.dp else 1.dp, borderColor, MaterialTheme.shapes.medium)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.sp4, vertical = MaterialTheme.spacing.sp3),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            // null: the row is the selectable element.
            onClick = null,
            enabled = enabled,
            modifier = Modifier.size(24.dp),
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.outline,
            ),
        )
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraSmall)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraSmall)
                .padding(horizontal = MaterialTheme.spacing.sp3, vertical = MaterialTheme.spacing.sp1),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = brand.badgeText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = "•••• $last4",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentCardOptionSelectedPreview() {
    DomatTheme {
        PaymentCardOption(brand = CardBrand.Mastercard, last4 = "4821", selected = true, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PaymentCardOptionPreview() {
    DomatTheme {
        PaymentCardOption(brand = CardBrand.Visa, last4 = "1234", selected = false, onClick = {})
    }
}
