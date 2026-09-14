package com.domatapp.core.presentation.component.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colorResource(R.color.slate_100))
            .background(colorResource(R.color.white))
            .padding(start = 16.dp, end = 16.dp, top = 17.dp, bottom = 32.dp),
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomActionBarPreview() {
    DomatTheme {
        BottomActionBar {
            Text(text = "İçerik")
        }
    }
}
