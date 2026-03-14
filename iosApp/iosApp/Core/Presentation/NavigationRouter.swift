import SwiftUI
import Shared

// MARK: - Navigation Router

/// Central navigation coordinator that bridges KMP Route definitions to SwiftUI NavigationStack.
/// Implements the shared Navigator interface so ViewModels can trigger navigation.
@MainActor
final class NavigationRouter: ObservableObject {

    @Published var path = NavigationPath()
    @Published var currentTab: MainTab = .home

    /// Represents the root flow state (auth vs main content).
    @Published var rootFlow: RootFlow = .auth

    enum RootFlow: Equatable {
        case auth
        case onboarding
        case main
    }
}

// MARK: - Navigation Actions

extension NavigationRouter {
    func navigate(to route: AppRoute) {
        path.append(route)
    }

    func popBack() {
        guard !path.isEmpty else {
            return
        }
        path.removeLast()
    }

    func popToRoot() {
        path = NavigationPath()
    }

    func switchToMain() {
        rootFlow = .main
        path = NavigationPath()
    }

    func switchToAuth() {
        rootFlow = .auth
        path = NavigationPath()
    }

    func switchToOnboarding() {
        rootFlow = .onboarding
        path = NavigationPath()
    }
}

// MARK: - App Routes (Swift-side navigation destinations)

/// Swift-native route enum for NavigationStack routing.
/// Maps 1:1 with KMP Route sealed interface hierarchy.
enum AppRoute: Hashable {
    // Auth
    case login
    case register
    case forgotPassword

    // Main (tab-based, handled separately)
    // These are for push-navigation within tabs:

    // Product
    case productList
    case productDetail(productId: String)
}

// MARK: - Main Tab

enum MainTab: String, CaseIterable {
    case home
    case wallet
    case notifications
    case profile

    var title: String {
        switch self {
        case .home:
            "Home"
        case .wallet:
            "Wallet"
        case .notifications:
            "Notifications"
        case .profile:
            "Profile"
        }
    }

    var icon: String {
        switch self {
        case .home:
            "house.fill"
        case .wallet:
            "wallet.bifold.fill"
        case .notifications:
            "bell.fill"
        case .profile:
            "person.fill"
        }
    }
}

// MARK: - Route Destination ViewBuilder

/// Resolves an AppRoute to its corresponding SwiftUI view.
struct RouteDestination: View {
    let route: AppRoute

    var body: some View {
        switch route {
        case .login:
            LoginView()
        case .register:
            RegisterView()
        case .forgotPassword:
            ForgotPasswordView()
        case .productList:
            ProductListView()
        case .productDetail(let productId):
            ProductDetailView(productId: productId)
        }
    }
}
