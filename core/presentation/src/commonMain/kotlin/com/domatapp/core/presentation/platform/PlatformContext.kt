package com.domatapp.core.presentation.platform

import androidx.compose.runtime.Composable

/**
 * The host handle an OS-modal API needs, for the platforms this app runs on.
 *
 * On Android it *is* `android.content.Context` - and, read through [currentPlatformContext] under
 * `ComponentActivity.setContent`, specifically the Activity, which is what Credential Manager and
 * anything else that puts up a system dialog requires. On iOS nothing equivalent has to be threaded
 * through composition: a `UIViewController` is presented from Swift, which already knows its own
 * window, so the iOS actual is an empty marker that exists only to keep the signature common.
 *
 * `abstract`, not a plain `expect class`: an `expect class` with no declared constructor implies a
 * no-arg one, and `android.content.Context` - being abstract - cannot supply it, so the typealias
 * actual would not compile.
 */
expect abstract class PlatformContext

/** The [PlatformContext] of the composition this is called from. */
@Composable
expect fun currentPlatformContext(): PlatformContext
