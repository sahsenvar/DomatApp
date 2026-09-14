package com.domatapp.feature.onboarding.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
internal fun PersonAvatarCircle(@DrawableRes icon: Int, backgroundColor: Color, offsetX: Dp) {
    Box(
        modifier = Modifier
            .offset(x = offsetX)
            .size(48.dp)
            .border(4.dp, Color.White, CircleShape)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonAvatarCirclePreview() {
    DomatTheme {
        PersonAvatarCircle(
            icon = R.drawable.ic_person_community,
            backgroundColor = colorResource(R.color.malachite_20),
            offsetX = 0.dp,
        )
    }
}
