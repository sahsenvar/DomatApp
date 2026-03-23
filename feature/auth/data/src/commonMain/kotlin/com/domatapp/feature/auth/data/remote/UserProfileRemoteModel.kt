package com.domatapp.feature.auth.data.remote

import com.domatapp.core.data.model.RemoteModel
import kotlinx.serialization.Serializable

/**
 * Minimal profile model for onboarding check.
 * Only the existence of a record matters — fields are minimal.
 */
@Serializable
data class UserProfileRemoteModel(
    val id: String
) : RemoteModel
