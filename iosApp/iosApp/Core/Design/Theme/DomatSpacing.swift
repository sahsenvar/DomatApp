import SwiftUI

/// Spacing constants matching Android's DomatSpacing.
enum DomatSpacing {
    static let xxs: CGFloat = 2
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
    static let xxl: CGFloat = 48
}

/// Elevation constants matching Android's DomatElevation.
/// On iOS, these translate to shadow radius values.
enum DomatElevation {
    static let none: CGFloat = 0
    static let xs: CGFloat = 1
    static let sm: CGFloat = 2
    static let md: CGFloat = 4
    static let lg: CGFloat = 8
    static let xl: CGFloat = 12
}

/// Shape corner radius values matching Android's DomatShapes.
enum DomatShape {
    static let small: CGFloat = 8
    static let medium: CGFloat = 12
    static let large: CGFloat = 16
    static let extraLarge: CGFloat = 24
}

// MARK: - Shadow Modifier

extension View {
    /// Applies a Domat-themed shadow matching Android elevation semantics.
    func domatShadow(_ elevation: CGFloat) -> some View {
        self.shadow(
            color: .black.opacity(elevation > 0 ? 0.15: 0),
            radius: elevation,
            x: 0,
            y: elevation / 2
        )
    }
}
