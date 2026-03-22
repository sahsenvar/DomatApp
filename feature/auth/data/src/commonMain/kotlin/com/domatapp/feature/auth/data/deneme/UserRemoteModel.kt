package com.domatapp.feature.auth.data.deneme

import com.domatapp.core.data.model.RemoteModel
import com.domatapp.core.mapping.annotations.FieldMap
import com.domatapp.core.mapping.annotations.MapFrom
import com.domatapp.core.mapping.annotations.MapTo
import com.domatapp.feature.auth.domain.model.ProfileDomainModel
import com.domatapp.feature.auth.domain.model.UserDomainModel
import com.domatapp.feature.auth.domain.model.toUserLocalModel
import com.domatapp.feature.auth.domain.model.toUserRemoteModel

@MapTo(UserDomainModel::class)
@MapTo(ProfileDomainModel::class)
@MapFrom(UserDomainModel::class)
data class UserRemoteModel( // constructor 1
    val userName: String?,
    @FieldMap(fieldName = "id", targetClass = UserDomainModel::class)
    @FieldMap(fieldName = "id", targetClass = ProfileDomainModel::class)
    @FieldMap(fieldName = "remoteId", targetClass = ProfileDomainModel::class)
    val remoteId: Int?,
    val name: String?,
    val surname: String?
) : RemoteModel {
    internal val fullName: String get() = "$name $surname"

    constructor( // constructor 2
        userName: String?,
        remoteId: Int?,
        fullName: String?
    ) : this(
        userName = userName,
        remoteId = remoteId,
        name = fullName?.split(" ")?.dropLast(1)?.joinToString(separator = " "),
        surname = fullName?.split(" ")?.last()
    )
}


// deneme yeri
lateinit var remote: UserRemoteModel
lateinit var local: UserLocalModel
lateinit var domain: UserDomainModel

val remoteToDomain = remote.toUserDomainModel()
val remoteToDomain2 = remote.toProfileDomainModel()

val domainToRemoteWithConstructor1 = domain.toUserRemoteModel(name = "", surname = "")
val domainToRemoteWithConstructor2 = domain.toUserRemoteModel()

val localToDomain = local.toUserDomainModel(fullName = "")
val domainToLocal = domain.toUserLocalModel()
