package com.domatapp.feature.auth.data.repository

import com.domatapp.feature.auth.data.datasource.UserProfileRemoteDataSource
import com.domatapp.feature.auth.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UserProfileRepositoryImpl(
    private val remoteSource: UserProfileRemoteDataSource
) : UserProfileRepository {

    // todo: Burası henüz bitmedi. Supabase'e adresler için table eklemem lazım
    override fun hasOnboardingRecord(userId: String): Flow<Boolean> = flow {
        val profiles = remoteSource.getProfile(
            userId = "eq.$userId",
            select = "id"
        )
        emit(profiles.isNotEmpty())
    }.catch {
        // Profile check failure -> redirect to onboarding (safe side)
        emit(false)
    }
}
