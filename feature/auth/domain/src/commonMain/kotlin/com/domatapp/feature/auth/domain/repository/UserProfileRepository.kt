package com.domatapp.feature.auth.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository for user profile operations.
 * Temporarily in auth:domain; will be moved to a dedicated user module in the future.
 */
interface UserProfileRepository {

    /**
     * Checks whether the user has completed onboarding (has a profile record).
     * Returns Flow<Boolean> — true if profile exists, false otherwise.
     */
    fun hasOnboardingRecord(userId: String): Flow<Boolean>
}
