package com.domatapp.feature.auth.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.domatapp.core.resource.R

@Composable
internal fun ContentSection(
    isLoading: Boolean,
    isSignInInProgress: Boolean,
    onGoogleSignInClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.onboarding_login_subtitle),
            color = colorResource(R.color.slate_900),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onGoogleSignInClick,
            enabled = !isLoading && !isSignInInProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colorResource(R.color.white),
                contentColor = colorResource(R.color.slate_900),
                disabledContainerColor = colorResource(R.color.slate_100),
                disabledContentColor = colorResource(R.color.cool_gray_400),
            ),
            border = BorderStroke(width = 1.dp, color = colorResource(R.color.slate_200)),
        ) {
            if (isLoading || isSignInInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = colorResource(R.color.slate_900),
                )
            } else {
                GoogleIcon(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.google_sign_in_button_text),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.auth_tos_text),
            color = colorResource(R.color.slate_400),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentSectionPreview() {
    DomatTheme {
        ContentSection(
            isLoading = false,
            isSignInInProgress = false,
            onGoogleSignInClick = {},
        )
    }
}
