package com.domatapp.core.presentation.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.design.theme.DomatTheme
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(DomatColors.primary),
            contentColor = colorResource(DomatColors.textPrimary),
            disabledContainerColor = colorResource(DomatColors.surfaceMuted),
            disabledContentColor = colorResource(DomatColors.textDisabled),
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
        Text(text = text, style = MaterialTheme.typography.titleLarge)
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Composable
fun DomatPrimaryMediumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(DomatColors.primary),
            contentColor = colorResource(DomatColors.textPrimary),
            disabledContainerColor = colorResource(DomatColors.surfaceMuted),
            disabledContentColor = colorResource(DomatColors.textDisabled),
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun DomatPrimarySmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(DomatColors.primary),
            contentColor = colorResource(DomatColors.textPrimary),
            disabledContainerColor = colorResource(DomatColors.surfaceMuted),
            disabledContentColor = colorResource(DomatColors.textDisabled),
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DomatSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(DomatColors.surfaceDark),
            contentColor = colorResource(DomatColors.textInverse),
            disabledContainerColor = colorResource(DomatColors.surfaceMuted),
            disabledContentColor = colorResource(DomatColors.textDisabled),
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun DomatGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colorResource(DomatColors.surfaceDefault),
            contentColor = colorResource(DomatColors.textPrimary),
            disabledContentColor = colorResource(DomatColors.textDisabled),
        ),
        border = BorderStroke(
            width = 0.dp,
            color = colorResource(DomatColors.surfaceDefault),
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

enum class DomatIconButtonSize { Large, Medium }

@Composable
fun DomatIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: DomatIconButtonSize = DomatIconButtonSize.Large,
) {
    val sizeDp = when (size) {
        DomatIconButtonSize.Large -> 48.dp
        DomatIconButtonSize.Medium -> 40.dp
    }
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(sizeDp),
        enabled = enabled,
        shape = RoundedCornerShape(percent = 50),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = colorResource(DomatColors.surfaceMuted),
            contentColor = colorResource(DomatColors.textPrimary),
            disabledContainerColor = colorResource(DomatColors.borderDefault),
            disabledContentColor = colorResource(DomatColors.textDisabled),
        ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatPrimaryButtonPreview() {
    DomatTheme {
        DomatPrimaryButton(text = "Devam Et", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatPrimaryMediumButtonPreview() {
    DomatTheme {
        DomatPrimaryMediumButton(text = "Devam Et", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatPrimarySmallButtonPreview() {
    DomatTheme {
        DomatPrimarySmallButton(text = "Detay", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatSecondaryButtonPreview() {
    DomatTheme {
        DomatSecondaryButton(text = "İptal", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatGhostButtonPreview() {
    DomatTheme {
        DomatGhostButton(text = "Atla", onClick = {})
    }
}

