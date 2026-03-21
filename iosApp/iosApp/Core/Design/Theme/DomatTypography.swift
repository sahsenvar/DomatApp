import SwiftUI

/// Typography scale matching Android's DomatTypographyScale and Material 3 type system.
/// Uses Nunito Sans font family (must be added to the Xcode project bundle).
/// Falls back to system font if Nunito Sans is not available.
enum DomatTypography {

    // MARK: - Font Family
    // Projede mevcut font: NunitoSans-12ptExtraLight (nunito_sans_regular.ttf)
    // ExtraBold/Bold varyantları henüz eklenmemiş — gerekli font dosyaları eklenirse
    // PostScript isimlerini buraya ekleyip fontFamily güncellenmeli.

    private static let fontRegular = "NunitoSans-12ptExtraLight"

    private static func font(size: CGFloat, weight: Font.Weight) -> Font {
        if let _ = UIFont(name: fontRegular, size: size) {
            return .custom(fontRegular, size: size).weight(weight)
        }
        return .system(size: size, weight: weight, design: .default)
    }

    // MARK: - Display

    static let displayLarge = font(size: 57, weight: .regular)
    static let displayMedium = font(size: 45, weight: .regular)
    static let displaySmall = font(size: 36, weight: .regular)
    static let displaySmallExtraBold = font(size: 36, weight: .heavy)

    // MARK: - Headline

    static let headlineLarge = font(size: 32, weight: .regular)
    static let headlineMedium = font(size: 28, weight: .regular)
    static let headlineSmall = font(size: 24, weight: .regular)

    // MARK: - Title

    static let titleLarge = font(size: 22, weight: .medium)
    static let titleMedium = font(size: 16, weight: .medium)
    static let titleSmall = font(size: 14, weight: .medium)

    // MARK: - Body

    static let bodyLarge = font(size: 16, weight: .regular)
    static let bodyMedium = font(size: 14, weight: .regular)
    static let bodySmall = font(size: 12, weight: .regular)

    // MARK: - Label

    static let labelLarge = font(size: 14, weight: .medium)
    static let labelMedium = font(size: 12, weight: .medium)
    static let labelSmall = font(size: 11, weight: .medium)
}

// MARK: - Line Height Modifiers

/// Line height values matching Android's DomatTypographyScale.
/// Apply via `.lineSpacing()` or use the convenience modifiers below.
enum DomatLineHeight {
    static let displayLarge: CGFloat = 64
    static let displayMedium: CGFloat = 52
    static let displaySmall: CGFloat = 44

    static let headlineLarge: CGFloat = 40
    static let headlineMedium: CGFloat = 36
    static let headlineSmall: CGFloat = 32

    static let titleLarge: CGFloat = 28
    static let titleMedium: CGFloat = 24
    static let titleSmall: CGFloat = 20

    static let bodyLarge: CGFloat = 24
    static let bodyMedium: CGFloat = 20
    static let bodySmall: CGFloat = 16

    static let labelLarge: CGFloat = 20
    static let labelMedium: CGFloat = 16
    static let labelSmall: CGFloat = 16
}
