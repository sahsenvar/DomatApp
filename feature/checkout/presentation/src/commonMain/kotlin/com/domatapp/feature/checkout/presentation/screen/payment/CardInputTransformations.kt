package com.domatapp.feature.checkout.presentation.screen.payment

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Display-only formatting for the card inputs; the state keeps raw digits
 * (design/screens/C4/annotations.md → "Klavye ve odak").
 */

/** `5528790012344821` → `5528 7900 1234 4821` (4-4-4-4). */
internal object CardNumberTransformation : VisualTransformation {
    private const val GROUP = 4

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = raw.chunked(GROUP).joinToString(" ")
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + (offset - 1).coerceAtLeast(0) / GROUP

            override fun transformedToOriginal(offset: Int): Int =
                (offset - offset / (GROUP + 1)).coerceIn(0, raw.length)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

/** `0828` → `08/28` (AA/YY). */
internal object CardExpiryTransformation : VisualTransformation {
    private const val MONTH_DIGITS = 2

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = if (raw.length > MONTH_DIGITS) {
            raw.take(MONTH_DIGITS) + "/" + raw.drop(MONTH_DIGITS)
        } else {
            raw
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset > MONTH_DIGITS) offset + 1 else offset

            override fun transformedToOriginal(offset: Int): Int =
                (if (offset > MONTH_DIGITS) offset - 1 else offset).coerceIn(0, raw.length)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
