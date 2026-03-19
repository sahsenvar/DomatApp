import SwiftUI
import Shared

// MARK: - ViewModel Binder (Coordinator)

/// Onboarding akışının koordinatörü.
/// ViewModel'ı bağlar, sayfa değişimlerini VM'a iletir ve effect'lere göre navigasyon yapar.
/// Sayfa içerikleri saf UI view'ları olarak ayrıştırılmıştır (uiState + onIntent pattern).
struct OnboardingView: View {
    @EnvironmentObject private var router: NavigationRouter

    @StateObject private var vm = koinViewModel(
        koinHelper.onboardingWelcomeViewModel(),
        initialState: PresentationOnboardingWelcomeUiState(currentPage: PresentationOnboardingPage.welcome, targetPage: nil)
    )

    @State private var currentPage: Int = 0

    var body: some View {
        VStack(spacing: 0) {
            TabView(selection: $currentPage) {
                OnboardingWelcomePageContent()
                    .tag(0)
                OnboardingPricingPageContent()
                    .tag(1)
                OnboardingCommunityPageContent()
                    .tag(2)
                OnboardingTrustPageContent()
                    .tag(3)
                OnboardingEffortlessPageContent()
                    .tag(4)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))

            OnboardingBottomBarView(
                uiState: vm.state,
                onIntent: { vm.send($0) }
            )
        }
        .ignoresSafeArea(edges: .bottom)
        // Kullanıcı swipe yaparsa VM'ı güncelle
        .onChange(of: currentPage) { _, page in
            vm.send(PresentationOnboardingWelcomeIntentOnPageChanged(page: Int32(page)))
        }
        // VM targetPage set ederse programatik scroll yap
        .onChange(of: vm.state.targetPage?.index) { _, idx in
            if let idx = idx {
                withAnimation(.easeInOut(duration: 0.3)) {
                    currentPage = Int(idx)
                }
                vm.send(PresentationOnboardingWelcomeIntentOnScrollConsumed.shared)
            }
        }
        // Login'e geçiş effect'i
        .onEffect(from: vm) { effect in
            if effect is PresentationOnboardingWelcomeEffectNavigateToLogin {
                router.switchToAuth()
            }
        }
    }
}

// MARK: - Bottom Bar (uiState + onIntent)

/// Sayfa noktaları ve devam butonu — saf UI, VM bağlantısı yok.
private struct OnboardingBottomBarView: View {
    let uiState: PresentationOnboardingWelcomeUiState
    let onIntent: (PresentationOnboardingWelcomeIntent) -> Void

    @Environment(\.domatColors) private var colors

    private var buttonText: String {
        switch uiState.currentPage {
        case PresentationOnboardingPage.welcome:    return "Devam Et →"
        case PresentationOnboardingPage.pricing:    return "Hmm.. Güzelmiş. Başka? →"
        case PresentationOnboardingPage.community:  return "Uygunsa kalitesi kötü müdür? →"
        case PresentationOnboardingPage.trust:      return "Ben pazardan alıyorum 😬"
        case PresentationOnboardingPage.effortless: return "Süpermiş! Hadi başlayalım →"
        default:                                    return "Devam Et →"
        }
    }

    private var activeIndex: Int { Int(uiState.currentPage.index) }

    var body: some View {
        VStack(spacing: 20) {
            // Progress dots
            HStack(spacing: 8) {
                ForEach(0..<5, id: \.self) { index in
                    Capsule()
                        .fill(index == activeIndex ? colors.primary : colors.outlineVariant)
                        .frame(width: index == activeIndex ? 24 : 8, height: 8)
                        .animation(.easeInOut(duration: 0.2), value: activeIndex)
                }
            }

            // Continue button
            Button {
                onIntent(PresentationOnboardingWelcomeIntentOnContinueClicked.shared)
            } label: {
                Text(buttonText)
                    .font(DomatTypography.titleMedium)
                    .foregroundStyle(colors.onPrimary)
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(colors.primary)
                    .clipShape(RoundedRectangle(cornerRadius: DomatShape.medium))
            }
        }
        .padding(.horizontal, 24)
        .padding(.top, 20)
        .padding(.bottom, 40)
        .background(colors.surface)
    }
}

// MARK: - MOKO Image Loader (file-private)

private func mokoImage(_ name: String) -> UIImage? {
    if let url = Bundle.main.url(forResource: "DomatApp.core:resource", withExtension: "bundle"),
       let resourceBundle = Bundle(url: url),
       let img = UIImage(named: name, in: resourceBundle, with: nil) {
        return img
    }
    return nil
}

// MARK: - Page 0: Welcome

private struct OnboardingWelcomePageContent: View {
    @Environment(\.domatColors) private var colors

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(alignment: .leading, spacing: 0) {
                Spacer().frame(height: 32)

                // Mahalle görseli
                Group {
                    if let img = mokoImage("img_welcome_neighborhood") {
                        Image(uiImage: img)
                            .resizable()
                            .scaledToFill()
                    } else {
                        colors.primary.opacity(0.1)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 230)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .clipped()

                Spacer().frame(height: 32)

                // Başlık: "Hoş Geldiniz 👋\n[GREEN]Taze sebze ve meyveler[/GREEN]\nmahallenize geliyor"
                Text(welcomeTitle)
                    .font(DomatTypography.displayMedium)

                Spacer().frame(height: 16)

                Text("Her hafta en taze domatesleri doğrudan üreticiden sitenize getiriyoruz. Stres yok, market gezmek yok, sürpriz yok.")
                    .font(DomatTypography.bodyLarge)
                    .foregroundStyle(colors.onSurfaceVariant)

                Spacer().frame(height: 32)
            }
            .padding(.horizontal, 16)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    private var welcomeTitle: AttributedString {
        var line1 = AttributedString("Hoş Geldiniz 👋\n")
        line1.foregroundColor = colors.onBackground

        var highlight = AttributedString("Taze sebze ve meyveler")
        highlight.foregroundColor = colors.primary

        var line3 = AttributedString("\nmahallenize geliyor")
        line3.foregroundColor = colors.onBackground

        return line1 + highlight + line3
    }
}

// MARK: - Page 1: Pricing (Supply Chain)

private struct OnboardingPricingPageContent: View {
    @Environment(\.domatColors) private var colors

    struct RowData {
        let imageName: String
        let title: String
        let subtitle: String
        let isActive: Bool
        let isConsumer: Bool
        let showConnector: Bool
    }

    let rows: [RowData] = [
        RowData(imageName: "ic_pricing_producer",   title: "Üretici",           subtitle: "Doğrudan Kaynak",          isActive: true,  isConsumer: false, showConnector: true),
        RowData(imageName: "ic_pricing_wholesaler", title: "Toptancı",          subtitle: "Kâr Marjı + Depolama",     isActive: false, isConsumer: false, showConnector: true),
        RowData(imageName: "ic_pricing_retail",     title: "Perakende Mağaza",  subtitle: "Kira + Personel",          isActive: false, isConsumer: false, showConnector: true),
        RowData(imageName: "ic_pricing_consumer",   title: "Siz",               subtitle: "Ortalama %40 tasarruf edin", isActive: true, isConsumer: true, showConnector: false),
    ]

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                VStack(spacing: 16) {
                    Text("Neden bu kadar\nuygun fiyatlı?")
                        .font(DomatTypography.headlineLarge)
                        .foregroundStyle(colors.onBackground)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)

                    Text("Çünkü arada aracı yok. Domatesler doğrudan üreticiden size geliyor. Toptancı yok, depo yok, dükkan kirası yok, gereksiz maliyetler yok.")
                        .font(DomatTypography.bodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                }
                .padding(.top, 48)
                .padding(.bottom, 40)
                .padding(.horizontal, 32)

                VStack(spacing: 0) {
                    ForEach(rows.indices, id: \.self) { i in
                        SupplyChainRow(row: rows[i])
                    }
                }
                .padding(.horizontal, 35)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

private struct SupplyChainRow: View {
    let row: OnboardingPricingPageContent.RowData
    @Environment(\.domatColors) private var colors

    var body: some View {
        let iconBg: SwiftUI.Color = row.isConsumer ? colors.primary :
                            row.isActive   ? colors.primary.opacity(0.2) : colors.outlineVariant
        let titleColor: SwiftUI.Color   = row.isActive ? colors.onSurface : colors.onSurfaceVariant
        let subtitleColor: SwiftUI.Color = row.isActive ? colors.primary : colors.onSurface
        let connectorColor: SwiftUI.Color = row.isActive ? colors.primary.opacity(0.3) : colors.outlineVariant

        HStack(alignment: .center, spacing: 0) {
            VStack(spacing: 0) {
                ZStack {
                    Circle()
                        .fill(iconBg)
                        .frame(width: 48, height: 48)
                    if let img = mokoImage(row.imageName) {
                        Image(uiImage: img)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 22, height: 22)
                    } else {
                        Image(systemName: "circle.fill")
                            .foregroundStyle(row.isConsumer ? colors.onPrimary : colors.primary)
                    }
                    if !row.isActive && !row.isConsumer {
                        Text("✕")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundStyle(colors.error)
                    }
                }

                if row.showConnector {
                    Rectangle()
                        .fill(connectorColor)
                        .frame(width: 2, height: 40)
                }
            }
            .frame(width: 64)

            VStack(alignment: .leading, spacing: 2) {
                Text(row.title)
                    .font(.system(size: 16, weight: row.isActive ? .bold : .medium))
                    .strikethrough(!row.isActive)
                    .foregroundStyle(titleColor)
                Text(row.subtitle)
                    .font(DomatTypography.bodySmall)
                    .fontWeight(row.isConsumer ? .semibold : .regular)
                    .foregroundStyle(subtitleColor)
            }
            .padding(.vertical, 16)

            Spacer()
        }
        .opacity((!row.isActive && !row.isConsumer) ? 0.4 : 1.0)
    }
}

// MARK: - Page 2: Community Power

private struct OnboardingCommunityPageContent: View {
    @Environment(\.domatColors) private var colors

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                Spacer().frame(height: 48)

                CommunityHeroCard()
                    .padding(.horizontal, 32)

                Spacer().frame(height: 32)

                VStack(spacing: 16) {
                    communityTitle
                        .frame(maxWidth: .infinity)

                    Text("Mahallenizde ne kadar çok kişi sipariş verirse, nakliye maliyeti o kadar düşer. Biz de bu tasarrufu doğrudan size yansıtırız. Birlikte sipariş verdiğimizde herkes kazanır.")
                        .font(DomatTypography.bodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                }
                .padding(.horizontal, 32)

                Spacer().frame(height: 32)
            }
            .frame(maxWidth: .infinity)
        }
    }

    private var communityTitle: some View {
        var line1 = AttributedString("Birlikte alalım,\n")
        var highlight = AttributedString("daha az ödeyelim")
        highlight.foregroundColor = colors.primary
        return Text(line1 + highlight)
            .font(DomatTypography.headlineLarge)
            .foregroundStyle(colors.onBackground)
            .multilineTextAlignment(.center)
    }
}

private struct CommunityHeroCard: View {
    @Environment(\.domatColors) private var colors

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [colors.primary.opacity(0.05), Color.clear],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            Circle()
                .fill(colors.primary.opacity(0.05))
                .frame(width: 160, height: 160)
                .blur(radius: 32)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                .offset(x: -40, y: -40)
            Circle()
                .fill(colors.primary.opacity(0.1))
                .frame(width: 160, height: 160)
                .blur(radius: 32)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomTrailing)
                .offset(x: 40, y: 40)

            VStack(spacing: 24) {
                OverlappingAvatarsView()
                TruckWithPriceView()
            }
            .padding(.vertical, 28)
        }
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.05), radius: 4, x: 0, y: 2)
    }
}

private struct OverlappingAvatarsView: View {
    @Environment(\.domatColors) private var colors

    var body: some View {
        ZStack(alignment: .leading) {
            avatarCircle(index: 0, bg: colors.primary.opacity(0.2), iconName: "ic_person_community")
            avatarCircle(index: 1, bg: colors.primary.opacity(0.3), iconName: "ic_person_community")
            avatarCircle(index: 2, bg: colors.primary.opacity(0.3), iconName: "ic_person_community")
            avatarCircle(index: 3, bg: colors.primary,              iconName: "ic_person_community_white")
        }
        .frame(width: 144, height: 48)
    }

    private func avatarCircle(index: Int, bg: SwiftUI.Color, iconName: String) -> some View {
        ZStack {
            Circle().fill(bg)
            Circle().strokeBorder(SwiftUI.Color.white, lineWidth: 4)
            if let img = mokoImage(iconName) {
                Image(uiImage: img).resizable().scaledToFit().frame(width: 16, height: 16)
            } else {
                Image(systemName: "person.fill")
                    .font(.system(size: 14))
                    .foregroundStyle(index == 3 ? SwiftUI.Color.white : colors.primary)
            }
        }
        .frame(width: 48, height: 48)
        .offset(x: CGFloat(index) * 32)
    }
}

private struct TruckWithPriceView: View {
    @Environment(\.domatColors) private var colors

    var body: some View {
        ZStack(alignment: .bottom) {
            Capsule()
                .fill(colors.outlineVariant)
                .frame(height: 4)
                .padding(.bottom, 16)

            HStack(alignment: .bottom) {
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.white)
                    .frame(width: 56, height: 44)
                    .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(colors.outlineVariant, lineWidth: 1))
                    .shadow(color: .black.opacity(0.05), radius: 1, x: 0, y: 1)
                    .overlay {
                        if let img = mokoImage("ic_delivery_truck_green") {
                            Image(uiImage: img).resizable().scaledToFit().frame(width: 24, height: 24)
                        } else {
                            Image(systemName: "truck.box.fill").foregroundStyle(colors.primary)
                        }
                    }
                    .offset(y: -24)

                Spacer()

                VStack(alignment: .trailing, spacing: 2) {
                    HStack(spacing: 4) {
                        if let img = mokoImage("ic_trending_down") {
                            Image(uiImage: img).resizable().frame(width: 12, height: 7)
                        } else {
                            Image(systemName: "arrow.down.right").font(.system(size: 10)).foregroundStyle(colors.primary)
                        }
                        Text("₺2.00")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(colors.primary)
                    }
                    Text("₺12.50")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(colors.onSurfaceVariant)
                        .strikethrough()
                }
                .offset(y: -32)
                .padding(.trailing, 16)
            }
        }
        .frame(width: 256, height: 96)
    }
}

// MARK: - Page 3: Trust & Safety

private struct OnboardingTrustPageContent: View {
    @Environment(\.domatColors) private var colors

    private let features: [(icon: String, text: String)] = [
        ("ic_feature_producer", "Üretici adı görünür"),
        ("ic_feature_location", "Üretici şehri görünür"),
        ("ic_feature_origin",   "Memnun kalmazsanız paranız iade!"),
    ]

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 0) {
                Spacer().frame(height: 48)

                // Kalkan daire
                ZStack {
                    Circle()
                        .fill(colors.primary.opacity(0.1))
                        .frame(width: 280, height: 280)
                    Circle()
                        .strokeBorder(
                            colors.primary.opacity(0.2),
                            style: StrokeStyle(lineWidth: 2, dash: [6, 6])
                        )
                        .frame(width: 248, height: 248)

                    ZStack(alignment: .bottomTrailing) {
                        if let shield = mokoImage("ic_shield_large") {
                            Image(uiImage: shield).resizable().scaledToFit().frame(width: 70, height: 90)
                        } else {
                            Image(systemName: "shield.fill").font(.system(size: 80)).foregroundStyle(colors.primary)
                        }
                        if let badge = mokoImage("ic_trust_wallet_badge") {
                            Image(uiImage: badge).resizable().scaledToFit().frame(width: 42, height: 41)
                                .offset(x: 8, y: 8)
                        } else {
                            Image(systemName: "wallet.pass.fill")
                                .font(.system(size: 24))
                                .foregroundStyle(colors.primary)
                                .padding(6)
                                .background(Color.white)
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                                .offset(x: 8, y: 8)
                        }
                    }
                }

                Spacer().frame(height: 32)

                VStack(spacing: 11) {
                    Text("Basit. Adil. Şeffaf.")
                        .font(DomatTypography.headlineLarge)
                        .foregroundStyle(colors.onBackground)
                        .multilineTextAlignment(.center)

                    Text("Siparişinizde eksik veya hasarlı bir ürün varsa, tutarı anında uygulama içi cüzdanınıza iade edilir.")
                        .font(DomatTypography.bodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                }
                .padding(.horizontal, 32)

                Spacer().frame(height: 24)

                VStack(spacing: 12) {
                    ForEach(features.indices, id: \.self) { i in
                        TrustFeatureRow(iconName: features[i].icon, text: features[i].text)
                    }
                }
                .padding(.horizontal, 32)

                Spacer().frame(height: 24)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

private struct TrustFeatureRow: View {
    let iconName: String
    let text: String
    @Environment(\.domatColors) private var colors

    var body: some View {
        HStack(spacing: 12) {
            RoundedRectangle(cornerRadius: 8)
                .fill(colors.primary.opacity(0.1))
                .frame(width: 40, height: 40)
                .overlay {
                    if let img = mokoImage(iconName) {
                        Image(uiImage: img).resizable().scaledToFit().frame(width: 17, height: 17)
                    } else {
                        Image(systemName: "checkmark").font(.system(size: 14)).foregroundStyle(colors.primary)
                    }
                }

            Text(text)
                .font(DomatTypography.bodySmall)
                .foregroundStyle(colors.onSurface)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(13)
        .background(colors.surfaceVariant.opacity(0.5))
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(colors.outlineVariant, lineWidth: 1))
    }
}

// MARK: - Page 4: Effortless Shopping

private struct OnboardingEffortlessPageContent: View {
    @Environment(\.domatColors) private var colors

    var body: some View {
        ZStack {
            colors.surface.ignoresSafeArea()

            // Radial glow'lar
            Circle()
                .fill(colors.primary.opacity(0.05))
                .frame(width: 256, height: 256)
                .blur(radius: 40)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
                .offset(x: -96, y: 96)
            Circle()
                .fill(colors.primary.opacity(0.1))
                .frame(width: 256, height: 256)
                .blur(radius: 40)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                .offset(x: 96, y: -96)

            VStack(spacing: 0) {
                Spacer().frame(height: 48)

                // İllüstrasyon
                Group {
                    if let img = mokoImage("img_effortless_illustration") {
                        Image(uiImage: img)
                            .resizable()
                            .scaledToFit()
                    } else {
                        ZStack {
                            Circle().fill(colors.primary.opacity(0.1)).frame(width: 260, height: 260)
                            VStack(spacing: 8) {
                                Image(systemName: "iphone").font(.system(size: 80)).foregroundStyle(colors.onSurface)
                                Image(systemName: "qrcode").font(.system(size: 40)).foregroundStyle(colors.primary)
                            }
                        }
                    }
                }
                .frame(maxWidth: 320)
                .frame(maxWidth: .infinity)
                .padding(.horizontal, 32)

                Spacer()

                VStack(spacing: 16) {
                    Text("Haftalık alışverişi\nzahmetsiz hale\ngetiriyoruz")
                        .font(DomatTypography.headlineLarge)
                        .foregroundStyle(colors.onBackground)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)

                    Text("Her hafta pazara gitmeye son. Bir kez sipariş verin, teslimat günü apartman önüne gelin, QR kodunuzu okutun ve domateslerinizi alın. İşte bu kadar!")
                        .font(DomatTypography.bodyMedium)
                        .foregroundStyle(colors.onSurfaceVariant)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                }
                .padding(.horizontal, 32)
                .padding(.bottom, 24)
            }
        }
    }
}

// MARK: - Preview

#Preview {
    OnboardingView()
        .environmentObject(NavigationRouter())
        .domatTheme()
}
