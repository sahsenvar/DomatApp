package com.domatapp.core.presentation.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
fun DomatLocationCardConnector(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 36.dp)
            .width(2.dp)
            .height(24.dp)
            .background(colorResource(R.color.slate_200)),
    )
}

@Preview(showBackground = true)
@Composable
private fun DomatLocationCardConnectorPreview() {
    DomatTheme {
        DomatLocationCardConnector()
    }
}
