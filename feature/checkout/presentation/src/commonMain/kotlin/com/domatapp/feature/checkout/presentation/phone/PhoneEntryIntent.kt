package com.domatapp.feature.checkout.presentation.phone

/**
 * `design/screens/C1/card.yaml → actions`, plus [PhoneFocusLost]: the annotations show the format
 * error when the field loses focus, which needs its own event.
 */
sealed interface PhoneEntryIntent {
    data class PhoneChanged(val digits: String) : PhoneEntryIntent
    data object PhoneFocusLost : PhoneEntryIntent
    data class KvkkToggled(val checked: Boolean) : PhoneEntryIntent
    data object KvkkExpandToggled : PhoneEntryIntent
    data object SendCodeClicked : PhoneEntryIntent
    data object BackClicked : PhoneEntryIntent
}
