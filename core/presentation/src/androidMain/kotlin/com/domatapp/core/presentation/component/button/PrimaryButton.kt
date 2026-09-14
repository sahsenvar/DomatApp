package com.domatapp.core.presentation.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

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
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val height = when (size) {
        ButtonSize.Large -> 60.dp
        ButtonSize.Medium -> 56.dp
        ButtonSize.Small -> 36.dp
    }
    val cornerRadius = when (size) {
        ButtonSize.Large, ButtonSize.Medium -> 12.dp
        ButtonSize.Small -> 8.dp
    }
    val textStyle = when (size) {
        ButtonSize.Large, ButtonSize.Medium -> MaterialTheme.typography.titleLarge
        ButtonSize.Small -> MaterialTheme.typography.labelLarge
    }
    val contentPadding = when (size) {
        ButtonSize.Small -> PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        else -> ButtonDefaults.ContentPadding
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        contentPadding = contentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.malachite),
            contentColor = colorResource(R.color.slate_900),
            disabledContainerColor = colorResource(R.color.slate_100),
            disabledContentColor = colorResource(R.color.cool_gray_400),
        ),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = textStyle)
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
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
