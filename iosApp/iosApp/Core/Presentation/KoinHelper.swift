import Shared

// MARK: - Koin Swift Integration

/// Shared KoinHelper instance. Available after `initializeKoin()` is called.
private(set) var koinHelper: Shared.KoinHelper!

/// Initializes Koin DI container for iOS.
/// Call this once at app startup (in iOSApp.init).
func initializeKoin() {
    KoinHelperKt.doInitKoin()
    koinHelper = Shared.KoinHelper()
}
