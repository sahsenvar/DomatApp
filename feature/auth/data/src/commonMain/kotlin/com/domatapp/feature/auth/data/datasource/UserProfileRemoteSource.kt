package com.domatapp.feature.auth.data.datasource

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.annotation.Query
import com.domatapp.feature.auth.data.remote.UserProfileRemoteModel

/**
 * Remote source for user profile operations via Supabase PostgREST.
 * KtorfitX's KSP processor generates `impls.UserProfileRemoteSourceImpl` plus the
 * `Ktorfitx.userProfileRemoteSource` extension property used to obtain it.
 */
@Api
interface UserProfileRemoteSource {

    /**
     * Get user profile by ID.
     * Query format for Supabase PostgREST: userId = "eq.{userId}", select = "id"
     */
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Query("id") userId: String,
        @Query("select") select: String
    ): List<UserProfileRemoteModel>
}
