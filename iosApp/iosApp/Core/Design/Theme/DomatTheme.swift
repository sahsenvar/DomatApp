import SwiftUI

/// View modifier that applies the Domat theme (color scheme based on system appearance).
/// Usage: `.domatTheme()` on any root view.
struct DomatThemeModifier: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        content.environment(\.domatColors, colorScheme == .dark ? .dark: .light).tint(colorScheme == .dark ? DomatPalette.green300: DomatPalette.green700)
    }
}

extension View {
    func domatTheme() -> some View {
        modifier(DomatThemeModifier())
    }
}
