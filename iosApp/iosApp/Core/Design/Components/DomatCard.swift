import SwiftUI

// MARK: - Card Style

enum DomatCardStyle {
    case elevated
    case filled
    case outlined
}

// MARK: - DomatCard

struct DomatCard<Content: View>: View {
    let style: DomatCardStyle
    let action: (() -> Void)?
    @ViewBuilder let content: Content

    @Environment(\.domatColors) private var colors

    init(style: DomatCardStyle = .elevated,
    action: (() -> Void)? = nil,
    @ViewBuilder content: () -> Content) {
        self.style = style
        self.action = action
        self.content = content()
    }

    var body: some View {
        Group {
            if let action {
                Button(action: action) {
                    cardContent
                }.buttonStyle(.plain)
            } else {
                cardContent
            }
        }
    }

    private var cardContent: some View {
        content.frame(maxWidth: .infinity, alignment: .leading).padding(DomatSpacing.md).background(backgroundColor).clipShape(RoundedRectangle(cornerRadius: DomatShape.medium)).overlay {
            if style == .outlined {
                RoundedRectangle(cornerRadius: DomatShape.medium).strokeBorder(colors.outlineVariant, lineWidth: 1)
            }
        }.domatShadow(shadowElevation)
    }

    // MARK: - Style Properties

    private var backgroundColor: Color {
        switch style {
        case .elevated:
            colors.surface
        case .filled:
            colors.surfaceVariant
        case .outlined:
            colors.surface
        }
    }

    private var shadowElevation: CGFloat {
        switch style {
        case .elevated:
            DomatElevation.xs
        case .filled, .outlined:
            DomatElevation.none
        }
    }
}

// MARK: - Preview

#Preview {
    VStack(spacing: DomatSpacing.md) {
        DomatCard(style: .elevated) {
            VStack(alignment: .leading, spacing: DomatSpacing.sm) {
                Text("Elevated Card").font(DomatTypography.titleMedium)
                Text("This is an elevated card with shadow").font(DomatTypography.bodyMedium)
            }
        }

        DomatCard(style: .filled) {
            Text("Filled Card").font(DomatTypography.titleMedium)
        }

        DomatCard(style: .outlined) {
            Text("Outlined Card").font(DomatTypography.titleMedium)
        }

        DomatCard(style: .elevated, action: {
            print("tapped")
        }) {
            Text("Tappable Card").font(DomatTypography.titleMedium)
        }
    }.padding().domatTheme()
}
