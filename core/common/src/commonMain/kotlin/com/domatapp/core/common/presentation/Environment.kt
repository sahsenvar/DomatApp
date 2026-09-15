package com.domatapp.core.common.presentation

object Environment {
    var accessToken: String? = null

    // Google Sign-In web client ID (Credential Manager / One Tap). Not a secret - it identifies
    // the OAuth client, not a credential - but it belongs here rather than hardcoded at a call
    // site, matching supabaseHost/publicKey in core:remote's provideKtorfitx.kt.
    const val googleWebClientId = "60308278582-09rmm39o0mmpc5krfdjhp7kkd514j20e.apps.googleusercontent.com"
}