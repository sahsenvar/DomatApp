import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var router: NavigationRouter
    @Environment(\.domatColors) private var colors

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: DomatSpacing.lg) {
                // Greeting
                greetingSection

                // Quick Actions
                quickActionsSection

                // Recent Activity
                recentActivitySection
            }.padding(.horizontal, DomatSpacing.md).padding(.top, DomatSpacing.md)
        }.background(colors.background).navigationTitle("Home").navigationBarTitleDisplayMode(.large)
    }

    // MARK: - Sections

    private var greetingSection: some View {
        VStack(alignment: .leading, spacing: DomatSpacing.xs) {
            Text("Good morning").font(DomatTypography.titleMedium).foregroundStyle(colors.onSurfaceVariant)

            Text("Welcome to DomatApp").font(DomatTypography.headlineSmall).foregroundStyle(colors.onBackground)
        }
    }

    private var quickActionsSection: some View {
        VStack(alignment: .leading, spacing: DomatSpacing.sm) {
            Text("Quick Actions").font(DomatTypography.titleMedium).foregroundStyle(colors.onBackground)

            LazyVGrid(columns: [
                GridItem(.flexible()),
                GridItem(.flexible()),
            ], spacing: DomatSpacing.sm) {
                quickActionCard(icon: "leaf.fill", title: "Products", color: colors.primary) {
                    router.navigate(to: .productList)
                }
                quickActionCard(icon: "wallet.bifold.fill", title: "Wallet", color: colors.tertiary) {
                    router.currentTab = .wallet
                }
                quickActionCard(icon: "bell.fill", title: "Alerts", color: colors.secondary) {
                    router.currentTab = .notifications
                }
                quickActionCard(icon: "person.fill", title: "Profile", color: colors.tertiary) {
                    router.currentTab = .profile
                }
            }
        }
    }

    private var recentActivitySection: some View {
        VStack(alignment: .leading, spacing: DomatSpacing.sm) {
            Text("Recent Activity").font(DomatTypography.titleMedium).foregroundStyle(colors.onBackground)

            // Placeholder cards
            ForEach(0 ..< 3, id: \.self) {
                index in
                DomatCard(style: .outlined) {
                    HStack(spacing: DomatSpacing.md) {
                        Circle().fill(colors.primaryContainer).frame(width: 40, height: 40).overlay {
                            Image(systemName: "clock.fill").foregroundStyle(colors.onPrimaryContainer).font(DomatTypography.bodyMedium)
                        }

                        VStack(alignment: .leading, spacing: DomatSpacing.xxs) {
                            Text("Activity \(index + 1)").font(DomatTypography.titleSmall).foregroundStyle(colors.onSurface)
                            Text("Description placeholder").font(DomatTypography.bodySmall).foregroundStyle(colors.onSurfaceVariant)
                        }

                        Spacer()

                        Text("2h ago").font(DomatTypography.labelSmall).foregroundStyle(colors.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // MARK: - Helpers

    private func quickActionCard(icon: String,
    title: String,
    color: Color,
    action: @escaping () -> Void) -> some View {
        DomatCard(style: .filled, action: action) {
            VStack(spacing: DomatSpacing.sm) {
                Image(systemName: icon).font(.system(size: 24)).foregroundStyle(color)
                Text(title).font(DomatTypography.labelLarge).foregroundStyle(colors.onSurface)
            }.frame(maxWidth: .infinity).padding(.vertical, DomatSpacing.sm)
        }
    }
}

#Preview {
    NavigationStack {
        HomeView()
    }.environmentObject(NavigationRouter()).domatTheme()
}
