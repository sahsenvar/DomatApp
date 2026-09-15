package com.domatapp.feature.auth.presentation.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.domatapp.core.design.theme.DomatColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme

@Composable
fun LocationCardConnector(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 36.dp)
            .width(2.dp)
            .height(24.dp)
            .background(DomatColors.Slate200),
    )
}

@Preview
@Composable
private fun LocationCardConnectorPreview() {
    DomatTheme {
        LocationCardConnector()
    }
}
