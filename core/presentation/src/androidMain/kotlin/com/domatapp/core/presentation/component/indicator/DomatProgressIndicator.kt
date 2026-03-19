package com.domatapp.core.presentation.component.indicator

import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatProgressDots(
    totalDots: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalDots) { index ->
            val pillShape = RoundedCornerShape(9999.dp)
            if (index == activeIndex) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(8.dp)
                        .clip(pillShape)
                        .background(colorResource(DomatColors.primary))
                        .animateContentSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colorResource(DomatColors.borderDefault)),
                )
            }
        }
    }
}

@Composable
fun DomatProgressSteps(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val pillShape = RoundedCornerShape(9999.dp)
            when {
                index < currentStep -> Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colorResource(DomatColors.primary)),
                )
                index == currentStep -> Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(6.dp)
                        .clip(pillShape)
                        .background(colorResource(DomatColors.primary))
                        .animateContentSize(),
                )
                else -> Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colorResource(DomatColors.borderDefault)),
                )
            }
        }
    }
}
