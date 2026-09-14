package com.domatapp.feature.auth.data.datasource

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.Body
import cn.ktorfitx.multiplatform.annotation.POST
import cn.ktorfitx.multiplatform.annotation.Query
import com.domatapp.feature.auth.data.remote.AuthSessionRemoteModel
import com.domatapp.feature.auth.data.remote.GoogleSignInRemoteModel

/**
 * Remote source for authentication via Supabase.
 * KtorfitX's KSP processor generates `impls.AuthRemoteSourceImpl` plus the
 * `Ktorfitx.authRemoteSource` extension property used to obtain it.
 *
 * Paths are relative to the KtorfitX base URL configured in `core:remote` (`provideKtorfitx`).
 */
@Api
interface AuthRemoteSource {

    /**
     * Sign in with Google ID token via Supabase auth endpoint.
     */
    @POST("auth/v1/token")
    suspend fun signInWithIdToken(
        @Query("grant_type") grantType: String,
        @Body body: GoogleSignInRemoteModel
    ): AuthSessionRemoteModel

    /**
     * Logout current user.
     */
    @POST("auth/v1/logout")
    suspend fun logout()
}
