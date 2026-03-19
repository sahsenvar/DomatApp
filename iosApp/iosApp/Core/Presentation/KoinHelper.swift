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

// MARK: - koinViewModel

/// Compose'daki `koinViewModel()` eşdeğeri.
/// Koin'den alınan bir KMP ViewModel'ı SwiftUI'a bağlayan ViewModelWrapper oluşturur.
///
/// Kullanım:
/// ```swift
/// @StateObject private var vm = koinViewModel(koinHelper.authViewModel(), initialState: AuthUiState())
/// vm.state         // → UiState (AuthUiState, HomeUiState vs.)
/// vm.send(intent)  // → Intent gönder
/// .onEffect(from: vm) { effect in ... }  // → Effect dinle
/// ```
@MainActor
func koinViewModel<VM: BaseViewModel<State, Intent, Effect>,
                   State: AnyObject,
                   Intent: AnyObject,
                   Effect: AnyObject>(
    _ viewModel: VM,
    initialState: State
) -> ViewModelWrapper<VM, State, Intent, Effect> {
    ViewModelWrapper(viewModel: viewModel, initialState: initialState)
}
