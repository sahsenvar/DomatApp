package com.domatapp.core.presentation.component.input

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.domatapp.core.design.theme.DomatColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme

@Composable
fun DomatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String = "",
    isError: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = DomatColors.Slate400) }
        } else null,
        label = if (label.isNotEmpty()) {
            { Text(label, style = MaterialTheme.typography.bodyMedium) }
        } else null,
        enabled = enabled,
        isError = isError,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DomatColors.Malachite,
            unfocusedBorderColor = DomatColors.Slate200,
            errorBorderColor = DomatColors.Red500,
            disabledBorderColor = DomatColors.Slate100,
            focusedTextColor = DomatColors.Slate900,
            unfocusedTextColor = DomatColors.Slate900,
            disabledTextColor = DomatColors.CoolGray400,
            errorTextColor = DomatColors.Slate900,
            cursorColor = DomatColors.Malachite,
            errorCursorColor = DomatColors.Red500,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
    )
}

@Preview
@Composable
private fun DomatTextFieldPreview() {
    DomatTheme {
        DomatTextField(
            value = "Örnek metin",
            onValueChange = {},
            placeholder = "Giriniz",
            label = "Ad",
        )
    }
}
