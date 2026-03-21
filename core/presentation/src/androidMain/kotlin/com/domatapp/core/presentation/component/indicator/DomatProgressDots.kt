package com.domatapp.core.presentation.component.indicator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
fun DomatProgressDots(
    totalDots: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier,
) {
    val primaryColor = colorResource(R.color.malachite)
    val inactiveColor = colorResource(R.color.slate_100)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalDots) { index ->
            val isActive = index == activeIndex

            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 6.dp,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
                label = "dot_width_$index",
            )
            val color by animateColorAsState(
                targetValue = if (isActive) primaryColor else inactiveColor,
                animationSpec = tween(durationMillis = 200),
                label = "dot_color_$index",
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(6.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(color),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatProgressDotsPreview() {
    DomatTheme {
        DomatProgressDots(totalDots = 5, activeIndex = 2)
    }
}
