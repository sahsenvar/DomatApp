package com.domatapp.feature.auth.domain.model

import com.domatapp.core.domain.model.DomainModel
import kotlin.time.ExperimentalTime

data class UserDomainModel @OptIn(ExperimentalTime::class) constructor(
    val userName: String,
    val id: String,
    val fullName: String
) : DomainModel

data class ProfileDomainModel @OptIn(ExperimentalTime::class) constructor(
    val userName: String,
    val id: Int,
    val remoteId: String,
    val fullName: String
) : DomainModel