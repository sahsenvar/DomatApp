import SwiftUI

struct ProductListView: View {
    @EnvironmentObject private var router: NavigationRouter
    @Environment(\.domatColors) private var colors

    // Placeholder data
    private let products = (1 ... 8).map {
        index in
        ProductItem(
            id: "\(index)",
            name: "Product \(index)",
            price: "$\(Double(index) * 3.49)",
            category: index % 2 == 0 ? "Vegetables": "Fruits"
        )
    }

    var body: some View {
        ScrollView {
            LazyVGrid(columns: [
                GridItem(.flexible(), spacing: DomatSpacing.sm),
                GridItem(.flexible(), spacing: DomatSpacing.sm),
            ], spacing: DomatSpacing.sm) {
                ForEach(products) {
                    product in
                    productCard(product)
                }
            }.padding(.horizontal, DomatSpacing.md).padding(.top, DomatSpacing.sm)
        }.background(colors.background).navigationTitle("Products").navigationBarTitleDisplayMode(.large)
    }

    private func productCard(_ product: ProductItem) -> some View {
        DomatCard(style: .elevated, action: {
            router.navigate(to: .productDetail(productId: product.id))
        }) {
            VStack(alignment: .leading, spacing: DomatSpacing.sm) {
                // Placeholder image
                RoundedRectangle(cornerRadius: DomatShape.small).fill(colors.primaryContainer).frame(height: 100).overlay {
                    Image(systemName: "leaf.fill").font(.system(size: 32)).foregroundStyle(colors.onPrimaryContainer.opacity(0.5))
                }

                VStack(alignment: .leading, spacing: DomatSpacing.xxs) {
                    Text(product.name).font(DomatTypography.titleSmall).foregroundStyle(colors.onSurface)

                    Text(product.category).font(DomatTypography.labelSmall).foregroundStyle(colors.onSurfaceVariant)

                    Text(product.price).font(DomatTypography.labelLarge).foregroundStyle(colors.primary)
                }
            }
        }
    }
}

// MARK: - Model

private struct ProductItem: Identifiable {
    let id: String
    let name: String
    let price: String
    let category: String
}

#Preview {
    NavigationStack {
        ProductListView()
    }.environmentObject(NavigationRouter()).domatTheme()
}
