package com.domatapp.feature.auth.presentation.screen.login

import com.domatapp.core.presentation.platform.PlatformContext

/**
 * Puts up the platform's Google account picker and returns the ID token it yields.
 *
 * Returns `null` for every non-success outcome - the user dismissed the sheet, no account was
 * available, the platform SDK failed - which the caller turns into `LoginIntent.OnGoogleSignInCancelled`.
 * Distinguishing those cases is a UX decision the screen does not make today; the ViewModel only
 * needs to know whether it has a token to exchange.
 *
 * @param context the host handle; the Activity on Android, an empty marker on iOS.
 * @param serverClientId the OAuth *web* client ID, which is what the backend validates the token
 *   against - not the per-platform Android/iOS client ID.
 */
internal expect suspend fun requestGoogleIdToken(
    context: PlatformContext,
    serverClientId: String,
): String?
