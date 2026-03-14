import SwiftUI
import Shared

/// Root content view that switches between auth, onboarding, and main flows
/// based on NavigationRouter state.
struct ContentView: View {
    @EnvironmentObject private var router: NavigationRouter

    var body: some View {
        Group {
            switch router.rootFlow {
            case .onboarding:
                OnboardingView()

            case .auth:
                authFlow

            case .main:
                mainFlow
            }
        }.animation(.easeInOut(duration: 0.3), value: router.rootFlow)
    }

    // MARK: - Auth Flow

    private var authFlow: some View {
        NavigationStack(path: $router.path) {
            LoginView().navigationDestination(for: AppRoute.self) {
                route in
                RouteDestination(route: route)
            }
        }
    }

    // MARK: - Main Flow (Tab-Based)

    private var mainFlow: some View {
        TabView(selection: $router.currentTab) {
            NavigationStack(path: $router.path) {
                HomeView().navigationDestination(for: AppRoute.self) {
                    route in
                    RouteDestination(route: route)
                }
            }.tabItem {
                Label(MainTab.home.title, systemImage: MainTab.home.icon)
            }.tag(MainTab.home)

            NavigationStack {
                WalletView()
            }.tabItem {
                Label(MainTab.wallet.title, systemImage: MainTab.wallet.icon)
            }.tag(MainTab.wallet)

            NavigationStack {
                NotificationView()
            }.tabItem {
                Label(MainTab.notifications.title, systemImage: MainTab.notifications.icon)
            }.tag(MainTab.notifications)

            NavigationStack {
                ProfileView()
            }.tabItem {
                Label(MainTab.profile.title, systemImage: MainTab.profile.icon)
            }.tag(MainTab.profile)
        }
    }
}

#Preview {
    ContentView().environmentObject(NavigationRouter()).domatTheme()
}
