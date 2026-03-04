package com.domatapp.core.resource.api

import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource

interface StringResourceApi {
    fun getString(resource: StringResource): String
    fun getString(resource: StringResource, vararg args: Any): String
    fun getPlural(resource: PluralsResource, quantity: Int): String
    fun getPlural(resource: PluralsResource, quantity: Int, vararg args: Any): String
}
