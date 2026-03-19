import SwiftUI
import Shared

// MARK: - ViewModelWrapper

/// Generic ObservableObject wrapper that bridges a KMP BaseViewModel to SwiftUI.
///
/// SKIE converts Kotlin `StateFlow<T>` to a Swift `AsyncSequence` and sealed classes
/// to Swift enums, so we can collect state and effects using Swift concurrency.
///
/// Usage:
/// ```swift
/// @StateObject private var vm = ViewModelWrapper(
///     viewModel: koinGet(AuthViewModel.self),
///     initialState: AuthUiState()
/// )
/// ```
@MainActor
final class ViewModelWrapper<VM: BaseViewModel<UiState, Intent, Effect>,
UiState: AnyObject,
Intent: AnyObject,
Effect: AnyObject>: ObservableObject {

    @Published private(set) var state: UiState
    @Published private(set) var latestEffect: Effect? = nil

    let viewModel: VM
    private var stateTask: Task<Void, Never>?
    private var effectTask: Task<Void, Never>?

    /// Handler called when a new effect is emitted. Set this from the view.
    var onEffect: ((Effect) -> Void)?

    init(viewModel: VM, initialState: UiState) {
        self.viewModel = viewModel
        self.state = initialState
        startCollecting()
    }

    func send(_ intent: Intent) {
        viewModel.onIntent(intent: intent)
    }

    private func startCollecting() {
        // Collect StateFlow via SKIE's AsyncSequence adaptation
        stateTask = Task {
            @MainActor [weak self] in
            guard let self else {
                return
            }
            do {
                for try await newState in self.viewModel.state {
                    guard !Task.isCancelled else {
                        break
                    }
                    if let state = newState as? UiState {
                        self.state = state
                    }
                }
            } catch {
                print("Error collecting state: \(error)")
            }
        }

        // Collect effect Flow via SKIE's AsyncSequence adaptation
        effectTask = Task {
            @MainActor [weak self] in
            guard let self else {
                return
            }
            do {
                for try await effect in self.viewModel.effect {
                    guard !Task.isCancelled else {
                        break
                    }
                    if let eff = effect as? Effect {
                        self.latestEffect = eff
                        self.onEffect?(eff)
                    }
                }
            } catch {
                print("Error collecting effect: \(error)")
            }
        }
    }

    func cancelCollection() {
        stateTask?.cancel()
        effectTask?.cancel()
    }

    deinit {
        stateTask?.cancel()
        effectTask?.cancel()
    }
}
