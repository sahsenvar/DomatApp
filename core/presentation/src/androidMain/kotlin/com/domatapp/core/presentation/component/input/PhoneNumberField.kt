package com.domatapp.core.presentation.component.input

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme

/** Turkish mobile numbers: 10 digits after the fixed +90 prefix. */
const val TR_PHONE_DIGITS = 10

/**
 * Fixed `+90` prefix + live `5XX XXX XX XX` formatting. Figma: `Input/PhoneNumber` (a `Input/TextField`
 * instance with `showPrefix = true`). [value] holds digits only; non-digits and overflow are dropped here.
 */
@Composable
fun PhoneNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    DomatTextField(
        value = value,
        onValueChange = { new -> onValueChange(new.filter(Char::isDigit).take(TR_PHONE_DIGITS)) },
        modifier = modifier,
        label = label,
        prefix = "+90",
        isError = isError,
        supportingText = if (isError) errorText else null,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = imeAction),
        keyboardActions = keyboardActions,
        visualTransformation = TrPhoneVisualTransformation,
    )
}

/** 5321234567 → "532 123 45 67". Spaces sit after the 3rd, 6th and 8th digit. */
private object TrPhoneVisualTransformation : VisualTransformation {
    private val breaks = intArrayOf(3, 6, 8)

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val out = buildString {
            digits.forEachIndexed { i, c ->
                if (i in breaks) append(' ')
                append(c)
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset + breaks.count { offset > it }
            override fun transformedToOriginal(offset: Int): Int {
                val spaces = out.take(offset).count { it == ' ' }
                return (offset - spaces).coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneNumberFieldPreview() {
    DomatTheme { PhoneNumberField(value = "5321234567", onValueChange = {}, label = "Telefon numarası") }
}

@Preview(showBackground = true)
@Composable
private fun PhoneNumberFieldErrorPreview() {
    DomatTheme {
        PhoneNumberField(
            value = "53212",
            onValueChange = {},
            label = "Telefon numarası",
            isError = true,
            errorText = "Geçerli bir cep telefonu numarası gir (5XX XXX XX XX).",
        )
    }
}
