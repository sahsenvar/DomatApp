package com.domatapp.feature.onboarding.presentation.ui

import androidx.compose.foundation.background
import com.domatapp.core.resource.generated.resources.ic_person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R
import com.domatapp.core.resource.generated.resources.Res
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun PersonAvatarCircle(
    icon: DrawableResource,
    backgroundColor: Color,
    offsetX: Dp,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = Modifier
            .offset(x = offsetX)
            .size(48.dp)
            .border(4.dp, Color.White, CircleShape)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonAvatarCirclePreview() {
    DomatTheme {
        PersonAvatarCircle(
            icon = Res.drawable.ic_person,
            backgroundColor = colorResource(R.color.malachite_20),
            offsetX = 0.dp,
        )
    }
}
