package com.domatapp.core.presentation.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.ic_check
import com.domatapp.core.resource.generated.resources.ic_expand_more
import org.jetbrains.compose.resources.painterResource

/**
 * Closed-list picker. Figma: `Input/Dropdown` (state = empty | closed | open).
 * The anchor IS a read-only [DomatTextField] (same outline, floating label, spec) with a trailing
 * expand icon; the menu is a surface (radius md) with 48 dp items, the selected one on primary-container
 * with a check, then an optional divider + [footerOption] ("Listede bulamadım") in label-large tertiary.
 * Replaces feature/auth `InputDropdown` (status: promote).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomatDropdown(
    label: String,
    items: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    footerOption: String? = null,
    onFooterOptionSelected: () -> Unit = {},
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        DomatTextField(
            value = selectedItem.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = label,
            isError = isError,
            enabled = enabled,
            trailingIcon = { ExpandIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.medium,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            DropdownMenuContent(
                items = items,
                selectedItem = selectedItem,
                footerOption = footerOption,
                onItemSelected = { onItemSelected(it); expanded = false },
                onFooterOptionSelected = { onFooterOptionSelected(); expanded = false },
            )
        }
    }
}

@Composable
private fun ExpandIcon(expanded: Boolean) {
    Icon(
        painter = painterResource(Res.drawable.ic_expand_more),
        contentDescription = null,
        modifier = Modifier.size(24.dp).rotate(if (expanded) 180f else 0f),
    )
}

/** Menu rows. Shared by the real popup and the static open-state preview (popups are not captured). */
@Composable
private fun DropdownMenuContent(
    items: List<String>,
    selectedItem: String?,
    footerOption: String?,
    onItemSelected: (String) -> Unit,
    onFooterOptionSelected: () -> Unit,
) {
    items.forEach { item ->
        val selected = item == selectedItem
        DropdownMenuItem(
            text = { Text(item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) },
            onClick = { onItemSelected(item) },
            trailingIcon = if (selected) {
                {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                null
            },
            modifier = Modifier.background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.sp4),
        )
    }
    if (footerOption != null) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DropdownMenuItem(
            text = { Text(footerOption, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary) },
            onClick = onFooterOptionSelected,
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.sp4),
        )
    }
}

/**
 * Static rendering of the open state (anchor + menu panel in normal layout) for previews and
 * the design comparison: Robolectric screenshots do not include popup windows.
 */
@Composable
fun DomatDropdownOpenPreviewLayout(
    label: String,
    items: List<String>,
    selectedItem: String?,
    modifier: Modifier = Modifier,
    footerOption: String? = null,
) {
    // While the menu is open the anchor holds focus (primary border, tertiary label). A preview cannot
    // focus, so emit the focus interaction instead.
    val focused = remember { MutableInteractionSource() }
    LaunchedEffect(focused) { focused.emit(FocusInteraction.Focus()) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DomatTextField(
            value = selectedItem.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = label,
            trailingIcon = { ExpandIcon(expanded = true) },
            modifier = Modifier.fillMaxWidth(),
            interactionSource = focused,
        )
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp) {
            Column(Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.sp2)) {
                DropdownMenuContent(items, selectedItem, footerOption, {}, {})
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatDropdownPreview() {
    DomatTheme { DomatDropdown(label = "Blok", items = listOf("A Blok", "B Blok"), selectedItem = "A Blok", onItemSelected = {}) }
}

@Preview(showBackground = true)
@Composable
private fun DomatDropdownOpenPreview() {
    DomatTheme {
        DomatDropdownOpenPreviewLayout(
            label = "Blok",
            items = listOf("A Blok", "B Blok", "C Blok"),
            selectedItem = "A Blok",
            footerOption = "Listede bulamadım",
        )
    }
}
