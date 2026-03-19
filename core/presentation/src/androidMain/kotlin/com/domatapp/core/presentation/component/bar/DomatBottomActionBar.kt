package com.domatapp.core.presentation.component.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatBottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colorResource(DomatColors.borderLight))
            .background(colorResource(DomatColors.surfaceDefault))
            .padding(start = 16.dp, end = 16.dp, top = 17.dp, bottom = 32.dp),
    ) {
        content()
    }
}
