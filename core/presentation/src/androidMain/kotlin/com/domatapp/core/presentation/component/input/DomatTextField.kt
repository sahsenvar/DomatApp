package com.domatapp.core.presentation.component.input

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource

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
            { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = colorResource(DomatColors.textMuted)) }
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
            focusedBorderColor = colorResource(DomatColors.primary),
            unfocusedBorderColor = colorResource(DomatColors.borderDefault),
            errorBorderColor = colorResource(DomatColors.error),
            disabledBorderColor = colorResource(DomatColors.borderLight),
            focusedTextColor = colorResource(DomatColors.textPrimary),
            unfocusedTextColor = colorResource(DomatColors.textPrimary),
            disabledTextColor = colorResource(DomatColors.textDisabled),
            errorTextColor = colorResource(DomatColors.textPrimary),
            cursorColor = colorResource(DomatColors.primary),
            errorCursorColor = colorResource(DomatColors.error),
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
    )
}
