package com.domatapp.feature.auth.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource
import domatapp.feature.auth.presentation.generated.resources.Res
import domatapp.feature.auth.presentation.generated.resources.ic_google
import org.jetbrains.compose.resources.painterResource
import com.domatapp.core.navigation.Route.AuthRoute
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.feature.auth.presentation.model.AuthIntent
import com.domatapp.feature.auth.presentation.model.AuthUiState

@NavigationScreen(AuthRoute.AuthScreen::class)
@Composable
fun ColumnScope.AuthPage(
    uiState: AuthUiState,
    onIntent: (AuthIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(DomatColors.surfaceDefault)),
    ) {
        HeroSection()
        ContentSection(
            isLoading = uiState.isLoading,
            isSignInInProgress = uiState.isGoogleSignInInProgress,
            onGoogleSignInClick = { onIntent(AuthIntent.OnGoogleSignInClicked) },
        )
    }
}

@Composable
private fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(442.dp),
    ) {
        // Hero image placeholder — replace with painterResource(Res.drawable.img_auth_hero)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(colorResource(DomatColors.heroGradientStart), colorResource(DomatColors.heroGradientEnd)),
                    )
                ),
        )

        // Dark gradient overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, colorResource(DomatColors.overlayBlack55)),
                        startY = 150f,
                    )
                ),
        )

        // Brand content — bottom-start aligned
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // "taze & Yerel" badge
            Row(
                modifier = Modifier
                    .background(color = colorResource(DomatColors.primary).copy(alpha = 0.2f), shape = CircleShape)
                    .border(width = 1.dp, color = colorResource(DomatColors.primary), shape = CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = "\uD83C\uDF31", style = MaterialTheme.typography.labelSmall)
                Text(
                    text = "taze & Yerel",
                    color = colorResource(DomatColors.primary),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Text(
                text = "DomatApp",
                color = colorResource(DomatColors.textInverse),
                style = MaterialTheme.typography.displayMedium,
            )
        }
    }
}

@Composable
private fun ContentSection(
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
            text = "Haftalık olarak en taze ürünleri\nsitenize/kapınıza kadar getiriyoruz",
            color = colorResource(DomatColors.textPrimary),
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
                containerColor = colorResource(DomatColors.surfaceDefault),
                contentColor = colorResource(DomatColors.textPrimary),
                disabledContainerColor = colorResource(DomatColors.surfaceMuted),
                disabledContentColor = colorResource(DomatColors.textDisabled),
            ),
            border = BorderStroke(width = 1.dp, color = colorResource(DomatColors.borderDefault)),
        ) {
            if (isLoading || isSignInInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = colorResource(DomatColors.textPrimary),
                )
            } else {
                GoogleIcon(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Google ile Devam Et",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Devam ederek, Hizmet Şartlarımızı ve\nGizlilik Politikamızı kabul etmiş olursunuz.",
            color = colorResource(DomatColors.textMuted),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(Res.drawable.ic_google),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthPagePreview() = Column {
    AuthPage(
        uiState = AuthUiState(isLoading = false, isGoogleSignInInProgress = false),
        onIntent = {},
    )
}
