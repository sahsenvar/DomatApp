import SwiftUI

struct WalletView: View {
    @Environment(\.domatColors) private var colors

    var body: some View {
        ScrollView {
            VStack(spacing: DomatSpacing.lg) {
                // Balance Card
                balanceCard

                // Quick Actions
                quickActions

                // Transactions
                transactionsSection
            }.padding(.horizontal, DomatSpacing.md).padding(.top, DomatSpacing.md)
        }.background(colors.background).navigationTitle("Wallet").navigationBarTitleDisplayMode(.large)
    }

    // MARK: - Balance Card

    private var balanceCard: some View {
        DomatCard(style: .filled) {
            VStack(spacing: DomatSpacing.sm) {
                Text("Total Balance").font(DomatTypography.labelLarge).foregroundStyle(colors.onSurfaceVariant)

                Text("$0.00").font(DomatTypography.displaySmall).foregroundStyle(colors.onSurface)
            }.frame(maxWidth: .infinity).padding(.vertical, DomatSpacing.md)
        }
    }

    // MARK: - Quick Actions

    private var quickActions: some View {
        HStack(spacing: DomatSpacing.sm) {
            walletAction(icon: "plus.circle.fill", title: "Add Funds")
            walletAction(icon: "arrow.up.circle.fill", title: "Send")
            walletAction(icon: "arrow.down.circle.fill", title: "Request")
        }
    }

    private func walletAction(icon: String, title: String) -> some View {
        DomatCard(style: .outlined) {
            VStack(spacing: DomatSpacing.sm) {
                Image(systemName: icon).font(.system(size: 24)).foregroundStyle(colors.primary)
                Text(title).font(DomatTypography.labelMedium).foregroundStyle(colors.onSurface)
            }.frame(maxWidth: .infinity)
        }
    }

    // MARK: - Transactions

    private var transactionsSection: some View {
        VStack(alignment: .leading, spacing: DomatSpacing.sm) {
            Text("Recent Transactions").font(DomatTypography.titleMedium).foregroundStyle(colors.onBackground)

            // Empty state
            VStack(spacing: DomatSpacing.md) {
                Image(systemName: "creditcard").font(.system(size: 36)).foregroundStyle(colors.onSurfaceVariant.opacity(0.5))

                Text("No transactions yet").font(DomatTypography.bodyMedium).foregroundStyle(colors.onSurfaceVariant)
            }.frame(maxWidth: .infinity).padding(.vertical, DomatSpacing.xxl)
        }
    }
}

#Preview {
    NavigationStack {
        WalletView()
    }.domatTheme()
}
