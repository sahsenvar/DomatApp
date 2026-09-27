package com.domatapp.feature.checkout.presentation.screen.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.presentation.component.bar.ScreenHeader
import com.domatapp.core.presentation.component.button.TextLink
import com.domatapp.core.presentation.component.feedback.BannerTone
import com.domatapp.core.presentation.component.feedback.InfoBanner
import com.domatapp.core.presentation.component.input.OtpCodeField
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.c2_change_number
import com.domatapp.core.resource.generated.resources.c2_description
import com.domatapp.core.resource.generated.resources.c2_error_wrong_code
import com.domatapp.core.resource.generated.resources.c2_linked_account
import com.domatapp.core.resource.generated.resources.c2_resend
import com.domatapp.core.resource.generated.resources.c2_resend_countdown
import com.domatapp.core.resource.generated.resources.c2_title
import com.domatapp.feature.checkout.presentation.otp.OtpVerifyIntent
import com.domatapp.feature.checkout.presentation.otp.OtpVerifyUiState
import com.domatapp.feature.checkout.presentation.otp.maskTrPhone
import dev.gezgin.core.annotation.Screen
import org.jetbrains.compose.resources.stringResource

/** annotations.md → `verifying`: a 24 dp M3 circular indicator under the cells. */
private val VerifyingIndicatorSize = 24.dp

/**
 * C2 - OTP Doğrulama (design/screens/C2). [ScreenHeader] + body only: there is **no primary
 * button** - the sixth digit starts verification. Order: description → "Numarayı Değiştir" →
 * [OtpCodeField] → error text → "Kodu Tekrar Gönder" + countdown (or the verifying indicator /
 * linked-account banner in their states).
 */
@Screen(CheckoutGraph.OtpVerifyRoute::class)
@Composable
fun ColumnScope.OtpVerifyScreen(
    uiState: OtpVerifyUiState,
    onIntent: (OtpVerifyIntent) -> Unit,
) {
    ScreenHeader(
        title = stringResource(Res.string.c2_title),
        onBackClick = { onIntent(OtpVerifyIntent.BackClicked) },
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.sp4),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp4),
    ) {
        Text(
            text = stringResource(Res.string.c2_description, uiState.maskedPhone),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextLink(
            text = stringResource(Res.string.c2_change_number),
            onClick = { onIntent(OtpVerifyIntent.ChangeNumberClicked) },
        )
        OtpCodeField(
            value = uiState.code,
            onValueChange = { onIntent(OtpVerifyIntent.CodeChanged(it)) },
            length = OtpVerifyUiState.CODE_LENGTH,
            isError = uiState.isError,
            enabled = uiState.codeEnabled,
        )
        if (uiState.isError) {
            // Screen-specific plain text: body-small on-error-container (DESIGN.md: error text colour).
            Text(
                text = stringResource(Res.string.c2_error_wrong_code),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        if (uiState.isVerifying) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(VerifyingIndicatorSize),
                )
            }
        }
        if (uiState.showResend) {
            TextLink(
                text = stringResource(Res.string.c2_resend),
                onClick = { onIntent(OtpVerifyIntent.ResendClicked) },
                enabled = uiState.canResend,
            )
            if (uiState.showCountdown) {
                Text(
                    text = stringResource(Res.string.c2_resend_countdown, uiState.cooldownSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (uiState.showLinkedAccountNote) {
            InfoBanner(tone = BannerTone.Info, body = stringResource(Res.string.c2_linked_account))
        }
    }
}

// ── Previews: one per design state (design/screens/C2/states/*.png) ─────────────────────────────

/** Fake states built from `card.yaml → sampleData`. Preview-only. */
private object C2PreviewStates {
    private const val CODE = "482915"
    private const val COOLDOWN = 45

    val default = OtpVerifyUiState(
        maskedPhone = maskTrPhone("5321234567"),
        cooldownSeconds = COOLDOWN,
        attemptsLeft = 5,
    )

    val verifying = default.copy(code = CODE, isVerifying = true)

    val error = default.copy(code = CODE, isError = true, attemptsLeft = 4)

    val resendAvailable = default.copy(cooldownSeconds = 0)

    val linkedAccount = default.copy(code = CODE, showLinkedAccountNote = true)
}

@Composable
private fun C2Preview(state: OtpVerifyUiState) {
    DomatTheme(darkTheme = false) {
        Column {
            OtpVerifyScreen(uiState = state, onIntent = {})
        }
    }
}

@Preview(name = "C2@default", widthDp = 390, heightDp = 844)
@Composable
private fun C2DefaultPreview() = C2Preview(C2PreviewStates.default)

@Preview(name = "C2@verifying", widthDp = 390, heightDp = 844)
@Composable
private fun C2VerifyingPreview() = C2Preview(C2PreviewStates.verifying)

@Preview(name = "C2@error", widthDp = 390, heightDp = 844)
@Composable
private fun C2ErrorPreview() = C2Preview(C2PreviewStates.error)

@Preview(name = "C2@resend-available", widthDp = 390, heightDp = 844)
@Composable
private fun C2ResendAvailablePreview() = C2Preview(C2PreviewStates.resendAvailable)

@Preview(name = "C2@linked-account", widthDp = 390, heightDp = 844)
@Composable
private fun C2LinkedAccountPreview() = C2Preview(C2PreviewStates.linkedAccount)
