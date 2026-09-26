package com.domatapp.feature.checkout.presentation.screen.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.presentation.component.bar.BottomActionBar
import com.domatapp.core.presentation.component.bar.ScreenHeader
import com.domatapp.core.presentation.component.button.ButtonSize
import com.domatapp.core.presentation.component.button.PrimaryButton
import com.domatapp.core.presentation.component.input.ConsentCheckbox
import com.domatapp.core.presentation.component.input.PhoneNumberField
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.c1_cta
import com.domatapp.core.resource.generated.resources.c1_kvkk_label
import com.domatapp.core.resource.generated.resources.c1_kvkk_legal_text
import com.domatapp.core.resource.generated.resources.c1_phone_error
import com.domatapp.core.resource.generated.resources.c1_phone_label
import com.domatapp.core.resource.generated.resources.c1_rationale
import com.domatapp.core.resource.generated.resources.c1_title
import com.domatapp.feature.checkout.presentation.phone.PhoneEntryIntent
import com.domatapp.feature.checkout.presentation.phone.PhoneEntryUiState
import dev.gezgin.core.annotation.Screen
import org.jetbrains.compose.resources.stringResource

/**
 * C1 - Telefon Numarası Girişi (design/screens/C1). Fixed [ScreenHeader], body that fills the
 * remaining height (scrolls when the KVKK text is expanded), fixed [BottomActionBar] riding above
 * the keyboard. Region order follows `card.yaml → regions`.
 */
@Screen(CheckoutGraph.PhoneEntryRoute::class)
@Composable
fun ColumnScope.PhoneEntryScreen(
    uiState: PhoneEntryUiState,
    onIntent: (PhoneEntryIntent) -> Unit,
) {
    ScreenHeader(
        title = stringResource(Res.string.c1_title),
        onBackClick = { onIntent(PhoneEntryIntent.BackClicked) },
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
        Text(
            text = stringResource(Res.string.c1_rationale),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PhoneField(uiState, onIntent)
        ConsentCheckbox(
            label = stringResource(Res.string.c1_kvkk_label),
            checked = uiState.kvkkChecked,
            onCheckedChange = { onIntent(PhoneEntryIntent.KvkkToggled(it)) },
            legalText = stringResource(Res.string.c1_kvkk_legal_text),
            expanded = uiState.kvkkExpanded,
            onExpandToggle = { onIntent(PhoneEntryIntent.KvkkExpandToggled) },
            enabled = uiState.inputsEnabled,
        )
    }
    BottomActionBar(modifier = Modifier.imePadding()) {
        PrimaryButton(
            text = stringResource(Res.string.c1_cta),
            onClick = { onIntent(PhoneEntryIntent.SendCodeClicked) },
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
            enabled = uiState.canSend,
            loading = uiState.isSending,
        )
    }
}

/** The format error is reported on blur (focus gained, then lost) - never while typing. */
@Composable
private fun PhoneField(uiState: PhoneEntryUiState, onIntent: (PhoneEntryIntent) -> Unit) {
    var hadFocus by remember { mutableStateOf(false) }
    PhoneNumberField(
        value = uiState.phoneDigits,
        onValueChange = { onIntent(PhoneEntryIntent.PhoneChanged(it)) },
        label = stringResource(Res.string.c1_phone_label),
        isError = uiState.isPhoneErrorVisible,
        errorText = uiState.errorMessage ?: stringResource(Res.string.c1_phone_error),
        enabled = uiState.inputsEnabled,
        imeAction = ImeAction.Done,
        // IME Done = "Kod Gönder" (the ViewModel shows the format error if the number is invalid).
        keyboardActions = KeyboardActions(onDone = { onIntent(PhoneEntryIntent.SendCodeClicked) }),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focus ->
                if (hadFocus && !focus.isFocused) onIntent(PhoneEntryIntent.PhoneFocusLost)
                hadFocus = focus.isFocused
            },
    )
}

// ── Previews: one per design state (design/screens/C1/states/*.png) ─────────────────────────────

/** Fake states built from `card.yaml → sampleData`. Preview-only. */
private object C1PreviewStates {
    private const val VALID_PHONE = "5321234567"

    val default = PhoneEntryUiState()

    val ready = PhoneEntryUiState(phoneDigits = VALID_PHONE, kvkkChecked = true)

    val phoneInvalid = PhoneEntryUiState(phoneDigits = "53212", showPhoneError = true, kvkkChecked = true)

    val kvkkExpanded = PhoneEntryUiState(kvkkExpanded = true)

    val sending = ready.copy(isSending = true)
}

@Composable
private fun C1Preview(state: PhoneEntryUiState) {
    DomatTheme(darkTheme = false) {
        Column {
            PhoneEntryScreen(uiState = state, onIntent = {})
        }
    }
}

@Preview(name = "C1@default", widthDp = 390, heightDp = 844)
@Composable
private fun C1DefaultPreview() = C1Preview(C1PreviewStates.default)

@Preview(name = "C1@ready", widthDp = 390, heightDp = 844)
@Composable
private fun C1ReadyPreview() = C1Preview(C1PreviewStates.ready)

@Preview(name = "C1@phone-invalid", widthDp = 390, heightDp = 844)
@Composable
private fun C1PhoneInvalidPreview() = C1Preview(C1PreviewStates.phoneInvalid)

@Preview(name = "C1@kvkk-expanded", widthDp = 390, heightDp = 844)
@Composable
private fun C1KvkkExpandedPreview() = C1Preview(C1PreviewStates.kvkkExpanded)

@Preview(name = "C1@sending", widthDp = 390, heightDp = 844)
@Composable
private fun C1SendingPreview() = C1Preview(C1PreviewStates.sending)
