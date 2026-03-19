package com.domatapp.feature.onboarding.presentation.screen

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
import com.domatapp.core.presentation.component.button.DomatPrimaryMediumButton
import com.domatapp.core.presentation.component.indicator.DomatProgressDots

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
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DomatProgressDots(
            totalDots = uiModel.totalDots,
            activeIndex = uiModel.activeDotIndex,
        )
        DomatPrimaryMediumButton(
            text = uiModel.buttonText,
            onClick = onContinue,
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
