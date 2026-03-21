import SwiftUI
import Shared

// MARK: - Private helper

private func c(_ resource: Shared.ColorResource) -> SwiftUI.Color {
    SwiftUI.Color(uiColor: resource.getUIColor())
}

// MARK: - Color Hex Initializer

extension SwiftUI.Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255.0,
            green: Double((hex >> 8) & 0xFF) / 255.0,
            blue: Double(hex & 0xFF) / 255.0,
            opacity: alpha
        )
    }
}

// MARK: - Semantic Color Scheme

struct DomatColorScheme {
    let primary: SwiftUI.Color
    let onPrimary: SwiftUI.Color
    let primaryContainer: SwiftUI.Color
    let onPrimaryContainer: SwiftUI.Color

    let secondary: SwiftUI.Color
    let onSecondary: SwiftUI.Color
    let secondaryContainer: SwiftUI.Color
    let onSecondaryContainer: SwiftUI.Color

    let tertiary: SwiftUI.Color
    let onTertiary: SwiftUI.Color
    let tertiaryContainer: SwiftUI.Color
    let onTertiaryContainer: SwiftUI.Color

    let background: SwiftUI.Color
    let onBackground: SwiftUI.Color

    let surface: SwiftUI.Color
    let onSurface: SwiftUI.Color
    let surfaceVariant: SwiftUI.Color
    let onSurfaceVariant: SwiftUI.Color

    let error: SwiftUI.Color
    let onError: SwiftUI.Color
    let errorContainer: SwiftUI.Color
    let onErrorContainer: SwiftUI.Color

    let outline: SwiftUI.Color
    let outlineVariant: SwiftUI.Color
}

extension DomatColorScheme {
    static let light = DomatColorScheme(
        primary:             c(MR.colors.shared.malachite),
        onPrimary:           c(MR.colors.shared.slate_900),
        primaryContainer:    c(MR.colors.shared.malachite_20),
        onPrimaryContainer:  c(MR.colors.shared.midnight_green),
        secondary:           c(MR.colors.shared.slate_600),
        onSecondary:         c(MR.colors.shared.white),
        secondaryContainer:  c(MR.colors.shared.slate_100),
        onSecondaryContainer:c(MR.colors.shared.slate_900),
        tertiary:            c(MR.colors.shared.blue_900),
        onTertiary:          c(MR.colors.shared.white),
        tertiaryContainer:   c(MR.colors.shared.blue_100),
        onTertiaryContainer: c(MR.colors.shared.blue_900),
        background:          c(MR.colors.shared.white),
        onBackground:        c(MR.colors.shared.slate_900),
        surface:             c(MR.colors.shared.white),
        onSurface:           c(MR.colors.shared.slate_900),
        surfaceVariant:      c(MR.colors.shared.slate_50),
        onSurfaceVariant:    c(MR.colors.shared.slate_600),
        error:               c(MR.colors.shared.red_500),
        onError:             c(MR.colors.shared.white),
        errorContainer:      c(MR.colors.shared.red_100),
        onErrorContainer:    c(MR.colors.shared.red_800),
        outline:             c(MR.colors.shared.slate_300),
        outlineVariant:      c(MR.colors.shared.slate_200)
    )

    static let dark = DomatColorScheme(
        primary:             c(MR.colors.shared.malachite),
        onPrimary:           c(MR.colors.shared.midnight_green),
        primaryContainer:    c(MR.colors.shared.midnight_green),
        onPrimaryContainer:  c(MR.colors.shared.malachite),
        secondary:           c(MR.colors.shared.slate_400),
        onSecondary:         c(MR.colors.shared.slate_900),
        secondaryContainer:  c(MR.colors.shared.slate_800),
        onSecondaryContainer:c(MR.colors.shared.white),
        tertiary:            c(MR.colors.shared.blue_100),
        onTertiary:          c(MR.colors.shared.blue_900),
        tertiaryContainer:   c(MR.colors.shared.blue_900),
        onTertiaryContainer: c(MR.colors.shared.blue_100),
        background:          c(MR.colors.shared.slate_900),
        onBackground:        c(MR.colors.shared.white),
        surface:             c(MR.colors.shared.slate_900),
        onSurface:           c(MR.colors.shared.white),
        surfaceVariant:      c(MR.colors.shared.slate_800),
        onSurfaceVariant:    c(MR.colors.shared.slate_400),
        error:               c(MR.colors.shared.red_500),
        onError:             c(MR.colors.shared.white),
        errorContainer:      c(MR.colors.shared.red_900),
        onErrorContainer:    c(MR.colors.shared.red_100),
        outline:             c(MR.colors.shared.slate_400),
        outlineVariant:      c(MR.colors.shared.slate_700)
    )
}

// MARK: - Environment Integration

private struct DomatColorSchemeKey: EnvironmentKey {
    static let defaultValue: DomatColorScheme = .light
}

extension EnvironmentValues {
    var domatColors: DomatColorScheme {
        get { self[DomatColorSchemeKey.self] }
        set { self[DomatColorSchemeKey.self] = newValue }
    }
}
