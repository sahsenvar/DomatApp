import SwiftUI

struct NotificationView: View {
    @Environment(\.domatColors) private var colors

    var body: some View {
        Group {
            if true /* placeholder: notifications.isEmpty */ {
                emptyState
            }
        }.frame(maxWidth: .infinity, maxHeight: .infinity).background(colors.background).navigationTitle("Notifications").navigationBarTitleDisplayMode(.large)
    }

    private var emptyState: some View {
        VStack(spacing: DomatSpacing.md) {
            Image(systemName: "bell.slash").font(.system(size: 48)).foregroundStyle(colors.onSurfaceVariant.opacity(0.5))

            Text("No notifications yet").font(DomatTypography.titleMedium).foregroundStyle(colors.onSurfaceVariant)

            Text("You'll see your notifications here when you get them.").font(DomatTypography.bodyMedium).foregroundStyle(colors.onSurfaceVariant.opacity(0.7)).multilineTextAlignment(.center).padding(.horizontal, DomatSpacing.xl)
        }
    }
}

#Preview {
    NavigationStack {
        NotificationView()
    }.domatTheme()
}
