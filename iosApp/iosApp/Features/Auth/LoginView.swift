import SwiftUI
import Shared

struct LoginView: View {
    @EnvironmentObject private var router: NavigationRouter
    @Environment(\.domatColors) private var colors

    @StateObject private var vm = koinViewModel(
        koinHelper.loginViewModel(),
        initialState: LoginUiState(isLoading: false)
    )

    var body: some View {
        ZStack {
            colors.surface.ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(spacing: 0) {
                    heroSection
                    contentSection
                    footerSection
                }
                .frame(maxWidth: .infinity)
            }
        }
        .ignoresSafeArea(edges: .top)
        .onEffect(from: vm) { effect in
            if effect is LoginEffectNavigateToLocationSelection {
                router.navigate(to: .locationSelection)
            }
        }
    }

    // MARK: - Hero Section

    private var heroSection: some View {
        ZStack(alignment: .bottomLeading) {
            // Hero görseli — tam genişlik, 442pt yükseklik
            Group {
                if let img = mokoImage("img_hero_login") {
                    Image(uiImage: img)
                        .resizable()
                        .scaledToFill()
                } else {
                    colors.primaryContainer
                }
            }
            .frame(maxWidth: .infinity, minHeight: 442, maxHeight: 442)
            .clipped()

            // Gradient overlay — transparent → siyah %70
            LinearGradient(
                stops: [
                    .init(color: .clear, location: 0.0),
                    .init(color: .black.opacity(0.2), location: 0.5),
                    .init(color: .black.opacity(0.7), location: 1.0),
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(maxWidth: .infinity, minHeight: 442, maxHeight: 442)

            // Badge + Uygulama adı (alt-sol) — Figma: padding t=321,b=32,l=24,r=24 (mainAxis=MAX → content bottom-aligned)
            VStack(alignment: .leading, spacing: 16) {
                DomatHeroBadge(text: "Taze & Yerel", systemIcon: "leaf.fill")

                Text("DomatApp")
                    .font(DomatTypography.displaySmallExtraBold)
                    .foregroundStyle(Color.white)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
        .frame(maxWidth: .infinity, minHeight: 442, maxHeight: 442)
        // Figma: hero section düz alt kenar, köşe yuvarlama yok, gölge yok
    }

    // MARK: - Content Section
    // Figma: Content Body — padding t=32, b=16, l=24, r=24

    private var contentSection: some View {
        VStack(alignment: .center, spacing: 0) {
            // Figma: VP:margin → bottom padding=32 (spacer before button)
            Text("Haftalık olarak en taze ürünleri sitenize/kapınıza kadar getiriyoruz")
                .font(.system(size: 18))
                .foregroundStyle(colors.onSurfaceVariant)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                .padding(.bottom, 32)

            // Figma: Spacer rectangle 124pt
            Color.clear.frame(height: 124)

            DomatGoogleSignInButton(
                text: "Google ile Devam Et",
                icon: mokoImage("ic_google")
            ) {
                vm.send(LoginIntentOnGoogleSignInClicked.shared)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 24)
        .padding(.top, 32)
        .padding(.bottom, 16)
    }

    // MARK: - Footer (ToS)
    // Figma: Terms footer — padding t=32, b=24 (no horizontal on outer container)

    private var footerSection: some View {
        VStack(alignment: .center, spacing: 0) {
            tosText
                .frame(maxWidth: .infinity)
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 16)
        .padding(.top, 32)
        .padding(.bottom, 24)
    }

    private var tosText: some View {
        var full = AttributedString("Devam ederek, ")

        var link1 = AttributedString("Hizmet Şartlarımızı")
        link1.foregroundColor = colors.primary
        link1.underlineStyle = .single

        var connector = AttributedString(" ve ")

        var link2 = AttributedString("Gizlilik Politikamızı")
        link2.foregroundColor = colors.primary
        link2.underlineStyle = .single

        let suffix = AttributedString(" kabul etmiş olursunuz.")

        return Text(full + link1 + connector + link2 + suffix)
            .font(DomatTypography.labelMedium)
            .foregroundStyle(colors.onSurfaceVariant)
            .multilineTextAlignment(.center)
    }

    // MARK: - MOKO Image Yükleyici

    private func mokoImage(_ name: String) -> UIImage? {
        if let url = Bundle.main.url(forResource: "DomatApp.core:resource", withExtension: "bundle"),
           let resourceBundle = Bundle(url: url),
           let img = UIImage(named: name, in: resourceBundle, with: nil) {
            return img
        }
        return nil
    }
}

#Preview {
    LoginView().environmentObject(NavigationRouter()).domatTheme()
}
