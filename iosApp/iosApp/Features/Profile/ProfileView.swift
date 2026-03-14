import SwiftUI

struct ProfileView: View {
    @EnvironmentObject private var router: NavigationRouter
    @Environment(\.domatColors) private var colors

    var body: some View {
        ScrollView {
            VStack(spacing: DomatSpacing.lg) {
                // Profile Header
                profileHeader

                // Menu Sections
                menuSection
            }.padding(.horizontal, DomatSpacing.md).padding(.top, DomatSpacing.md)
        }.background(colors.background).navigationTitle("Profile").navigationBarTitleDisplayMode(.large)
    }

    // MARK: - Header

    private var profileHeader: some View {
        DomatCard(style: .filled) {
            HStack(spacing: DomatSpacing.md) {
                Circle().fill(colors.primaryContainer).frame(width: 64, height: 64).overlay {
                    Image(systemName: "person.fill").font(.system(size: 28)).foregroundStyle(colors.onPrimaryContainer)
                }

                VStack(alignment: .leading, spacing: DomatSpacing.xxs) {
                    Text("User Name").font(DomatTypography.titleMedium).foregroundStyle(colors.onSurface)
                    Text("user@example.com").font(DomatTypography.bodySmall).foregroundStyle(colors.onSurfaceVariant)
                }

                Spacer()

                Image(systemName: "chevron.right").foregroundStyle(colors.onSurfaceVariant)
            }
        }
    }

    // MARK: - Menu

    private var menuSection: some View {
        VStack(spacing: DomatSpacing.sm) {
            menuItem(icon: "gearshape.fill", title: "Settings")
            menuItem(icon: "bell.fill", title: "Notification Preferences")
            menuItem(icon: "lock.fill", title: "Privacy & Security")
            menuItem(icon: "questionmark.circle.fill", title: "Help & Support")
            menuItem(icon: "doc.text.fill", title: "Terms of Service")

            Divider().padding(.vertical, DomatSpacing.sm)

            // Sign Out
            Button {
                router.switchToAuth()
            } label: {
                HStack(spacing: DomatSpacing.md) {
                    Image(systemName: "rectangle.portrait.and.arrow.right").foregroundStyle(colors.error).frame(width: 24)
                    Text("Sign Out").font(DomatTypography.bodyLarge).foregroundStyle(colors.error)
                    Spacer()
                }.padding(.vertical, DomatSpacing.sm)
            }
        }
    }

    private func menuItem(icon: String, title: String) -> some View {
        Button {
            // Navigation to detail screens
        } label: {
            HStack(spacing: DomatSpacing.md) {
                Image(systemName: icon).foregroundStyle(colors.onSurfaceVariant).frame(width: 24)
                Text(title).font(DomatTypography.bodyLarge).foregroundStyle(colors.onSurface)
                Spacer()
                Image(systemName: "chevron.right").font(DomatTypography.labelSmall).foregroundStyle(colors.onSurfaceVariant)
            }.padding(.vertical, DomatSpacing.sm)
        }
    }
}

#Preview {
    NavigationStack {
        ProfileView()
    }.environmentObject(NavigationRouter()).domatTheme()
}
