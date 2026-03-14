import SwiftUI

struct ProductDetailView: View {
    let productId: String

    @Environment(\.domatColors) private var colors
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: DomatSpacing.lg) {
                // Hero Image
                RoundedRectangle(cornerRadius: DomatShape.large).fill(colors.primaryContainer).frame(height: 250).overlay {
                    Image(systemName: "leaf.fill").font(.system(size: 64)).foregroundStyle(colors.onPrimaryContainer.opacity(0.4))
                }

                // Product Info
                VStack(alignment: .leading, spacing: DomatSpacing.sm) {
                    Text("Product \(productId)").font(DomatTypography.headlineSmall).foregroundStyle(colors.onBackground)

                    HStack {
                        Text("$12.99").font(DomatTypography.titleLarge).foregroundStyle(colors.primary)

                        Spacer()

                        // Rating
                        HStack(spacing: DomatSpacing.xxs) {
                            Image(systemName: "star.fill").foregroundStyle(.yellow)
                            Text("4.5").font(DomatTypography.labelLarge).foregroundStyle(colors.onSurface)
                            Text("(128)").font(DomatTypography.labelSmall).foregroundStyle(colors.onSurfaceVariant)
                        }
                    }

                    // Category Tag
                    Text("Vegetables").font(DomatTypography.labelMedium).foregroundStyle(colors.onSecondaryContainer).padding(.horizontal, DomatSpacing.sm).padding(.vertical, DomatSpacing.xs).background(colors.secondaryContainer).clipShape(Capsule())
                }

                // Description
                VStack(alignment: .leading, spacing: DomatSpacing.sm) {
                    Text("About").font(DomatTypography.titleMedium).foregroundStyle(colors.onBackground)

                    Text("Fresh, locally sourced produce delivered straight from the farm. Our products are carefully selected to ensure the highest quality and freshness.").font(DomatTypography.bodyMedium).foregroundStyle(colors.onSurfaceVariant)
                }

                Spacer().frame(height: DomatSpacing.md)

                // Add to Cart
                DomatButton("Add to Cart", icon: "cart.badge.plus") {
                    // Will be connected to KMP ViewModel
                }
            }.padding(.horizontal, DomatSpacing.md).padding(.top, DomatSpacing.md)
        }.background(colors.background).navigationTitle("Details").navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    NavigationStack {
        ProductDetailView(productId: "1")
    }.domatTheme()
}
