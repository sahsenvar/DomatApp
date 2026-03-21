import SwiftUI

/// Google ile giriş yapma butonu.
/// Compose: DomatGoogleSignInButton(onClick, iconPainter, text)
struct DomatGoogleSignInButton: View {
    let text: String
    var icon: UIImage? = nil
    let action: () -> Void

    @Environment(\.domatColors) private var colors

    var body: some View {
        Button(action: action) {
            ZStack {
                // Google ikonu — sol tarafta sabit
                HStack {
                    googleIcon
                        .padding(.leading, 20)
                    Spacer()
                }

                // Metin — ortada
                Text(text)
                    .font(DomatTypography.titleMedium)
                    .foregroundStyle(colors.onSurface)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(colors.surface)
            .clipShape(RoundedRectangle(cornerRadius: DomatShape.medium))
            .overlay {
                RoundedRectangle(cornerRadius: DomatShape.medium)
                    .strokeBorder(colors.outline, lineWidth: 1)
            }
            .domatShadow(DomatElevation.xs)
        }
    }

    @ViewBuilder
    private var googleIcon: some View {
        if let icon {
            Image(uiImage: icon)
                .resizable()
                .frame(width: 24, height: 24)
        } else {
            // Fallback: Google rengiyle "G"
            Text("G")
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(Color(hex: 0x4285F4))
                .frame(width: 24, height: 24)
        }
    }
}

#Preview {
    DomatGoogleSignInButton(text: "Google ile Devam Et") {}
        .padding()
        .domatTheme()
}
