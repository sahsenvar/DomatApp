package com.domatapp.feature.checkout.presentation.screen.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.presentation.component.bar.BottomActionBar
import com.domatapp.core.presentation.component.bar.ScreenHeader
import com.domatapp.core.presentation.component.button.PrimaryButton
import com.domatapp.core.presentation.component.button.SecondaryButton
import com.domatapp.core.presentation.component.button.TextLink
import com.domatapp.core.presentation.component.data.SummaryRow
import com.domatapp.core.presentation.component.feedback.BannerTone
import com.domatapp.core.presentation.component.feedback.InfoBanner
import com.domatapp.core.presentation.component.feedback.InlineNote
import com.domatapp.core.presentation.component.feedback.NoteTone
import com.domatapp.core.presentation.component.input.ConsentCheckbox
import com.domatapp.core.presentation.component.input.DomatTextField
import com.domatapp.core.presentation.component.layout.FormSection
import com.domatapp.core.presentation.component.payment.CardBrand
import com.domatapp.core.presentation.component.payment.PaymentCardOption
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.c4_back_to_cart
import com.domatapp.core.resource.generated.resources.c4_below_minimum
import com.domatapp.core.resource.generated.resources.c4_cancellation_passed
import com.domatapp.core.resource.generated.resources.c4_card_declined_3x_body
import com.domatapp.core.resource.generated.resources.c4_card_declined_title
import com.domatapp.core.resource.generated.resources.c4_card_expiry
import com.domatapp.core.resource.generated.resources.c4_card_expiry_placeholder
import com.domatapp.core.resource.generated.resources.c4_card_number
import com.domatapp.core.resource.generated.resources.c4_contract_label
import com.domatapp.core.resource.generated.resources.c4_contract_legal_text
import com.domatapp.core.resource.generated.resources.c4_cta_pay
import com.domatapp.core.resource.generated.resources.c4_cvv
import com.domatapp.core.resource.generated.resources.c4_email
import com.domatapp.core.resource.generated.resources.c4_email_helper
import com.domatapp.core.resource.generated.resources.c4_first_name
import com.domatapp.core.resource.generated.resources.c4_kvkk_reminder
import com.domatapp.core.resource.generated.resources.c4_last_name
import com.domatapp.core.resource.generated.resources.c4_marketing_consent
import com.domatapp.core.resource.generated.resources.c4_payment_title
import com.domatapp.core.resource.generated.resources.c4_personal_title
import com.domatapp.core.resource.generated.resources.c4_provision_explainer
import com.domatapp.core.resource.generated.resources.c4_retry
import com.domatapp.core.resource.generated.resources.c4_save_card
import com.domatapp.core.resource.generated.resources.c4_second_order_body
import com.domatapp.core.resource.generated.resources.c4_second_order_title
import com.domatapp.core.resource.generated.resources.c4_summary_title
import com.domatapp.core.resource.generated.resources.c4_summary_total
import com.domatapp.core.resource.generated.resources.c4_title
import com.domatapp.core.resource.generated.resources.c4_use_other_card
import com.domatapp.core.resource.generated.resources.ic_lock
import com.domatapp.feature.checkout.domain.model.Money
import com.domatapp.feature.checkout.domain.model.OrderLine
import com.domatapp.feature.checkout.domain.model.PaymentCardBrand
import com.domatapp.feature.checkout.domain.model.SavedPaymentCard
import com.domatapp.feature.checkout.presentation.format.formatTry
import com.domatapp.feature.checkout.presentation.payment.PaymentErrorUi
import com.domatapp.feature.checkout.presentation.payment.PaymentIntent
import com.domatapp.feature.checkout.presentation.payment.PaymentUiState
import dev.gezgin.core.annotation.Screen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * C4 - Ödeme (design/screens/C4). Fixed [ScreenHeader], scrolling body, fixed [BottomActionBar]
 * that rides above the keyboard. Region order follows `card.yaml → regions` and is binding.
 */
@Screen(CheckoutGraph.PaymentRoute::class)
@Composable
fun ColumnScope.PaymentScreen(
    uiState: PaymentUiState,
    onIntent: (PaymentIntent) -> Unit,
) {
    val cvvFocusRequester = remember { FocusRequester() }
    var cvvFocusRequests by remember { mutableIntStateOf(0) }
    // "PaymentCardOption seçilince CVV alanı odak alır" - only after a user selection, and only
    // once the CVV field for the selected card is in composition.
    LaunchedEffect(cvvFocusRequests, uiState.selectedCardId) {
        if (cvvFocusRequests > 0 && uiState.selectedCardId != null && !uiState.useNewCard) {
            cvvFocusRequester.requestFocus()
        }
    }

    ScreenHeader(
        title = stringResource(Res.string.c4_title),
        onBackClick = { onIntent(PaymentIntent.BackClicked) },
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
        if (uiState.isFirstOrder) {
            PersonalInfoSection(uiState, onIntent)
        }
        if (uiState.showSecondOrderBanner) {
            InfoBanner(
                tone = BannerTone.Info,
                title = stringResource(Res.string.c4_second_order_title, uiState.orderSequenceInWindow),
                body = stringResource(
                    Res.string.c4_second_order_body,
                    uiState.total.formatTry(),
                    uiState.currentDeliveryCode,
                ),
            )
        }
        if (uiState.deliveryCancellationDeadlinePassed) {
            InfoBanner(tone = BannerTone.Warning, body = stringResource(Res.string.c4_cancellation_passed))
        }
        OrderSummarySection(uiState)
        InfoBanner(
            tone = BannerTone.Neutral,
            icon = painterResource(Res.drawable.ic_lock),
            body = stringResource(Res.string.c4_provision_explainer, uiState.total.formatTry()),
        )
        PaymentMethodSection(
            uiState = uiState,
            onIntent = onIntent,
            cvvFocusRequester = cvvFocusRequester,
            onSavedCardClick = { id ->
                onIntent(PaymentIntent.SavedCardSelected(id))
                cvvFocusRequests++
            },
        )
        uiState.paymentError?.let { error -> CardErrorBanner(error, uiState.inputsEnabled, onIntent) }
        ConsentCheckbox(
            label = stringResource(Res.string.c4_contract_label),
            checked = uiState.contractChecked,
            onCheckedChange = { onIntent(PaymentIntent.ContractToggled(it)) },
            legalText = stringResource(Res.string.c4_contract_legal_text),
            expanded = uiState.contractExpanded,
            onExpandToggle = { onIntent(PaymentIntent.ContractExpandToggled) },
            enabled = uiState.inputsEnabled,
        )
    }
    BottomActionBar(modifier = Modifier.imePadding()) {
        if (uiState.belowMinimum) {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3)) {
                InfoBanner(
                    tone = BannerTone.Warning,
                    body = stringResource(Res.string.c4_below_minimum, uiState.minimumAmount.formatTry()),
                )
                SecondaryButton(
                    text = stringResource(Res.string.c4_back_to_cart),
                    onClick = { onIntent(PaymentIntent.BackToCartClicked) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            PrimaryButton(
                text = stringResource(Res.string.c4_cta_pay, uiState.total.formatTry()),
                onClick = { onIntent(PaymentIntent.PayClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canPay,
                loading = uiState.isPaying,
            )
        }
    }
}

@Composable
private fun PersonalInfoSection(uiState: PaymentUiState, onIntent: (PaymentIntent) -> Unit) {
    val nameKeyboard = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Next,
    )
    FormSection(title = stringResource(Res.string.c4_personal_title)) {
        DomatTextField(
            value = uiState.firstName,
            onValueChange = { onIntent(PaymentIntent.FirstNameChanged(it)) },
            label = stringResource(Res.string.c4_first_name),
            enabled = uiState.inputsEnabled,
            keyboardOptions = nameKeyboard,
            modifier = Modifier.fillMaxWidth(),
        )
        DomatTextField(
            value = uiState.lastName,
            onValueChange = { onIntent(PaymentIntent.LastNameChanged(it)) },
            label = stringResource(Res.string.c4_last_name),
            enabled = uiState.inputsEnabled,
            keyboardOptions = nameKeyboard,
            modifier = Modifier.fillMaxWidth(),
        )
        DomatTextField(
            value = uiState.email,
            onValueChange = { onIntent(PaymentIntent.EmailChanged(it)) },
            label = stringResource(Res.string.c4_email),
            supportingText = stringResource(Res.string.c4_email_helper),
            enabled = uiState.inputsEnabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        if (uiState.showMarketingConsent) {
            ConsentCheckbox(
                label = stringResource(Res.string.c4_marketing_consent),
                checked = uiState.marketingConsent,
                onCheckedChange = { onIntent(PaymentIntent.MarketingToggled(it)) },
                enabled = uiState.inputsEnabled,
            )
        }
        InlineNote(text = stringResource(Res.string.c4_kvkk_reminder), tone = NoteTone.Neutral)
    }
}

@Composable
private fun OrderSummarySection(uiState: PaymentUiState) {
    FormSection(title = stringResource(Res.string.c4_summary_title)) {
        uiState.items.forEach { line ->
            SummaryRow(
                label = line.productName,
                detail = line.quantityLabel,
                amount = line.lineTotal.formatTry(),
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SummaryRow(
            label = stringResource(Res.string.c4_summary_total),
            amount = uiState.total.formatTry(),
            emphasis = true,
        )
    }
}

@Composable
private fun PaymentMethodSection(
    uiState: PaymentUiState,
    onIntent: (PaymentIntent) -> Unit,
    cvvFocusRequester: FocusRequester,
    onSavedCardClick: (String) -> Unit,
) {
    val numberKeyboard = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next)
    val cvvKeyboard = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done)

    FormSection(title = stringResource(Res.string.c4_payment_title)) {
        if (uiState.showSavedCards) {
            uiState.savedCards.forEach { card ->
                val selected = !uiState.useNewCard && card.id == uiState.selectedCardId
                PaymentCardOption(
                    brand = card.brand.toCardBrand(),
                    last4 = card.last4,
                    selected = selected,
                    onClick = { onSavedCardClick(card.id) },
                    enabled = uiState.inputsEnabled,
                )
                if (selected) {
                    DomatTextField(
                        value = uiState.cvv,
                        onValueChange = { onIntent(PaymentIntent.CvvChanged(it)) },
                        label = stringResource(Res.string.c4_cvv),
                        enabled = uiState.inputsEnabled,
                        keyboardOptions = cvvKeyboard,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(cvvFocusRequester),
                    )
                }
            }
        }
        if (uiState.showUseOtherCardLink) {
            TextLink(
                text = stringResource(Res.string.c4_use_other_card),
                onClick = { onIntent(PaymentIntent.UseOtherCardClicked) },
                enabled = uiState.inputsEnabled,
            )
        }
        if (uiState.showNewCardForm) {
            DomatTextField(
                value = uiState.cardNumber,
                onValueChange = { onIntent(PaymentIntent.CardNumberChanged(it)) },
                label = stringResource(Res.string.c4_card_number),
                enabled = uiState.inputsEnabled,
                keyboardOptions = numberKeyboard,
                visualTransformation = CardNumberTransformation,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3)) {
                DomatTextField(
                    value = uiState.cardExpiry,
                    onValueChange = { onIntent(PaymentIntent.ExpiryChanged(it)) },
                    label = stringResource(Res.string.c4_card_expiry),
                    placeholder = stringResource(Res.string.c4_card_expiry_placeholder),
                    enabled = uiState.inputsEnabled,
                    keyboardOptions = numberKeyboard,
                    visualTransformation = CardExpiryTransformation,
                    modifier = Modifier.weight(1f),
                )
                DomatTextField(
                    value = uiState.cvv,
                    onValueChange = { onIntent(PaymentIntent.CvvChanged(it)) },
                    label = stringResource(Res.string.c4_cvv),
                    enabled = uiState.inputsEnabled,
                    keyboardOptions = cvvKeyboard,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.weight(1f),
                )
            }
            if (uiState.showSaveCardOption) {
                ConsentCheckbox(
                    label = stringResource(Res.string.c4_save_card),
                    checked = uiState.saveCard,
                    onCheckedChange = { onIntent(PaymentIntent.SaveCardToggled(it)) },
                    enabled = uiState.inputsEnabled,
                )
            }
        }
    }
}

@Composable
private fun CardErrorBanner(
    error: PaymentErrorUi,
    enabled: Boolean,
    onIntent: (PaymentIntent) -> Unit,
) {
    InfoBanner(
        tone = BannerTone.Error,
        title = stringResource(Res.string.c4_card_declined_title),
        body = if (error.isRepeated) stringResource(Res.string.c4_card_declined_3x_body) else error.userMessage,
        actions = {
            val retry: @Composable () -> Unit = {
                TextLink(
                    text = stringResource(Res.string.c4_retry),
                    onClick = { onIntent(PaymentIntent.RetryClicked) },
                    enabled = enabled,
                )
            }
            val useOtherCard: @Composable () -> Unit = {
                TextLink(
                    text = stringResource(Res.string.c4_use_other_card),
                    onClick = { onIntent(PaymentIntent.UseOtherCardClicked) },
                    enabled = enabled,
                )
            }
            // After the 3rd consecutive decline a different card is put forward; retrying the
            // same card stays possible.
            if (error.isRepeated) {
                useOtherCard()
                retry()
            } else {
                retry()
                useOtherCard()
            }
        },
    )
}

private fun PaymentCardBrand.toCardBrand(): CardBrand = when (this) {
    PaymentCardBrand.Mastercard -> CardBrand.Mastercard
    PaymentCardBrand.Visa -> CardBrand.Visa
    PaymentCardBrand.Troy -> CardBrand.Troy
    PaymentCardBrand.Unknown -> CardBrand.Unknown
}

// ── Previews: one per design state (design/screens/C4/states/*.png) ─────────────────────────────

/** Fake states built from `card.yaml → sampleData` / `structure.json`. Preview-only. */
private object C4PreviewStates {
    private val savedCard = SavedPaymentCard(id = "card-1", brand = PaymentCardBrand.Mastercard, last4 = "4821")
    private val items = listOf(
        OrderLine("Domates", "3 kg", Money.ofLira(96)),
        OrderLine("Salatalık", "2 kg", Money.ofLira(54)),
        OrderLine("Kuru Soğan", "2 kg", Money.ofLira(38)),
    )

    val firstOrder = PaymentUiState(
        isFirstOrder = true,
        firstName = "Elif",
        lastName = "Yılmaz",
        email = "elif.yilmaz@ornek.com",
        items = items,
        total = Money.ofLira(188),
        savedCardEnabled = true,
        cardNumber = "5528790012344821",
        cardExpiry = "0828",
        cvv = "123",
        minimumAmount = Money.ofLira(60),
    )

    val returning = PaymentUiState(
        items = items,
        total = Money.ofLira(188),
        savedCardEnabled = true,
        savedCards = listOf(savedCard),
        selectedCardId = savedCard.id,
        cvv = "123",
        contractChecked = true,
        minimumAmount = Money.ofLira(60),
    )

    val secondOrder = returning.copy(orderSequenceInWindow = 2, currentDeliveryCode = "#DAL1")

    val cancellationPassed = secondOrder.copy(deliveryCancellationDeadlinePassed = true)

    val belowMinimum = returning.copy(
        items = listOf(
            OrderLine("Domates", "1 kg", Money.ofLira(32)),
            OrderLine("Maydanoz", "1 demet", Money.ofLira(10)),
        ),
        total = Money.ofLira(42),
        contractChecked = false,
        belowMinimum = true,
    )

    val cardDeclined = returning.copy(
        paymentError = PaymentErrorUi(
            userMessage = "Bankan şu anda ödeme isteğini kabul etmiyor. Kartından para çekilmedi; bilgilerin korunuyor.",
            consecutiveFailures = 1,
        ),
    )

    val cardDeclined3x = cardDeclined.copy(
        paymentError = cardDeclined.paymentError?.copy(consecutiveFailures = PaymentErrorUi.REPEATED_FAILURE_THRESHOLD),
    )

    val paying = returning.copy(isPaying = true)
}

@Composable
private fun C4Preview(state: PaymentUiState) {
    DomatTheme(darkTheme = false) {
        Column {
            PaymentScreen(uiState = state, onIntent = {})
        }
    }
}

@Preview(name = "C4@first-order", widthDp = 390, heightDp = 1416)
@Composable
private fun C4FirstOrderPreview() = C4Preview(C4PreviewStates.firstOrder)

@Preview(name = "C4@returning", widthDp = 390, heightDp = 982)
@Composable
private fun C4ReturningPreview() = C4Preview(C4PreviewStates.returning)

@Preview(name = "C4@second-order", widthDp = 390, heightDp = 1114)
@Composable
private fun C4SecondOrderPreview() = C4Preview(C4PreviewStates.secondOrder)

@Preview(name = "C4@cancellation-passed", widthDp = 390, heightDp = 1222)
@Composable
private fun C4CancellationPassedPreview() = C4Preview(C4PreviewStates.cancellationPassed)

@Preview(name = "C4@below-minimum", widthDp = 390, heightDp = 1038)
@Composable
private fun C4BelowMinimumPreview() = C4Preview(C4PreviewStates.belowMinimum)

@Preview(name = "C4@card-declined", widthDp = 390, heightDp = 1146)
@Composable
private fun C4CardDeclinedPreview() = C4Preview(C4PreviewStates.cardDeclined)

@Preview(name = "C4@card-declined-3x", widthDp = 390, heightDp = 1126)
@Composable
private fun C4CardDeclined3xPreview() = C4Preview(C4PreviewStates.cardDeclined3x)

/** Not a separate Figma frame: `isPaying` → PrimaryButton loading, inputs disabled. */
@Preview(name = "C4@paying", widthDp = 390, heightDp = 982)
@Composable
private fun C4PayingPreview() = C4Preview(C4PreviewStates.paying)

@Preview(name = "C4@first-order · dark", widthDp = 390, heightDp = 1416)
@Composable
private fun C4FirstOrderDarkPreview() {
    DomatTheme(darkTheme = true) {
        Column {
            PaymentScreen(uiState = C4PreviewStates.firstOrder, onIntent = {})
        }
    }
}
