import SwiftUI
import GoogleSignIn
import Shared

@main
struct iOSApp: App {

    init() {
        // Must run before any screen composes: the Compose hierarchy resolves its ViewModels
        // through Koin.
        KoinHelperKt.doInitKoin()
        // Kotlin cannot call the GoogleSignIn-iOS SDK - it is a Swift package the Kotlin compiler
        // never sees - so the Login screen's `requestGoogleIdToken` awaits whatever is registered
        // here. Registering nothing would not crash; the Google button would simply report
        // cancellation.
        GoogleSignInBridge.shared.register(presenter: GoogleSignInPresenterImpl())
    }

    var body: some Scene {
        WindowGroup {
            // Only the keyboard inset is ignored - Compose handles that one itself. Ignoring the
            // whole safe area would put the app's own top bars under the status bar, where the
            // system draws over them and they receive no touches.
            ContentView()
                .ignoresSafeArea(.keyboard)
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
