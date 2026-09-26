package com.domatapp.feature.auth.presentation.screen.location

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.presentation.component.bar.ScreenHeader
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.location_selection_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LocationSelectionTopBar(
    onBackClick: () -> Unit,
) {
    ScreenHeader(
        title = stringResource(Res.string.location_selection_title),
        onBackClick = onBackClick,
    )
}

@Preview(showBackground = true)
@Composable
private fun LocationSelectionTopBarPreview() {
    DomatTheme {
        LocationSelectionTopBar(onBackClick = {})
    }
}
