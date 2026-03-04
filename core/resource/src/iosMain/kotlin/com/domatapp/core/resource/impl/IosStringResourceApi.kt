package com.domatapp.core.resource.impl

import com.domatapp.core.resource.api.StringResourceApi
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.PluralFormattedStringDesc
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc
import dev.icerock.moko.resources.desc.desc
import org.koin.core.annotation.Single

@Single(binds = [StringResourceApi::class])
class IosStringResourceApi : StringResourceApi {

    override fun getString(resource: StringResource): String =
        resource.desc().localized()

    override fun getString(resource: StringResource, vararg args: Any): String =
        ResourceFormattedStringDesc(resource, args.toList()).localized()

    override fun getPlural(resource: PluralsResource, quantity: Int): String =
        PluralFormattedStringDesc(resource, quantity, listOf(quantity)).localized()

    override fun getPlural(resource: PluralsResource, quantity: Int, vararg args: Any): String =
        PluralFormattedStringDesc(resource, quantity, args.toList()).localized()
}
