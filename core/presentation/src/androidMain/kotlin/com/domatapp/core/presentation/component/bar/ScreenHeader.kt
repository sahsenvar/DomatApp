package com.domatapp.core.presentation.component.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.cd_back_button
import com.domatapp.core.resource.generated.resources.ic_arrow_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Screen top bar: optional back button + title, with a 1 dp `outline-variant` divider below.
 *
 * Figma: `Bar/ScreenHeader`. Promoted from `feature.auth.presentation.screen.component` - the back
 * icon is resolved here (every screen uses the same one) instead of being passed in as a painter.
 *
 * @param onBackClick `null` hides the back button (Figma `showBack = false`).
 * @param bottomContent optional content under the title row, above the divider (e.g. a progress
 *   indicator).
 */
@Composable
fun ScreenHeader(
    title: String,
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    bottomContent: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = MaterialTheme.spacing.sp2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBackClick != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onBackClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = stringResource(Res.string.cd_back_button),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = MaterialTheme.spacing.sp2)
                    .semantics { heading() },
            )
        }
        bottomContent?.invoke()
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderPreview() {
    DomatTheme {
        ScreenHeader(title = "Ödeme", onBackClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenHeaderNoBackPreview() {
    DomatTheme {
        ScreenHeader(title = "Ödeme", onBackClick = null)
    }
}
