package com.domatapp.feature.auth.presentation.screen.login

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.domatapp.core.presentation.platform.PlatformContext
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

internal actual suspend fun requestGoogleIdToken(
    context: PlatformContext,
    serverClientId: String,
): String? = runCatching {
    val request = GetCredentialRequest.Builder().addCredentialOption(
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            // .setNonce() todo: Güvenlik için daha sonra eklenecek
            .build()
    ).build()

    val credential = CredentialManager.create(context)
        .getCredential(context, request)
        .credential

    val isGoogleIdToken = credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL

    if (isGoogleIdToken) GoogleIdTokenCredential.createFrom(credential.data).idToken else null
}.getOrNull()
