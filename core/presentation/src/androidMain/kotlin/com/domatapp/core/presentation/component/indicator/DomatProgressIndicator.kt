package com.domatapp.core.presentation.component.indicator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.design.theme.DomatTheme
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatProgressDots(
    totalDots: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier,
) {
    val primaryColor = colorResource(DomatColors.primary)
    val inactiveColor = colorResource(DomatColors.borderLight)

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

@Composable
fun DomatProgressSteps(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    val primaryColor = colorResource(DomatColors.primary)
    val inactiveColor = colorResource(DomatColors.borderDefault)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            when {
                index < currentStep -> Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(primaryColor),
                )
                index == currentStep -> {
                    val width by animateDpAsState(
                        targetValue = 32.dp,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
                        label = "step_width_$index",
                    )
                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(6.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(primaryColor),
                    )
                }
                else -> Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(inactiveColor),
                )
            }
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

@Preview(showBackground = true)
@Composable
private fun DomatProgressStepsPreview() {
    DomatTheme {
        DomatProgressSteps(totalSteps = 4, currentStep = 1)
    }
}
