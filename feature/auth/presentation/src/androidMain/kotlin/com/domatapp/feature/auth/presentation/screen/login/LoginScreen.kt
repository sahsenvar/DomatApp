package com.domatapp.feature.auth.presentation.screen.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.feature.auth.presentation.screen.component.IconBadge
import com.domatapp.feature.auth.presentation.screen.component.GoogleSignInButton
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.domatapp.core.resource.R
import com.domatapp.feature.auth.presentation.login.LoginIntent
import com.domatapp.feature.auth.presentation.login.LoginUiState

@NavigationScreen(Route.AuthRoute.Login::class)
@Composable
fun ColumnScope.LoginScreen(
    uiState: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
) {
    val heroShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.white))
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
                painter = painterResource(R.drawable.img_hero_login),
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
                    .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                IconBadge(
                    iconPainter = painterResource(R.drawable.ic_leaf_badge),
                    text = stringResource(R.string.onboarding_login_hero_badge),
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.onboarding_login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.slate_600),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(124.dp))

            GoogleSignInButton(
                onClick = { onIntent(LoginIntent.OnGoogleSignInClicked) },
                iconPainter = painterResource(R.drawable.ic_google),
                text = stringResource(R.string.google_sign_in_button_text),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.onboarding_login_tos_prefix))
                    withStyle(
                        SpanStyle(
                            color = colorResource(R.color.malachite),
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(stringResource(R.string.onboarding_login_tos_link1))
                    }
                    append(stringResource(R.string.onboarding_login_tos_connector))
                    withStyle(
                        SpanStyle(
                            color = colorResource(R.color.malachite),
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(stringResource(R.string.onboarding_login_tos_link2))
                    }
                    append(stringResource(R.string.onboarding_login_tos_suffix))
                },
                style = MaterialTheme.typography.labelMedium,
                color = colorResource(R.color.slate_400),
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
