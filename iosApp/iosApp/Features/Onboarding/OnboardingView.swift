import SwiftUI

struct OnboardingView: View {
    @EnvironmentObject private var router: NavigationRouter
    @Environment(\.domatColors) private var colors

    @State private var currentPage = 0

    private let pages: [OnboardingPage] = [
        OnboardingPage(
            icon: "leaf.fill",
            title: "Welcome to DomatApp",
            description: "Your fresh produce marketplace, right at your fingertips."
        ),
        OnboardingPage(
            icon: "cart.fill",
            title: "Browse & Order",
            description: "Explore a wide selection of fresh, locally sourced products."
        ),
        OnboardingPage(
            icon: "shippingbox.fill",
            title: "Fast Delivery",
            description: "Get your orders delivered fresh to your doorstep."
        ),
    ]

    var body: some View {
        VStack(spacing: 0) {
            // Page Content
            TabView(selection: $currentPage) {
                ForEach(pages.indices, id: \.self) {
                    index in
                    pageView(pages[index]).tag(index)
                }
            }.tabViewStyle(.page(indexDisplayMode: .never)).animation(.easeInOut, value: currentPage)

            // Bottom Section
            VStack(spacing: DomatSpacing.lg) {
                // Page Indicator
                HStack(spacing: DomatSpacing.sm) {
                    ForEach(pages.indices, id: \.self) {
                        index in
                        Capsule().fill(index == currentPage ? colors.primary: colors.outlineVariant).frame(width: index == currentPage ? 24: 8, height: 8).animation(.easeInOut(duration: 0.2), value: currentPage)
                    }
                }

                // Action Buttons
                if currentPage == pages.count - 1 {
                    DomatButton("Get Started") {
                        router.switchToAuth()
                    }
                } else {
                    HStack {
                        DomatButton("Skip", style: .text) {
                            router.switchToAuth()
                        }
                        DomatButton("Next") {
                            withAnimation {
                                currentPage += 1
                            }
                        }
                    }
                }
            }.padding(.horizontal, DomatSpacing.lg).padding(.bottom, DomatSpacing.xl)
        }.background(colors.background)
    }

    // MARK: - Page View

    private func pageView(_ page: OnboardingPage) -> some View {
        VStack(spacing: DomatSpacing.lg) {
            Spacer()

            Image(systemName: page.icon).font(.system(size: 80)).foregroundStyle(colors.primary)

            VStack(spacing: DomatSpacing.sm) {
                Text(page.title).font(DomatTypography.headlineMedium).foregroundStyle(colors.onBackground).multilineTextAlignment(.center)

                Text(page.description).font(DomatTypography.bodyLarge).foregroundStyle(colors.onSurfaceVariant).multilineTextAlignment(.center).padding(.horizontal, DomatSpacing.lg)
            }

            Spacer()
        }
    }
}

// MARK: - Model

private struct OnboardingPage {
    let icon: String
    let title: String
    let description: String
}

#Preview {
    OnboardingView().environmentObject(NavigationRouter()).domatTheme()
}
