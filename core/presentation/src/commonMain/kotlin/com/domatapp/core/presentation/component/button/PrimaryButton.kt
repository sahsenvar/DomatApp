package com.domatapp.core.presentation.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing

enum class ButtonSize {
    Large,
    Medium,
    Small,
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.Large,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val height = when (size) {
        ButtonSize.Large -> 60.dp
        ButtonSize.Medium -> 56.dp
        ButtonSize.Small -> 36.dp
    }
    val shape = when (size) {
        ButtonSize.Large, ButtonSize.Medium -> MaterialTheme.shapes.medium
        ButtonSize.Small -> MaterialTheme.shapes.small
    }
    val textStyle = when (size) {
        ButtonSize.Large, ButtonSize.Medium -> MaterialTheme.typography.titleLarge
        ButtonSize.Small -> MaterialTheme.typography.labelLarge
    }
    val contentPadding = when (size) {
        ButtonSize.Small -> PaddingValues(horizontal = MaterialTheme.spacing.sp4, vertical = 0.dp)
        else -> ButtonDefaults.ContentPadding
    }

    Button(
        // While loading the button keeps its enabled look but swallows clicks, so a payment/OTP
        // request cannot be fired twice.
        onClick = { if (!loading) onClick() },
        modifier = modifier.height(height),
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = text },
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
            return@Button
        }
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sp2))
        }
        Text(text = text, style = textStyle)
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.sp2))
            trailingContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonLargePreview() {
    DomatTheme {
        PrimaryButton(text = "Devam Et", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonMediumPreview() {
    DomatTheme {
        PrimaryButton(text = "Devam Et", onClick = {}, size = ButtonSize.Medium)
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonSmallPreview() {
    DomatTheme {
        PrimaryButton(text = "Detay", onClick = {}, size = ButtonSize.Small)
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonLoadingPreview() {
    DomatTheme {
        PrimaryButton(text = "Öde", onClick = {}, loading = true)
    }
}
