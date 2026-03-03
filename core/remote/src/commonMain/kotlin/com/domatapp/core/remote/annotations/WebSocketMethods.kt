package com.domatapp.core.remote.annotations

/**
 * WebSocket subscribe.
 * Returns a Flow that emits messages from the WebSocket.
 *
 * Usage:
 * ```kotlin
 * @Subscribe("events")
 * fun subscribeToEvents(): Flow<Event>
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Subscribe(val path: String)

/**
 * WebSocket send.
 * Sends a message through the WebSocket and waits for a response.
 *
 * Usage:
 * ```kotlin
 * @Send("commands")
 * suspend fun sendCommand(@Body command: Command): Response
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Send(val path: String)
