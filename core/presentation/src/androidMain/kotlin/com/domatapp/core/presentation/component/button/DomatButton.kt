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
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors

@Composable
fun DomatPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DomatColors.Primary,
            contentColor = DomatColors.TextPrimary,
            disabledContainerColor = DomatColors.SurfaceMuted,
            disabledContentColor = DomatColors.TextDisabled,
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
            containerColor = DomatColors.Primary,
            contentColor = DomatColors.TextPrimary,
            disabledContainerColor = DomatColors.SurfaceMuted,
            disabledContentColor = DomatColors.TextDisabled,
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
            containerColor = DomatColors.Primary,
            contentColor = DomatColors.TextPrimary,
            disabledContainerColor = DomatColors.SurfaceMuted,
            disabledContentColor = DomatColors.TextDisabled,
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
            containerColor = DomatColors.SurfaceDark,
            contentColor = DomatColors.TextInverse,
            disabledContainerColor = DomatColors.SurfaceMuted,
            disabledContentColor = DomatColors.TextDisabled,
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
            containerColor = DomatColors.SurfaceDefault,
            contentColor = DomatColors.TextPrimary,
            disabledContentColor = DomatColors.TextDisabled,
        ),
        border = BorderStroke(
            width = 0.dp,
            color = DomatColors.SurfaceDefault,
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
            containerColor = DomatColors.SurfaceMuted,
            contentColor = DomatColors.TextPrimary,
            disabledContainerColor = DomatColors.BorderDefault,
            disabledContentColor = DomatColors.TextDisabled,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
