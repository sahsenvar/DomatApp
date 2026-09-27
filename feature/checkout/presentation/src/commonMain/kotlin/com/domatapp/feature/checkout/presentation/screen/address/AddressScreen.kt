package com.domatapp.feature.checkout.presentation.screen.address

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.presentation.component.bar.BottomActionBar
import com.domatapp.core.presentation.component.bar.ScreenHeader
import com.domatapp.core.presentation.component.button.ButtonSize
import com.domatapp.core.presentation.component.button.PrimaryButton
import com.domatapp.core.presentation.component.data.InfoRow
import com.domatapp.core.presentation.component.feedback.InlineNote
import com.domatapp.core.presentation.component.feedback.NoteTone
import com.domatapp.core.presentation.component.input.DomatDropdown
import com.domatapp.core.presentation.component.input.DomatDropdownOpenPreviewLayout
import com.domatapp.core.presentation.component.input.DomatTextField
import com.domatapp.core.presentation.component.layout.FormSection
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.c3_apartment_label
import com.domatapp.core.resource.generated.resources.c3_block_free_label
import com.domatapp.core.resource.generated.resources.c3_block_label
import com.domatapp.core.resource.generated.resources.c3_block_manual_review
import com.domatapp.core.resource.generated.resources.c3_block_not_found
import com.domatapp.core.resource.generated.resources.c3_cta
import com.domatapp.core.resource.generated.resources.c3_form_title
import com.domatapp.core.resource.generated.resources.c3_invoice_edit
import com.domatapp.core.resource.generated.resources.c3_invoice_label
import com.domatapp.core.resource.generated.resources.c3_invoice_pending
import com.domatapp.core.resource.generated.resources.c3_note_helper
import com.domatapp.core.resource.generated.resources.c3_note_label
import com.domatapp.core.resource.generated.resources.c3_note_placeholder
import com.domatapp.core.resource.generated.resources.c3_site_label
import com.domatapp.core.resource.generated.resources.c3_title
import com.domatapp.feature.checkout.domain.model.SiteBlock
import com.domatapp.feature.checkout.presentation.address.AddressIntent
import com.domatapp.feature.checkout.presentation.address.AddressUiState
import dev.gezgin.core.annotation.Screen
import org.jetbrains.compose.resources.stringResource

/**
 * C3 - Adres Ekleme (design/screens/C3). Fixed [ScreenHeader], scrolling body
 * (`InfoRow · site` → `FormSection · address-form` → invoice row), fixed [BottomActionBar].
 */
@Screen(CheckoutGraph.AddressRoute::class)
@Composable
fun ColumnScope.AddressScreen(
    uiState: AddressUiState,
    onIntent: (AddressIntent) -> Unit,
) {
    AddressContent(uiState = uiState, onIntent = onIntent, previewDropdownOpen = false)
}

/**
 * @param previewDropdownOpen previews only: draws the block menu in normal layout via
 *   [DomatDropdownOpenPreviewLayout], because Robolectric screenshots do not capture popups
 *   (design/components.yaml → DomatDropdown.preview). In the app the menu is a popup.
 */
@Composable
private fun ColumnScope.AddressContent(
    uiState: AddressUiState,
    onIntent: (AddressIntent) -> Unit,
    previewDropdownOpen: Boolean,
) {
    ScreenHeader(
        title = stringResource(Res.string.c3_title),
        onBackClick = { onIntent(AddressIntent.BackClicked) },
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.sp4),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp4),
    ) {
        InfoRow(label = stringResource(Res.string.c3_site_label), value = uiState.siteName)
        AddressFormSection(uiState, onIntent, previewDropdownOpen)
        if (uiState.showInvoiceRow) {
            InfoRow(
                label = stringResource(Res.string.c3_invoice_label),
                value = uiState.fullName.orEmpty(),
                trailingAction = stringResource(Res.string.c3_invoice_edit),
                onTrailingActionClick = { onIntent(AddressIntent.EditInvoiceClicked) },
            )
        } else {
            InlineNote(text = stringResource(Res.string.c3_invoice_pending), tone = NoteTone.Neutral)
        }
    }
    BottomActionBar(modifier = Modifier.imePadding()) {
        PrimaryButton(
            text = stringResource(Res.string.c3_cta),
            onClick = { onIntent(AddressIntent.ContinueClicked) },
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
            enabled = uiState.canContinue,
            loading = uiState.isSaving,
        )
    }
}

@Composable
private fun AddressFormSection(
    uiState: AddressUiState,
    onIntent: (AddressIntent) -> Unit,
    previewDropdownOpen: Boolean,
) {
    val notFoundLabel = stringResource(Res.string.c3_block_not_found)
    val blockLabel = stringResource(Res.string.c3_block_label)
    val blockNames = uiState.blocks.map(SiteBlock::name)
    // "Listede bulamadım" shows as the dropdown's value once picked (block-not-found state).
    val selectedName = if (uiState.isBlockNotFound) notFoundLabel else uiState.selectedBlock?.name

    FormSection(title = stringResource(Res.string.c3_form_title)) {
        if (previewDropdownOpen) {
            DomatDropdownOpenPreviewLayout(
                label = blockLabel,
                items = blockNames,
                selectedItem = selectedName,
                footerOption = notFoundLabel,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            DomatDropdown(
                label = blockLabel,
                items = blockNames,
                selectedItem = selectedName,
                onItemSelected = { name ->
                    uiState.blocks.firstOrNull { it.name == name }?.let { onIntent(AddressIntent.BlockSelected(it.id)) }
                },
                footerOption = notFoundLabel,
                onFooterOptionSelected = { onIntent(AddressIntent.BlockNotFoundSelected) },
                enabled = uiState.inputsEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (uiState.isBlockNotFound) {
            DomatTextField(
                value = uiState.blockFreeText,
                onValueChange = { onIntent(AddressIntent.BlockFreeTextChanged(it)) },
                label = stringResource(Res.string.c3_block_free_label),
                enabled = uiState.inputsEnabled,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            InlineNote(text = stringResource(Res.string.c3_block_manual_review), tone = NoteTone.Warning)
        }
        DomatTextField(
            value = uiState.apartmentNo,
            onValueChange = { onIntent(AddressIntent.ApartmentChanged(it)) },
            label = stringResource(Res.string.c3_apartment_label),
            enabled = uiState.inputsEnabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        DomatTextField(
            value = uiState.deliveryNote,
            onValueChange = { onIntent(AddressIntent.NoteChanged(it)) },
            label = stringResource(Res.string.c3_note_label),
            placeholder = stringResource(Res.string.c3_note_placeholder),
            supportingText = stringResource(Res.string.c3_note_helper),
            singleLine = false,
            enabled = uiState.inputsEnabled,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── Previews: one per design state (design/screens/C3/states/*.png) ─────────────────────────────

/** Fake states built from `card.yaml → sampleData`. Preview-only. */
private object C3PreviewStates {
    private val blocks = listOf(
        SiteBlock(id = "block-a", name = "A Blok"),
        SiteBlock(id = "block-b", name = "B Blok"),
        SiteBlock(id = "block-c", name = "C Blok"),
    )

    val defaultFirstOrder = AddressUiState(siteName = "Dalyan Sitesi", blocks = blocks, isFirstOrder = true)

    /** The design shows "A Blok" highlighted in the open menu. */
    val dropdownOpen = defaultFirstOrder.copy(selectedBlockId = "block-a")

    val blockNotFound = defaultFirstOrder.copy(isBlockNotFound = true)

    val filledReturning = AddressUiState(
        siteName = "Dalyan Sitesi",
        blocks = blocks,
        selectedBlockId = "block-b",
        apartmentNo = "12",
        deliveryNote = "Kapı zili çalışmıyor, lütfen arayın.",
        isFirstOrder = false,
        fullName = "Elif Yılmaz",
    )
}

@Composable
private fun C3Preview(state: AddressUiState, dropdownOpen: Boolean = false) {
    DomatTheme(darkTheme = false) {
        Column {
            AddressContent(uiState = state, onIntent = {}, previewDropdownOpen = dropdownOpen)
        }
    }
}

@Preview(name = "C3@default-first-order", widthDp = 390, heightDp = 844)
@Composable
private fun C3DefaultFirstOrderPreview() = C3Preview(C3PreviewStates.defaultFirstOrder)

@Preview(name = "C3@dropdown-open", widthDp = 390, heightDp = 844)
@Composable
private fun C3DropdownOpenPreview() = C3Preview(C3PreviewStates.dropdownOpen, dropdownOpen = true)

@Preview(name = "C3@block-not-found", widthDp = 390, heightDp = 844)
@Composable
private fun C3BlockNotFoundPreview() = C3Preview(C3PreviewStates.blockNotFound)

@Preview(name = "C3@filled-returning", widthDp = 390, heightDp = 844)
@Composable
private fun C3FilledReturningPreview() = C3Preview(C3PreviewStates.filledReturning)
