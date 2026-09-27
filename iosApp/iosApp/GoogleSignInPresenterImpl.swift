import Foundation
import GoogleSignIn
import Shared
import UIKit

/// The Swift half of Google sign-in, handed to Kotlin from `iOSApp.init()`.
///
/// Android drives the account picker through Credential Manager from Kotlin; iOS cannot, because
/// GoogleSignIn-iOS is a Swift package with no cinterop definition here. So the Kotlin `actual` of
/// `requestGoogleIdToken` suspends on this instead, and the MVI contract above it - click, token,
/// `LoginIntent.OnGoogleTokenReceived` - is identical on both platforms.
final class GoogleSignInPresenterImpl: GoogleSignInPresenter {

    /// - Parameters:
    ///   - serverClientID: the OAuth **web** client ID (`Environment.googleWebClientId` in Kotlin).
    ///     It is what the backend validates the token against, and it is not the iOS client ID -
    ///     that one identifies this app to Google and is read from `GIDClientID` in `Info.plist`.
    ///   - onResult: invoked exactly once on the main thread, with `nil` for every non-success
    ///     outcome (cancelled, misconfigured, SDK error) - the Kotlin side turns that into
    ///     `LoginIntent.OnGoogleSignInCancelled`.
    func present(serverClientId: String, onResult: @escaping (String?) -> Void) {
        guard
            let clientID = Bundle.main.object(forInfoDictionaryKey: "GIDClientID") as? String,
            !clientID.isEmpty
        else {
            assertionFailure("GIDClientID is missing from Info.plist - Google sign-in cannot run.")
            onResult(nil)
            return
        }

        guard let presenting = Self.topViewController() else {
            onResult(nil)
            return
        }

        GIDSignIn.sharedInstance.configuration = GIDConfiguration(
            clientID: clientID,
            serverClientID: serverClientId
        )

        GIDSignIn.sharedInstance.signIn(withPresenting: presenting) { result, _ in
            onResult(result?.user.idToken?.tokenString)
        }
    }

    /// The controller actually on screen - presenting from the window root fails once anything is
    /// already presented over it.
    private static func topViewController() -> UIViewController? {
        let root = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)?
            .rootViewController

        var top = root
        while let presented = top?.presentedViewController {
            top = presented
        }
        return top
    }
}
