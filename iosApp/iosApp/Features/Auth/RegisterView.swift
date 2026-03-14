import SwiftUI

struct RegisterView: View {
    @EnvironmentObject private var router: NavigationRouter
    @Environment(\.domatColors) private var colors

    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""

    var body: some View {
        ScrollView {
            VStack(spacing: DomatSpacing.lg) {
                // Header
                VStack(spacing: DomatSpacing.sm) {
                    Text("Create Account").font(DomatTypography.headlineMedium).foregroundStyle(colors.onBackground)

                    Text("Fill in your details to get started").font(DomatTypography.bodyMedium).foregroundStyle(colors.onSurfaceVariant)
                }.padding(.top, DomatSpacing.xl)

                // Form Fields
                VStack(spacing: DomatSpacing.md) {
                    DomatTextField(
                        label: "Email",
                        text: $email,
                        placeholder: "you@example.com",
                        leadingIcon: "envelope",
                        keyboardType: .emailAddress
                    )

                    DomatTextField(
                        label: "Password",
                        text: $password,
                        placeholder: "Create a password",
                        isSecure: true,
                        leadingIcon: "lock"
                    )

                    DomatTextField(
                        label: "Confirm Password",
                        text: $confirmPassword,
                        placeholder: "Confirm your password",
                        isSecure: true,
                        leadingIcon: "lock"
                    )
                }

                // Register Button
                DomatButton("Create Account") {
                    // Will be connected to KMP ViewModel
                }

                // Social Sign-Up
                VStack(spacing: DomatSpacing.sm) {
                    dividerWithText("or continue with")

                    HStack(spacing: DomatSpacing.md) {
                        DomatButton("Google", style: .outlined, icon: "globe") {
                        }
                        DomatButton("Apple", style: .outlined, icon: "apple.logo") {
                        }
                    }
                }
            }.padding(.horizontal, DomatSpacing.lg)
        }.background(colors.background).navigationTitle("Register").navigationBarTitleDisplayMode(.inline)
    }

    private func dividerWithText(_ text: String) -> some View {
        HStack {
            Rectangle().frame(height: 1).foregroundStyle(colors.outlineVariant)
            Text(text).font(DomatTypography.labelMedium).foregroundStyle(colors.onSurfaceVariant).fixedSize()
            Rectangle().frame(height: 1).foregroundStyle(colors.outlineVariant)
        }
    }
}

#Preview {
    NavigationStack {
        RegisterView()
    }.environmentObject(NavigationRouter()).domatTheme()
}
