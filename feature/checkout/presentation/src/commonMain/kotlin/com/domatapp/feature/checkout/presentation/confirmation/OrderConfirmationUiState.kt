package com.domatapp.feature.checkout.presentation.confirmation

import com.domatapp.feature.checkout.domain.model.Money

/**
 * C5 - Sipariş Onay. Fields follow `design/screens/C5/card.yaml → data.uiState`.
 *
 * @property isAddition the order is the 2nd+ in this window: "Eklemen tamamlandı!", no share button.
 * @property communityProgress 0f..1f; for an addition, the combined effect of all orders this window.
 */
data class OrderConfirmationUiState(
    val isAddition: Boolean = false,
    val deliveryCode: String = "",
    val deliveryDay: String = "",
    val blockedAmount: Money = Money.Zero,
    val communityProgress: Float = 0f,
    val communityCaption: String = "",
) {
    /** `SecondaryButton · share-whatsapp` visibleWhen `orderSequenceInWindow == 1`. */
    val showShare: Boolean get() = !isAddition
}
