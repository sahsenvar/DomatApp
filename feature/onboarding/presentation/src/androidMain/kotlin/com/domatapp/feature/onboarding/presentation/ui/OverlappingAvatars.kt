package com.domatapp.feature.onboarding.presentation.ui

import androidx.compose.foundation.layout.Box
import com.domatapp.core.resource.generated.resources.ic_person
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.generated.resources.Res

@Composable
internal fun OverlappingAvatars() {
    val primary20 = MaterialTheme.colorScheme.primaryContainer
    val primary30 = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .width(48.dp + 48.dp + 48.dp + 48.dp - 16.dp - 16.dp - 16.dp)
            .height(48.dp),
    ) {
        PersonAvatarCircle(icon = Res.drawable.ic_person, backgroundColor = primary20, offsetX = 0.dp)
        PersonAvatarCircle(icon = Res.drawable.ic_person, backgroundColor = primary30, offsetX = 32.dp)
        PersonAvatarCircle(icon = Res.drawable.ic_person, backgroundColor = primary30, offsetX = 64.dp)
        // Dolu yeşil zeminde açık renk kontrast vermez (≈1,4:1) → onPrimary.
        PersonAvatarCircle(icon = Res.drawable.ic_person, backgroundColor = primary, offsetX = 96.dp, tint = MaterialTheme.colorScheme.onPrimary)
    }
}

@Preview(showBackground = true)
@Composable
private fun OverlappingAvatarsPreview() {
    DomatTheme {
        OverlappingAvatars()
    }
}
