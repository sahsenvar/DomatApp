package com.domatapp.feature.onboarding.presentation.ui

import com.domatapp.core.design.theme.spacing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.img_welcome_neighborhood
import com.domatapp.core.resource.generated.resources.onboarding_image_neighborhood_desc
import com.domatapp.core.resource.generated.resources.onboarding_welcome_body
import com.domatapp.core.resource.generated.resources.onboarding_welcome_title_highlight
import com.domatapp.core.resource.generated.resources.onboarding_welcome_title_line1
import com.domatapp.core.resource.generated.resources.onboarding_welcome_title_line3
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingWelcomePageContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.sp4),
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sp8))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.img_welcome_neighborhood),
                contentDescription = stringResource(Res.string.onboarding_image_neighborhood_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sp8))

        Text(
            text = buildAnnotatedString {
                append(stringResource(Res.string.onboarding_welcome_title_line1))
                append("\n")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(stringResource(Res.string.onboarding_welcome_title_highlight))
                }
                append("\n")
                append(stringResource(Res.string.onboarding_welcome_title_line3))
            },
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sp4))

        Text(
            text = stringResource(Res.string.onboarding_welcome_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingWelcomePageContentPreview() {
    DomatTheme {
        OnboardingWelcomePageContent()
    }
}
