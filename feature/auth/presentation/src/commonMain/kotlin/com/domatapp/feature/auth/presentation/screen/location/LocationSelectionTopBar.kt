package com.domatapp.feature.auth.presentation.screen.location

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.ic_arrow_back
import com.domatapp.core.resource.generated.resources.location_selection_title
import com.domatapp.feature.auth.presentation.screen.component.ScreenHeader
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LocationSelectionTopBar(
    onBackClick: () -> Unit,
) {
    ScreenHeader(
        title = stringResource(Res.string.location_selection_title),
        onBackClick = onBackClick,
        backIconPainter = painterResource(Res.drawable.ic_arrow_back),
    )
}

@Preview
@Composable
private fun LocationSelectionTopBarPreview() {
    DomatTheme {
        LocationSelectionTopBar(onBackClick = {})
    }
}
