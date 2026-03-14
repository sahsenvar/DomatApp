import SwiftUI

struct ForgotPasswordView: View {
    @EnvironmentObject private var router: NavigationRouter
    @Environment(\.domatColors) private var colors

    @State private var email = ""
    @State private var isSubmitted = false

    var body: some View {
        VStack(spacing: DomatSpacing.lg) {
            Spacer().frame(height: DomatSpacing.xl)

            if isSubmitted {
                successContent
            } else {
                formContent
            }

            Spacer()
        }.padding(.horizontal, DomatSpacing.lg).background(colors.background).navigationTitle("Reset Password").navigationBarTitleDisplayMode(.inline)
    }

    // MARK: - Form

    private var formContent: some View {
        VStack(spacing: DomatSpacing.lg) {
            Image(systemName: "lock.rotation").font(.system(size: 48)).foregroundStyle(colors.primary)

            Text("Enter your email address and we'll send you a link to reset your password.").font(DomatTypography.bodyMedium).foregroundStyle(colors.onSurfaceVariant).multilineTextAlignment(.center)

            DomatTextField(
                label: "Email",
                text: $email,
                placeholder: "you@example.com",
                leadingIcon: "envelope",
                keyboardType: .emailAddress
            )

            DomatButton("Send Reset Link") {
                withAnimation {
                    isSubmitted = true
                }
            }
        }
    }

    // MARK: - Success State

    private var successContent: some View {
        VStack(spacing: DomatSpacing.lg) {
            Image(systemName: "checkmark.circle.fill").font(.system(size: 64)).foregroundStyle(colors.primary)

            Text("Check your email").font(DomatTypography.headlineSmall).foregroundStyle(colors.onBackground)

            Text("We've sent a password reset link to \(email)").font(DomatTypography.bodyMedium).foregroundStyle(colors.onSurfaceVariant).multilineTextAlignment(.center)

            DomatButton("Back to Login", style: .outlined) {
                router.popBack()
            }
        }
    }
}

#Preview {
    NavigationStack {
        ForgotPasswordView()
    }.environmentObject(NavigationRouter()).domatTheme()
}
