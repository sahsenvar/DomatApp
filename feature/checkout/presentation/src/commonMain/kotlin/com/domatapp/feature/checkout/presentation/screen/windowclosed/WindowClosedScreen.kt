package com.domatapp.feature.checkout.presentation.screen.windowclosed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.presentation.component.bar.BottomActionBar
import com.domatapp.core.presentation.component.button.ButtonSize
import com.domatapp.core.presentation.component.button.PrimaryButton
import com.domatapp.core.presentation.component.feedback.StatusHero
import com.domatapp.core.presentation.component.feedback.StatusTone
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.window_closed_body
import com.domatapp.core.resource.generated.resources.window_closed_cta
import com.domatapp.core.resource.generated.resources.window_closed_title
import com.domatapp.feature.checkout.presentation.windowclosed.WindowClosedIntent
import com.domatapp.feature.checkout.presentation.windowclosed.WindowClosedUiState
import dev.gezgin.core.annotation.Screen
import org.jetbrains.compose.resources.stringResource

/**
 * Pencere Kapandı (design/screens/WindowClosed). No header and no back (`noBack`): a vertically
 * centred neutral [StatusHero] and a [BottomActionBar] with "Pazar'a Dön".
 */
@Screen(CheckoutGraph.WindowClosedRoute::class)
@Composable
fun ColumnScope.WindowClosedScreen(
    @Suppress("UNUSED_PARAMETER") uiState: WindowClosedUiState,
    onIntent: (WindowClosedIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(MaterialTheme.spacing.sp4),
        verticalArrangement = Arrangement.Center,
    ) {
        StatusHero(
            tone = StatusTone.Neutral,
            title = stringResource(Res.string.window_closed_title),
            subtitle = stringResource(Res.string.window_closed_body),
        )
    }
    BottomActionBar {
        PrimaryButton(
            text = stringResource(Res.string.window_closed_cta),
            onClick = { onIntent(WindowClosedIntent.BackToMarketClicked) },
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
        )
    }
}

// ── Preview: design/screens/WindowClosed/states/default.png ─────────────────────────────────────

@Preview(name = "WindowClosed@default", widthDp = 390, heightDp = 844)
@Composable
private fun WindowClosedDefaultPreview() {
    DomatTheme(darkTheme = false) {
        Column {
            WindowClosedScreen(uiState = WindowClosedUiState, onIntent = {})
        }
    }
}
