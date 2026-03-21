package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
internal fun OverlappingAvatars(primary20: Color, primary30: Color, primary: Color) {
    Box(
        modifier = Modifier
            .width(48.dp + 48.dp + 48.dp + 48.dp - 16.dp - 16.dp - 16.dp)
            .height(48.dp),
    ) {
        PersonAvatarCircle(icon = R.drawable.ic_person_community, backgroundColor = primary20, offsetX = 0.dp)
        PersonAvatarCircle(icon = R.drawable.ic_person_community, backgroundColor = primary30, offsetX = 32.dp)
        PersonAvatarCircle(icon = R.drawable.ic_person_community, backgroundColor = primary30, offsetX = 64.dp)
        PersonAvatarCircle(icon = R.drawable.ic_person_community_white, backgroundColor = primary, offsetX = 96.dp)
    }
}

@Preview(showBackground = true)
@Composable
private fun OverlappingAvatarsPreview() {
    DomatTheme {
        OverlappingAvatars(
            primary20 = colorResource(R.color.malachite_20),
            primary30 = colorResource(R.color.malachite_30),
            primary = colorResource(R.color.malachite),
        )
    }
}
