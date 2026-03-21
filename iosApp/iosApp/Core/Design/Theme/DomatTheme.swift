import SwiftUI
import Shared

/// View modifier that applies the Domat theme (color scheme based on system appearance).
/// Usage: `.domatTheme()` on any root view.
struct DomatThemeModifier: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        let tintColor = colorScheme == .dark
            ? SwiftUI.Color(uiColor: MR.colors.shared.malachite.getUIColor())
            : SwiftUI.Color(uiColor: MR.colors.shared.midnight_green.getUIColor())

        content
            .environment(\.domatColors, colorScheme == .dark ? .dark : .light)
            .tint(tintColor)
    }
}

extension View {
    func domatTheme() -> some View {
        modifier(DomatThemeModifier())
    }
}
