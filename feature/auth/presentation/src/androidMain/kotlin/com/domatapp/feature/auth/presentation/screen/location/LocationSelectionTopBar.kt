package com.domatapp.feature.auth.presentation.screen.location

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.presentation.component.header.DomatScreenHeader
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.domatapp.core.resource.R

@Composable
internal fun LocationSelectionTopBar(
    onBackClick: () -> Unit,
) {
    DomatScreenHeader(
        title = stringResource(R.string.location_selection_title),
        onBackClick = onBackClick,
        backIconPainter = painterResource(R.drawable.ic_arrow_back),
    )
}

@Preview(showBackground = true)
@Composable
private fun LocationSelectionTopBarPreview() {
    DomatTheme {
        LocationSelectionTopBar(onBackClick = {})
    }
}
