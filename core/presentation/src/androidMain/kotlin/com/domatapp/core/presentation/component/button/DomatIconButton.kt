package com.domatapp.core.presentation.component.button

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

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
            containerColor = colorResource(R.color.slate_100),
            contentColor = colorResource(R.color.slate_900),
            disabledContainerColor = colorResource(R.color.slate_200),
            disabledContentColor = colorResource(R.color.cool_gray_400),
        ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatIconButtonPreview() {
    val previewIcon = ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build()
    DomatTheme {
        DomatIconButton(
            icon = previewIcon,
            contentDescription = null,
            onClick = {},
        )
    }
}
