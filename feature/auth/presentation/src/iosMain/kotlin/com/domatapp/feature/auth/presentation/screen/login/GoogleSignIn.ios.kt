package com.domatapp.feature.auth.presentation.screen.login

import com.domatapp.core.presentation.platform.PlatformContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * The Swift half of Google sign-in on iOS.
 *
 * Kotlin/Native cannot call the GoogleSignIn-iOS SDK: it is a Swift package the Xcode project
 * consumes, not something the Kotlin compiler sees, and no cinterop definition for it exists here.
 * So the direction is inverted relative to Android - Swift implements this and hands it to
 * [GoogleSignInBridge], and Kotlin only awaits the token.
 */
interface GoogleSignInPresenter {

    /**
     * Presents the Google account picker and reports the resulting ID token.
     *
     * @param serverClientId the OAuth *web* client ID the backend validates against.
     * @param onResult must be invoked exactly once, on the main thread, with the ID token - or with
     *   `null` if the user cancelled or the SDK failed. A second invocation is ignored rather than
     *   crashing, but it still means the Swift side has a bug.
     */
    fun present(serverClientId: String, onResult: (String?) -> Unit)
}

/**
 * Where Swift installs its [GoogleSignInPresenter], from `iOSApp.init()`, before any screen runs.
 *
 * Nothing registered means sign-in reports cancellation rather than crashing: an iOS build that
 * forgets the registration shows a button that does nothing, which is a far better failure than a
 * `NullPointerException` inside a coroutine.
 */
object GoogleSignInBridge {

    private var presenter: GoogleSignInPresenter? = null

    fun register(presenter: GoogleSignInPresenter) {
        this.presenter = presenter
    }

    internal fun current(): GoogleSignInPresenter? = presenter
}

internal actual suspend fun requestGoogleIdToken(
    context: PlatformContext,
    serverClientId: String,
): String? {
    val presenter = GoogleSignInBridge.current() ?: return null
    return suspendCancellableCoroutine { continuation ->
        var resumed = false
        presenter.present(serverClientId) { idToken ->
            if (!resumed) {
                resumed = true
                continuation.resume(idToken)
            }
        }
    }
}
