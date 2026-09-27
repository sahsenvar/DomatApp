package com.domatapp.core.presentation.component.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing

/**
 * The order's effect on the community tier. Figma: `Community/ProgressCard`.
 * Spec: card (surface, 1 outline-variant, radius md, padding sp4, gap sp3); title title-medium;
 * track 8 dp full-round surface-variant with primary indicator; caption body-small on-surface-variant.
 * Should be the same component as Flow B's community indicator (see components.yaml note).
 */
@Composable
fun CommunityProgressCard(
    title: String,
    progress: Float,
    caption: String,
    modifier: Modifier = Modifier,
) {
    val clamped = progress.coerceIn(0f, 1f)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .padding(MaterialTheme.spacing.sp4),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .semantics { progressBarRangeInfo = ProgressBarRangeInfo(clamped, 0f..1f) },
        ) {
            if (clamped > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clamped)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
        Text(text = caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityProgressCardPreview() {
    DomatTheme {
        CommunityProgressCard(
            title = "Topluluğa katkın",
            progress = 0.5f,
            caption = "Bir sonraki kademeye 12 sipariş kaldı — kg fiyatı ₺2 daha düşecek.",
        )
    }
}
