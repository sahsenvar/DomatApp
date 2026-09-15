package com.domatapp.core.resource.impl

import com.domatapp.core.resource.api.StringResourceApi
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString as loadPluralString
import org.jetbrains.compose.resources.getString as loadString

/**
 * The single implementation of [StringResourceApi], in common code.
 *
 * Aliased imports because the top-level Compose Resources functions have the same names as the
 * members overridden here, and an unaliased call would resolve to the member and recurse.
 */
internal class ComposeStringResourceApi : StringResourceApi {

    override suspend fun getString(resource: StringResource): String =
        loadString(resource)

    override suspend fun getString(resource: StringResource, vararg args: Any): String =
        loadString(resource, *args)

    // The quantity is passed as a format argument too, so "%1$d items" resolves without the caller
    // having to repeat it. This matches how the Moko-backed implementation behaved.
    override suspend fun getPlural(resource: PluralStringResource, quantity: Int): String =
        loadPluralString(resource, quantity, quantity)

    override suspend fun getPlural(
        resource: PluralStringResource,
        quantity: Int,
        vararg args: Any,
    ): String = loadPluralString(resource, quantity, *args)
}
