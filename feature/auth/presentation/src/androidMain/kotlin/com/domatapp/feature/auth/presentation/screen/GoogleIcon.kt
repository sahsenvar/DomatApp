package com.domatapp.feature.auth.presentation.screen

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import androidx.compose.ui.res.painterResource
import com.domatapp.core.resource.R

@Composable
internal fun GoogleIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_google),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun GoogleIconPreview() {
    DomatTheme {
        GoogleIcon(modifier = Modifier.size(24.dp))
    }
}
