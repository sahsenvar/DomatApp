package com.domatapp.core.presentation.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing

/**
 * One-time code entry drawn as [length] cells. Figma: `Input/OtpCode`.
 * Spec: cells 48×56, radius md, gap sp2; border 1 outline-variant, next cell 2 primary, error 2 error;
 * disabled (verifying) cells surface-variant; digit headline-small.
 * The "next" highlight follows [value].length, not focus, so the design state is reproducible.
 * Autofill: the field is tagged ContentType.SmsOtpCode (Android autofill / iOS oneTimeCode).
 */
@Composable
fun OtpCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    BasicTextField(
        value = value,
        onValueChange = { new -> onValueChange(new.filter(Char::isDigit).take(length)) },
        modifier = modifier.semantics { contentType = ContentType.SmsOtpCode },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = { _ ->
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp2)) {
                repeat(length) { i -> OtpCell(value.getOrNull(i), i, value.length, length, isError, enabled) }
            }
        },
    )
}

@Composable
private fun OtpCell(digit: Char?, index: Int, filled: Int, length: Int, isError: Boolean, enabled: Boolean) {
    val next = enabled && !isError && index == filled && filled < length
    val (borderWidth, borderColor) = when {
        isError -> 2.dp to MaterialTheme.colorScheme.error
        next -> 2.dp to MaterialTheme.colorScheme.primary
        else -> 1.dp to MaterialTheme.colorScheme.outlineVariant
    }
    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 56.dp)
            .background(
                if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.shapes.medium,
            )
            .border(borderWidth, borderColor, MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        if (digit != null) {
            Text(
                text = digit.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtpCodeFieldEmptyPreview() {
    DomatTheme { OtpCodeField(value = "", onValueChange = {}) }
}

@Preview(showBackground = true)
@Composable
private fun OtpCodeFieldErrorPreview() {
    DomatTheme { OtpCodeField(value = "482915", onValueChange = {}, isError = true) }
}
