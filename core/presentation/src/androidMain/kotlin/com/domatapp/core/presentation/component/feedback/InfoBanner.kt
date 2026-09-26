package com.domatapp.core.presentation.component.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.domatColors
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.presentation.component.button.TextLink
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.ic_check_circle
import com.domatapp.core.resource.generated.resources.ic_error
import com.domatapp.core.resource.generated.resources.ic_info
import com.domatapp.core.resource.generated.resources.ic_lock
import com.domatapp.core.resource.generated.resources.ic_warning
import org.jetbrains.compose.resources.painterResource

/** Figma `Feedback/InfoBanner` → `tone` variant. Colours: design/components.yaml → `toneTokens`. */
enum class BannerTone { Info, Warning, Error, Success, Neutral }

/**
 * Non-dismissible banner: icon + optional title + body + optional actions.
 *
 * Figma: `Feedback/InfoBanner`. The icon is decorative (`contentDescription = null`); title and
 * body are read together. An [BannerTone.Error] banner is a polite live region so that a screen
 * reader announces it when it appears.
 *
 * @param icon overrides the tone's default icon (Figma `icon` instance swap) - e.g. the lock on
 *   C4's provision explainer. `null` = tone default.
 * @param actions trailing row of [TextLink]s under the body (Figma `actions`).
 */
@Composable
fun InfoBanner(
    tone: BannerTone,
    body: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: Painter? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    val colors = bannerColors(tone)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.container, MaterialTheme.shapes.medium)
            .padding(MaterialTheme.spacing.sp4)
            .then(
                if (tone == BannerTone.Error) {
                    Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                } else {
                    Modifier
                },
            ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3),
    ) {
        Icon(
            painter = icon ?: painterResource(tone.defaultIcon()),
            contentDescription = null,
            tint = colors.icon,
            modifier = Modifier.size(24.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp1),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.content,
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.content,
            )
            if (actions != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp6),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
        }
    }
}

private class BannerColors(val container: Color, val content: Color, val icon: Color)

@Composable
private fun bannerColors(tone: BannerTone): BannerColors = when (tone) {
    BannerTone.Info -> BannerColors(
        container = MaterialTheme.colorScheme.tertiaryContainer,
        content = MaterialTheme.colorScheme.onTertiaryContainer,
        icon = MaterialTheme.colorScheme.onTertiaryContainer,
    )
    BannerTone.Warning -> BannerColors(
        container = MaterialTheme.domatColors.warningContainer,
        content = MaterialTheme.domatColors.onWarningContainer,
        icon = MaterialTheme.domatColors.onWarningContainer,
    )
    BannerTone.Error -> BannerColors(
        container = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer,
        // DESIGN.md: `error` is for icons and borders; error *text* uses on-error-container.
        icon = MaterialTheme.colorScheme.error,
    )
    BannerTone.Success -> BannerColors(
        container = MaterialTheme.domatColors.successContainer,
        content = MaterialTheme.domatColors.onSuccessContainer,
        icon = MaterialTheme.domatColors.success,
    )
    BannerTone.Neutral -> BannerColors(
        container = MaterialTheme.colorScheme.surfaceVariant,
        content = MaterialTheme.colorScheme.onSurfaceVariant,
        icon = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun BannerTone.defaultIcon() = when (this) {
    BannerTone.Info -> Res.drawable.ic_info
    BannerTone.Warning -> Res.drawable.ic_warning
    BannerTone.Error -> Res.drawable.ic_error
    BannerTone.Success -> Res.drawable.ic_check_circle
    BannerTone.Neutral -> Res.drawable.ic_info
}

@Preview(showBackground = true)
@Composable
private fun InfoBannerInfoPreview() {
    DomatTheme {
        InfoBanner(
            tone = BannerTone.Info,
            title = "Bu, bu haftaki 2. siparişin.",
            body = "₺188,00 ayrıca bloke edilecek. İlk siparişinle birlikte #DAL1 altında tek teslimatta buluşacaklar.",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoBannerWarningPreview() {
    DomatTheme {
        InfoBanner(
            tone = BannerTone.Warning,
            body = "Bu ekleme, iptal hakkı kapanmış bir teslimata eklenecek.",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoBannerNeutralLockPreview() {
    DomatTheme {
        InfoBanner(
            tone = BannerTone.Neutral,
            icon = painterResource(Res.drawable.ic_lock),
            body = "Şimdi ₺188,00 kartında bloke edilecek.",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoBannerErrorPreview() {
    DomatTheme {
        InfoBanner(
            tone = BannerTone.Error,
            title = "Ödeme alınamadı",
            body = "Bankan şu anda ödeme isteğini kabul etmiyor.",
            actions = {
                TextLink(text = "Tekrar Dene", onClick = {})
                TextLink(text = "Farklı kart kullan", onClick = {})
            },
        )
    }
}
