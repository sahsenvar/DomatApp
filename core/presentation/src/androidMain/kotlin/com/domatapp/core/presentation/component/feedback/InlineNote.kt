package com.domatapp.core.presentation.component.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.domatColors
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.ic_info
import com.domatapp.core.resource.generated.resources.ic_warning
import org.jetbrains.compose.resources.painterResource

/** Figma `Feedback/InlineNote` → `tone` variant. */
enum class NoteTone { Neutral, Warning }

/**
 * Small helper note with a leading icon, `body-small` (e.g. the KVKK reminder on C4).
 *
 * Figma: `Feedback/InlineNote`. No container - it sits inside a section; the icon is decorative.
 */
@Composable
fun InlineNote(
    text: String,
    modifier: Modifier = Modifier,
    tone: NoteTone = NoteTone.Neutral,
) {
    val color = when (tone) {
        NoteTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        NoteTone.Warning -> MaterialTheme.domatColors.onWarningContainer
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp2),
    ) {
        Icon(
            painter = painterResource(
                when (tone) {
                    NoteTone.Neutral -> Res.drawable.ic_info
                    NoteTone.Warning -> Res.drawable.ic_warning
                },
            ),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InlineNotePreview() {
    DomatTheme {
        InlineNote(text = "Bu bilgiler, kayıt olurken onayladığın KVKK aydınlatma metni kapsamında işlenir.")
    }
}

@Preview(showBackground = true)
@Composable
private fun InlineNoteWarningPreview() {
    DomatTheme {
        InlineNote(text = "Fatura bilgin bir sonraki adımda alınacak.", tone = NoteTone.Warning)
    }
}
