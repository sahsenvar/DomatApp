import SwiftUI

struct DomatTextField: View {
    let label: String
    @Binding var text: String
    var placeholder: String = ""
    var isSecure: Bool = false
    var errorMessage: String? = nil
    var leadingIcon: String? = nil
    var trailingIcon: String? = nil
    var onTrailingIconTap: (() -> Void)? = nil
    var keyboardType: UIKeyboardType = .default

    @Environment(\.domatColors) private var colors
    @FocusState private var isFocused: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: DomatSpacing.xs) {
            // Label
            Text(label).font(DomatTypography.bodySmall).foregroundStyle(labelColor)

            // Input Field
            HStack(spacing: DomatSpacing.sm) {
                if let leadingIcon {
                    Image(systemName: leadingIcon).foregroundStyle(iconColor).font(DomatTypography.bodyMedium)
                }

                Group {
                    if isSecure {
                        SecureField(placeholder, text: $text)
                    } else {
                        TextField(placeholder, text: $text)
                    }
                }.font(DomatTypography.bodyLarge).foregroundStyle(colors.onSurface).keyboardType(keyboardType).focused($isFocused)

                if let trailingIcon {
                    Button {
                        onTrailingIconTap ? ()
                    } label: {
                        Image(systemName: trailingIcon).foregroundStyle(iconColor).font(DomatTypography.bodyMedium)
                    }
                }
            }.padding(.horizontal, DomatSpacing.md).padding(.vertical, DomatSpacing.sm + DomatSpacing.xs).background(colors.surfaceVariant.opacity(0.5)).clipShape(RoundedRectangle(cornerRadius: DomatShape.small)).overlay {
                RoundedRectangle(cornerRadius: DomatShape.small).strokeBorder(borderColor, lineWidth: isFocused ? 2: 1)
            }

            // Error Message
            if let errorMessage {
                Text(errorMessage).font(DomatTypography.bodySmall).foregroundStyle(colors.error)
            }
        }
    }

    // MARK: - Computed Colors

    private var labelColor: Color {
        if errorMessage != nil {
            return colors.error
        }
        if isFocused {
            return colors.primary
        }
        return colors.onSurfaceVariant
    }

    private var borderColor: Color {
        if errorMessage != nil {
            return colors.error
        }
        if isFocused {
            return colors.primary
        }
        return colors.outline
    }

    private var iconColor: Color {
        if errorMessage != nil {
            return colors.error
        }
        return colors.onSurfaceVariant
    }
}

// MARK: - Preview

#Preview {
    VStack(spacing: DomatSpacing.md) {
        DomatTextField(
            label: "Email",
            text: .constant(""),
            placeholder: "Enter your email",
            leadingIcon: "envelope"
        )
        DomatTextField(
            label: "Password",
            text: .constant("secret"),
            isSecure: true,
            leadingIcon: "lock",
            trailingIcon: "eye.slash"
        )
        DomatTextField(
            label: "Error Field",
            text: .constant("bad input"),
            errorMessage: "This field is required",
            leadingIcon: "exclamationmark.circle"
        )
    }.padding().domatTheme()
}
