import SwiftUI
import Shared

struct LoginView: View {
    @EnvironmentObject private var router: NavigationRouter
    @Environment(\.domatColors) private var colors

    // In production, this would be initialized via Koin:
    // @StateObject private var vm = ViewModelWrapper(viewModel: koinGet(AuthViewModel.self), initialState: AuthUiState())

    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            // Logo / Branding
            brandingSection

            Spacer().frame(height: DomatSpacing.xxl)

            // Sign-In Options
            signInSection

            Spacer()

            // Footer
            footerSection
        }.padding(.horizontal, DomatSpacing.lg).background(colors.background).alert("Error", isPresented: .constant(errorMessage != nil)) {
            Button("OK") {
                errorMessage = nil
            }
        } message: {
            if let errorMessage {
                Text(errorMessage)
            }
        }
    }

    // MARK: - Sections

    private var brandingSection: some View {
        VStack(spacing: DomatSpacing.md) {
            Image(systemName: "leaf.fill").font(.system(size: 64)).foregroundStyle(colors.primary)

            Text("DomatApp").font(DomatTypography.headlineLarge).foregroundStyle(colors.onBackground)

            Text("Welcome back").font(DomatTypography.bodyLarge).foregroundStyle(colors.onSurfaceVariant)
        }
    }

    private var signInSection: some View {
        VStack(spacing: DomatSpacing.md) {
            // Google Sign-In Button
            DomatButton(
                "Sign in with Google",
                style: .outlined,
                isLoading: isLoading,
                icon: "globe"
            ) {
                handleGoogleSignIn()
            }

            // Apple Sign-In Button
            DomatButton(
                "Sign in with Apple",
                style: .filled,
                icon: "apple.logo"
            ) {
                // Future implementation
            }
        }
    }

    private var footerSection: some View {
        VStack(spacing: DomatSpacing.sm) {
            HStack(spacing: DomatSpacing.xs) {
                Text("Don't have an account?").font(DomatTypography.bodyMedium).foregroundStyle(colors.onSurfaceVariant)

                Button("Register") {
                    router.navigate(to: .register)
                }.font(DomatTypography.labelLarge).foregroundStyle(colors.primary)
            }

            Button("Forgot Password?") {
                router.navigate(to: .forgotPassword)
            }.font(DomatTypography.labelMedium).foregroundStyle(colors.tertiary)
        }.padding(.bottom, DomatSpacing.lg)
    }

    // MARK: - Actions

    private func handleGoogleSignIn() {
        // In production this triggers the shared ViewModel:
        // vm.send(AuthIntent.OnGoogleSignInClicked())
        // Then observe AuthEffect.LaunchGoogleSignIn to present
        // the native Google Sign-In flow.
        isLoading = true
        // Placeholder - will be connected to KMP ViewModel
    }
}

#Preview {
    NavigationStack {
        LoginView()
    }.environmentObject(NavigationRouter()).domatTheme()
}
