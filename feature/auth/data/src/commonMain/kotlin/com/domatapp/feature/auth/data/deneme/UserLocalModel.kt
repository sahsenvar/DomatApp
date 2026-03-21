package com.domatapp.feature.auth.data.deneme

import com.domatapp.core.data.model.LocalModel
import com.domatapp.core.mapping.annotations.FieldMap
import com.domatapp.core.mapping.annotations.MapFrom
import com.domatapp.core.mapping.annotations.MapTo
import com.domatapp.feature.auth.domain.model.UserDomainModel
import kotlin.time.Clock
import kotlin.time.Instant

@MapTo(UserDomainModel::class)
@MapFrom(UserDomainModel::class)
data class UserLocalModel(
    val userName: String,
    @FieldMap("id")
    val localId: Int,
    val createdTime: Instant = Clock.System.now()
) : LocalModel
