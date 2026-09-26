package com.domatapp.feature.auth.presentation.screen.login

import com.domatapp.core.design.theme.spacing
import androidx.compose.foundation.Image
import com.domatapp.core.resource.generated.resources.ic_eco
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.navigation.AuthGraph
import dev.gezgin.core.annotation.Screen
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.app_name
import com.domatapp.core.resource.generated.resources.google_sign_in_button_text
import com.domatapp.core.resource.generated.resources.ic_google
import com.domatapp.core.resource.generated.resources.img_hero_login
import com.domatapp.core.resource.generated.resources.onboarding_login_hero_badge
import com.domatapp.core.resource.generated.resources.onboarding_login_subtitle
import com.domatapp.core.resource.generated.resources.onboarding_login_tos_connector
import com.domatapp.core.resource.generated.resources.onboarding_login_tos_link1
import com.domatapp.core.resource.generated.resources.onboarding_login_tos_link2
import com.domatapp.core.resource.generated.resources.onboarding_login_tos_prefix
import com.domatapp.core.resource.generated.resources.onboarding_login_tos_suffix
import com.domatapp.feature.auth.presentation.login.LoginIntent
import com.domatapp.feature.auth.presentation.login.LoginUiState
import com.domatapp.feature.auth.presentation.screen.component.GoogleSignInButton
import com.domatapp.feature.auth.presentation.screen.component.IconBadge
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Screen(AuthGraph.LoginRoute::class)
@Composable
fun ColumnScope.LoginScreen(
    uiState: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
) {
    val heroShape = MaterialTheme.shapes.extraLarge.copy(topStart = CornerSize(0), topEnd = CornerSize(0))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = heroShape,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f),
                )
                .clip(heroShape),
        ) {
            Image(
                painter = painterResource(Res.drawable.img_hero_login),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.5f to Color.Black.copy(alpha = 0.2f),
                                1.0f to Color.Black.copy(alpha = 0.7f),
                            ),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = MaterialTheme.spacing.sp6, end = MaterialTheme.spacing.sp6, bottom = MaterialTheme.spacing.sp8),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp4),
            ) {
                IconBadge(
                    iconPainter = painterResource(Res.drawable.ic_eco),
                    text = stringResource(Res.string.onboarding_login_hero_badge),
                )
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.sp6)
                .padding(top = MaterialTheme.spacing.sp8, bottom = MaterialTheme.spacing.sp4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.onboarding_login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(124.dp))

            GoogleSignInButton(
                onClick = { onIntent(LoginIntent.OnGoogleSignInClicked) },
                iconPainter = painterResource(Res.drawable.ic_google),
                text = stringResource(Res.string.google_sign_in_button_text),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.sp4)
                .padding(top = MaterialTheme.spacing.sp8, bottom = MaterialTheme.spacing.sp6),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = buildAnnotatedString {
                    append(stringResource(Res.string.onboarding_login_tos_prefix))
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(stringResource(Res.string.onboarding_login_tos_link1))
                    }
                    append(stringResource(Res.string.onboarding_login_tos_connector))
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(stringResource(Res.string.onboarding_login_tos_link2))
                    }
                    append(stringResource(Res.string.onboarding_login_tos_suffix))
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    DomatTheme {
        Column {
            LoginScreen(
                uiState = LoginUiState(),
                onIntent = {},
            )
        }
    }
}
