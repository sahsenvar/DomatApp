package com.domatapp.core.presentation.component.indicator

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing

@Composable
fun ProgressSteps(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.sp3),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3, Alignment.CenterHorizontally),
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
                            .clip(CircleShape)
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
private fun ProgressStepsPreview() {
    DomatTheme {
        ProgressSteps(totalSteps = 4, currentStep = 1)
    }
}
