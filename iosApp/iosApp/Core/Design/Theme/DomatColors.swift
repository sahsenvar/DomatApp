import SwiftUI

// MARK: - Color Palette

/// Raw color palette matching Android Moko Resources colors.xml exactly.
/// All hex values are taken from core/resource/src/commonMain/moko-resources/base/colors.xml
enum DomatPalette {
    // Green
    static let green900 = Color(hex: 0x002204)
    static let green800 = Color(hex: 0x00390A)
    static let green700 = Color(hex: 0x1B5E20)
    static let green300 = Color(hex: 0x81C784)
    static let green200 = Color(hex: 0xA5D6A7)

    // Sage
    static let sage900 = Color(hex: 0x0B1F13)
    static let sage800 = Color(hex: 0x203527)
    static let sage700 = Color(hex: 0x374B3C)
    static let sage600 = Color(hex: 0x4E6352)
    static let sage500 = Color(hex: 0x717970)
    static let sage400 = Color(hex: 0x8B938A)
    static let sage300 = Color(hex: 0xB5CCB7)
    static let sage200 = Color(hex: 0xD0E8D2)
    static let sage100 = Color(hex: 0xDDE5DB)

    // Neutral
    static let neutral900 = Color(hex: 0x191C19)
    static let neutral700 = Color(hex: 0x414941)
    static let neutral300 = Color(hex: 0xC1C9BF)
    static let neutral100 = Color(hex: 0xE1E3DE)
    static let neutral50 = Color(hex: 0xF8FAF5)
    static let white = Color(hex: 0xFFFFFF)

    // Teal
    static let teal900 = Color(hex: 0x00201D)
    static let teal800 = Color(hex: 0x013733)
    static let teal700 = Color(hex: 0x1F4B47)
    static let teal600 = Color(hex: 0x3A635E)
    static let teal300 = Color(hex: 0xA0D0CA)
    static let teal200 = Color(hex: 0xBCECE6)

    // Red
    static let red900 = Color(hex: 0x410002)
    static let red800 = Color(hex: 0x690005)
    static let red700 = Color(hex: 0x93000A)
    static let red600 = Color(hex: 0xBA1A1A)
    static let red200 = Color(hex: 0xFFB4AB)
    static let red100 = Color(hex: 0xFFDAD6)
}

// MARK: - Semantic Color Scheme

/// Material 3 semantic color scheme matching Android's DomatTheme.
struct DomatColorScheme {
    let primary: Color
    let onPrimary: Color
    let primaryContainer: Color
    let onPrimaryContainer: Color

    let secondary: Color
    let onSecondary: Color
    let secondaryContainer: Color
    let onSecondaryContainer: Color

    let tertiary: Color
    let onTertiary: Color
    let tertiaryContainer: Color
    let onTertiaryContainer: Color

    let background: Color
    let onBackground: Color

    let surface: Color
    let onSurface: Color
    let surfaceVariant: Color
    let onSurfaceVariant: Color

    let error: Color
    let onError: Color
    let errorContainer: Color
    let onErrorContainer: Color

    let outline: Color
    let outlineVariant: Color
}

extension DomatColorScheme {
    static let light = DomatColorScheme(
        primary: Color(hex: 0x13EC49),
        onPrimary: DomatPalette.green900,
        primaryContainer: DomatPalette.green200,
        onPrimaryContainer: DomatPalette.green900,
        secondary: DomatPalette.sage600,
        onSecondary: DomatPalette.white,
        secondaryContainer: DomatPalette.sage200,
        onSecondaryContainer: DomatPalette.sage900,
        tertiary: DomatPalette.teal600,
        onTertiary: DomatPalette.white,
        tertiaryContainer: DomatPalette.teal200,
        onTertiaryContainer: DomatPalette.teal900,
        background: DomatPalette.neutral50,
        onBackground: DomatPalette.neutral900,
        surface: DomatPalette.neutral50,
        onSurface: DomatPalette.neutral900,
        surfaceVariant: DomatPalette.sage100,
        onSurfaceVariant: DomatPalette.neutral700,
        error: DomatPalette.red600,
        onError: DomatPalette.white,
        errorContainer: DomatPalette.red100,
        onErrorContainer: DomatPalette.red900,
        outline: DomatPalette.sage500,
        outlineVariant: DomatPalette.neutral300
    )

    static let dark = DomatColorScheme(
        primary: DomatPalette.green300,
        onPrimary: DomatPalette.green800,
        primaryContainer: DomatPalette.green700,
        onPrimaryContainer: DomatPalette.green200,
        secondary: DomatPalette.sage300,
        onSecondary: DomatPalette.sage800,
        secondaryContainer: DomatPalette.sage700,
        onSecondaryContainer: DomatPalette.sage200,
        tertiary: DomatPalette.teal300,
        onTertiary: DomatPalette.teal800,
        tertiaryContainer: DomatPalette.teal700,
        onTertiaryContainer: DomatPalette.teal200,
        background: DomatPalette.neutral900,
        onBackground: DomatPalette.neutral100,
        surface: DomatPalette.neutral900,
        onSurface: DomatPalette.neutral100,
        surfaceVariant: DomatPalette.neutral700,
        onSurfaceVariant: DomatPalette.neutral300,
        error: DomatPalette.red200,
        onError: DomatPalette.red800,
        errorContainer: DomatPalette.red700,
        onErrorContainer: DomatPalette.red100,
        outline: DomatPalette.sage400,
        outlineVariant: DomatPalette.neutral700
    )
}

// MARK: - Environment Integration

private struct DomatColorSchemeKey: EnvironmentKey {
    static let defaultValue: DomatColorScheme = .light
}

extension EnvironmentValues {
    var domatColors: DomatColorScheme {
        get {
            self[DomatColorSchemeKey.self]
        }
        set {
            self[DomatColorSchemeKey.self] = newValue
        }
    }
}

// MARK: - Color Hex Initializer

extension Color {
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
