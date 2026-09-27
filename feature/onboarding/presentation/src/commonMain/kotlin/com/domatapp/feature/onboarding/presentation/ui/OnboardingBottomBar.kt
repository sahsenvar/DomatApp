package com.domatapp.feature.onboarding.presentation.ui

import com.domatapp.core.design.theme.spacing
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.presentation.component.button.ButtonSize
import com.domatapp.core.presentation.component.button.PrimaryButton
import com.domatapp.core.presentation.component.indicator.ProgressDots

data class OnboardingBottomBarUiModel(
    val buttonText: String,
    val totalDots: Int,
    val activeDotIndex: Int,
)

@Composable
internal fun OnboardingBottomBar(
    uiModel: OnboardingBottomBarUiModel,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.sp6)
            .padding(top = MaterialTheme.spacing.sp5, bottom = MaterialTheme.spacing.sp10),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp5),
    ) {
        ProgressDots(
            totalDots = uiModel.totalDots,
            activeIndex = uiModel.activeDotIndex,
        )
        PrimaryButton(
            text = uiModel.buttonText,
            onClick = onContinue,
            size = ButtonSize.Medium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingBottomBarPreview() {
    DomatTheme {
        OnboardingBottomBar(
            uiModel = OnboardingBottomBarUiModel(
                buttonText = "Devam Et",
                totalDots = 5,
                activeDotIndex = 0,
            ),
            onContinue = {},
        )
    }
}
