package com.domatapp.app.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domatapp.feature.auth.presentation.model.AuthEffect
import com.domatapp.feature.auth.presentation.model.AuthIntent
import com.domatapp.feature.auth.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

private val Primary = Color(0xFF13EC49)
private val BackgroundLight = Color(0xFFF6F8F6)

@Composable
fun AuthScreen(
    onGoogleSignInRequested: suspend () -> String?,
    onNavigateToHome: () -> Unit = {},
    viewModel: AuthViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.LaunchGoogleSignIn -> {
                    val idToken = onGoogleSignInRequested()
                    if (idToken != null) {
                        viewModel.onIntent(AuthIntent.OnGoogleTokenReceived(idToken))
                    } else {
                        viewModel.onIntent(AuthIntent.OnGoogleSignInCancelled)
                    }
                }

                is AuthEffect.NavigateToHome -> onNavigateToHome()
                is AuthEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Hero Image Section
            HeroSection()

            // Content Body
            ContentSection(
                isLoading = state.isLoading,
                isSignInInProgress = state.isGoogleSignInInProgress,
                onGoogleSignInClick = {
                    viewModel.onIntent(AuthIntent.OnGoogleSignInClicked)
                }
            )
        }

        // Snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        // Hero Image
        // Image(
        //     painter = painterResource(R.drawable.auth_hero_tomatoes),
        //     contentDescription = "Fresh vibrant red vine tomatoes",
        //     contentScale = ContentScale.Crop,
        //     modifier = Modifier.fillMaxSize()
        // )

        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Brand Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            // Organic & Local Badge
            Row(
                modifier = Modifier
                    .background(
                        color = Primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = Primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌱",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ORGANIC & LOCAL",
                    color = Primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brand Name
            Text(
                text = """Tuzla
Tomato Co.""",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 40.sp,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@Composable
private fun ContentSection(
    isLoading: Boolean,
    isSignInInProgress: Boolean,
    onGoogleSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Value Proposition
        Text(
            text = "Weekly fresh produce delivery directly to your neighborhood.",
            color = Color(0xFF1E293B),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Location Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "📍", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("Serving: ")
                        }
                        withStyle(
                            SpanStyle(
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("Aydınlı Mah., Tuzla")
                        }
                    },
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Google Sign-In Button
            Button(
                onClick = onGoogleSignInClick,
                enabled = !isLoading && !isSignInInProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0F172A)
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
            ) {
                if (isLoading || isSignInInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF0F172A)
                    )
                } else {
                    GoogleIcon(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terms & Privacy
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                        append("By continuing, you agree to our ")
                    }
                    withStyle(
                        SpanStyle(
                            color = Primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Terms of Service")
                    }
                    withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                        append(" & ")
                    }
                    withStyle(
                        SpanStyle(
                            color = Primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Privacy Policy")
                    }
                    withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                        append(".")
                    }
                },
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Blue (top-right arc area)
        drawPath(
            path = Path().apply {
                moveTo(w * 0.94f, h * 0.51f)
                cubicTo(w * 0.94f, h * 0.48f, w * 0.93f, h * 0.44f, w * 0.93f, h * 0.42f)
                lineTo(w * 0.50f, h * 0.42f)
                lineTo(w * 0.50f, h * 0.59f)
                lineTo(w * 0.75f, h * 0.59f)
                cubicTo(w * 0.73f, h * 0.65f, w * 0.70f, h * 0.70f, w * 0.66f, h * 0.73f)
                lineTo(w * 0.81f, h * 0.84f)
                cubicTo(w * 0.89f, h * 0.76f, w * 0.94f, h * 0.65f, w * 0.94f, h * 0.51f)
            },
            color = Color(0xFF4285F4)
        )

        // Green (bottom-right)
        drawPath(
            path = Path().apply {
                moveTo(w * 0.50f, h * 0.96f)
                cubicTo(w * 0.62f, h * 0.96f, w * 0.73f, h * 0.92f, w * 0.81f, h * 0.84f)
                lineTo(w * 0.66f, h * 0.73f)
                cubicTo(w * 0.62f, h * 0.76f, w * 0.57f, h * 0.77f, w * 0.50f, h * 0.77f)
                cubicTo(w * 0.38f, h * 0.77f, w * 0.28f, h * 0.69f, w * 0.24f, h * 0.59f)
                lineTo(w * 0.09f, h * 0.70f)
                cubicTo(w * 0.17f, h * 0.86f, w * 0.32f, h * 0.96f, w * 0.50f, h * 0.96f)
            },
            color = Color(0xFF34A853)
        )

        // Yellow (bottom-left)
        drawPath(
            path = Path().apply {
                moveTo(w * 0.24f, h * 0.59f)
                cubicTo(w * 0.23f, h * 0.56f, w * 0.23f, h * 0.53f, w * 0.23f, h * 0.50f)
                cubicTo(w * 0.23f, h * 0.47f, w * 0.23f, h * 0.44f, w * 0.24f, h * 0.41f)
                lineTo(w * 0.09f, h * 0.30f)
                cubicTo(w * 0.06f, h * 0.36f, w * 0.04f, h * 0.43f, w * 0.04f, h * 0.50f)
                cubicTo(w * 0.04f, h * 0.57f, w * 0.06f, h * 0.64f, w * 0.09f, h * 0.70f)
                lineTo(w * 0.24f, h * 0.59f)
            },
            color = Color(0xFFFBBC05)
        )

        // Red (top-left)
        drawPath(
            path = Path().apply {
                moveTo(w * 0.50f, h * 0.22f)
                cubicTo(w * 0.57f, h * 0.22f, w * 0.63f, h * 0.25f, w * 0.68f, h * 0.29f)
                lineTo(w * 0.81f, h * 0.16f)
                cubicTo(w * 0.73f, h * 0.09f, w * 0.62f, h * 0.04f, w * 0.50f, h * 0.04f)
                cubicTo(w * 0.32f, h * 0.04f, w * 0.17f, h * 0.14f, w * 0.09f, h * 0.30f)
                lineTo(w * 0.24f, h * 0.41f)
                cubicTo(w * 0.28f, h * 0.31f, w * 0.38f, h * 0.22f, w * 0.50f, h * 0.22f)
            },
            color = Color(0xFFEA4335)
        )
    }
}
