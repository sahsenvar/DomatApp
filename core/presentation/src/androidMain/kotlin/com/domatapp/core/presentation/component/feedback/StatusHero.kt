package com.domatapp.core.presentation.component.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.domatColors
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.ic_check_circle
import com.domatapp.core.resource.generated.resources.ic_error
import com.domatapp.core.resource.generated.resources.ic_schedule
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class StatusTone { Success, Neutral, Error }

/**
 * Large status icon + title + subtitle, centred. Figma: `Feedback/StatusHero`.
 * Spec (design/components.yaml → StatusHero.spec): padding sp8/sp4, gap sp4; 80 dp circle, 40 dp icon;
 * title headline-small, subtitle body-medium on-surface-variant, texts gap sp2.
 */
@Composable
fun StatusHero(
    tone: StatusTone,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val (container, iconTint, icon) = heroStyle(tone)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.sp4, vertical = MaterialTheme.spacing.sp8),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(80.dp).background(container, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, tint = iconTint, modifier = Modifier.size(40.dp))
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp2),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().semantics { heading() },
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private data class HeroStyle(val container: Color, val icon: Color, val drawable: DrawableResource)

@Composable
private fun heroStyle(tone: StatusTone): HeroStyle = when (tone) {
    StatusTone.Success -> HeroStyle(MaterialTheme.domatColors.successContainer, MaterialTheme.domatColors.success, Res.drawable.ic_check_circle)
    StatusTone.Neutral -> HeroStyle(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Res.drawable.ic_schedule)
    StatusTone.Error -> HeroStyle(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, Res.drawable.ic_error)
}

@Preview(showBackground = true)
@Composable
private fun StatusHeroSuccessPreview() {
    DomatTheme { StatusHero(tone = StatusTone.Success, title = "Siparişin alındı!", subtitle = "Cumartesi, kapının önünde.") }
}

@Preview(showBackground = true)
@Composable
private fun StatusHeroNeutralPreview() {
    DomatTheme {
        StatusHero(
            tone = StatusTone.Neutral,
            title = "Bu haftanın siparişleri az önce kapandı.",
            subtitle = "Sepetin cihazında duruyor, gelecek hafta pencere açılınca güncel fiyatlarla devam edebilirsin.",
        )
    }
}
