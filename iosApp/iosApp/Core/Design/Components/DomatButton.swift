import SwiftUI

// MARK: - Button Style

enum DomatButtonStyle {
    case filled
    case outlined
    case text
    case tonal
}

// MARK: - DomatButton

struct DomatButton: View {
    let title: String
    let style: DomatButtonStyle
    let isLoading: Bool
    let isEnabled: Bool
    let icon: String?
    let action: () -> Void

    @Environment(\.domatColors) private var colors

    init(_ title: String,
    style: DomatButtonStyle = .filled,
    isLoading: Bool = false,
    isEnabled: Bool = true,
    icon: String? = nil,
    action: @escaping () -> Void) {
        self.title = title
        self.style = style
        self.isLoading = isLoading
        self.isEnabled = isEnabled
        self.icon = icon
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: DomatSpacing.sm) {
                if isLoading {
                    ProgressView().tint(foregroundColor)
                } else {
                    if let icon {
                        Image(systemName: icon).font(DomatTypography.labelLarge)
                    }
                    Text(title).font(DomatTypography.labelLarge)
                }
            }.frame(maxWidth: .infinity).padding(.horizontal, DomatSpacing.lg).padding(.vertical, DomatSpacing.sm + DomatSpacing.xs).background(backgroundColor).foregroundStyle(foregroundColor).clipShape(RoundedRectangle(cornerRadius: DomatShape.extraLarge)).overlay {
                if style == .outlined {
                    RoundedRectangle(cornerRadius: DomatShape.extraLarge).strokeBorder(borderColor, lineWidth: 1)
                }
            }
        }.disabled(!isEnabled || isLoading).opacity(isEnabled ? 1.0: 0.38)
    }

    // MARK: - Style Computed Properties

    private var backgroundColor: Color {
        switch style {
        case .filled:
            colors.primary
        case .tonal:
            colors.secondaryContainer
        case .outlined, .text:
            .clear
        }
    }

    private var foregroundColor: Color {
        switch style {
        case .filled:
            colors.onPrimary
        case .tonal:
            colors.onSecondaryContainer
        case .outlined:
            colors.primary
        case .text:
            colors.primary
        }
    }

    private var borderColor: Color {
        colors.outline
    }
}

// MARK: - Preview

#Preview {
    VStack(spacing: DomatSpacing.md) {
        DomatButton("Filled Button", style: .filled) {
        }
        DomatButton("Outlined Button", style: .outlined) {
        }
        DomatButton("Text Button", style: .text) {
        }
        DomatButton("Tonal Button", style: .tonal) {
        }
        DomatButton("Loading", isLoading: true) {
        }
        DomatButton("Disabled", isEnabled: false) {
        }
        DomatButton("With Icon", icon: "arrow.right") {
        }
    }.padding().domatTheme()
}
