package com.domatapp.core.resource.api

import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

/**
 * Reads localized strings outside of composition - from a ViewModel, a mapper, a use case.
 *
 * Inside composition, call `stringResource(Res.string.x)` directly instead; this exists for the
 * places that have no `@Composable` scope to read from.
 *
 * Every function suspends because Compose Resources itself is suspending off the main thread: a
 * resource is read from the platform's asset storage, not from an in-memory table. Moko Resources,
 * which this replaced, resolved strings synchronously through an Android `Context` / iOS
 * `NSBundle`, which is why this interface used to have an `expect`/`actual` implementation per
 * platform. It no longer needs one - `org.jetbrains.compose.resources` is common code.
 */
interface StringResourceApi {
    suspend fun getString(resource: StringResource): String
    suspend fun getString(resource: StringResource, vararg args: Any): String
    suspend fun getPlural(resource: PluralStringResource, quantity: Int): String
    suspend fun getPlural(resource: PluralStringResource, quantity: Int, vararg args: Any): String
}
