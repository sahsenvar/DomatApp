import SwiftUI
import Shared

// MARK: - Collect StateFlow as SwiftUI State

/// A property wrapper that collects a Kotlin StateFlow into a SwiftUI-published value.
/// Uses SKIE's AsyncSequence bridging under the hood.
///
/// Usage:
/// ```swift
/// @StateObject private var collector = StateFlowCollector(flow: viewModel.state)
/// // Access: collector.value
/// ```
@MainActor
final class StateFlowCollector<T: AnyObject>: ObservableObject {
    @Published private(set) var value: T
    private var task: Task<Void, Never>?

    init(flow: AsyncSequence, initialValue: T) {
        self.value = initialValue
        self.task = Task {
            [weak self] in
            do {
                for try await newValue in flow {
                    guard !Task.isCancelled, let self else {
                        break
                    }
                    if let typedValue = newValue as ? T {
                        self.value = typedValue
                    }
                }
            } catch {
                print("StateFlowCollector Error: \(error)")
            }
        }
    }

    deinit {
        task?.cancel()
    }
}

// MARK: - Effect Observer Modifier

/// A ViewModifier that observes effects from a ViewModelWrapper and invokes a handler.
struct EffectObserver<VM: BaseViewModel<UiState, Intent, Effect>,
UiState: AnyObject,
Intent: AnyObject,
Effect: AnyObject>: ViewModifier {

    @ObservedObject var wrapper: ViewModelWrapper<VM, UiState, Intent, Effect>
    let handler: (Effect) -> Void

    func body(content: Content) -> some View {
        content.onChange(of: ObjectIdentifier(type(of: wrapper.latestEffect as Any))) {
            _, _ in
            if let effect = wrapper.latestEffect {
                handler(effect)
            }
        }.onAppear {
            wrapper.onEffect = handler
        }
    }
}

extension View {
    /// Observes effects from a ViewModelWrapper and invokes the handler on each new effect.
    func onEffect<VM: BaseViewModel<UiState, Intent, Effect>,
    UiState: AnyObject,
    Intent: AnyObject,
    Effect: AnyObject>(from wrapper: ViewModelWrapper<VM, UiState, Intent, Effect>,
    perform handler: @escaping (Effect) -> Void) -> some View {
        modifier(EffectObserver(wrapper: wrapper, handler: handler))
    }
}
