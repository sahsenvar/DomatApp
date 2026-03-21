import SwiftUI

/// Hero alanı için ikon + metin pill badge.
/// Compose: DomatHeroBadge(iconPainter, text)
struct DomatHeroBadge: View {
    let text: String
    let systemIcon: String

    @Environment(\.domatColors) private var colors

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: systemIcon)
                .resizable()
                .scaledToFit()
                .frame(width: 13, height: 13)
                .foregroundStyle(colors.primary)

            Text(text.uppercased())
                .font(DomatTypography.labelLarge)
                .foregroundStyle(colors.primary)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 4)
        .background(colors.primary.opacity(0.2))
        .clipShape(Capsule())
        .overlay {
            Capsule().strokeBorder(colors.primary.opacity(0.3), lineWidth: 1)
        }
    }
}

#Preview {
    DomatHeroBadge(text: "Taze & Yerel", systemIcon: "leaf.fill")
        .padding()
        .domatTheme()
}
