package com.domatapp.core.presentation.component.input

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

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
            { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = colorResource(R.color.slate_400)) }
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
            focusedBorderColor = colorResource(R.color.malachite),
            unfocusedBorderColor = colorResource(R.color.slate_200),
            errorBorderColor = colorResource(R.color.red_500),
            disabledBorderColor = colorResource(R.color.slate_100),
            focusedTextColor = colorResource(R.color.slate_900),
            unfocusedTextColor = colorResource(R.color.slate_900),
            disabledTextColor = colorResource(R.color.cool_gray_400),
            errorTextColor = colorResource(R.color.slate_900),
            cursorColor = colorResource(R.color.malachite),
            errorCursorColor = colorResource(R.color.red_500),
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
    )
}

@Preview(showBackground = true)
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
