import SwiftUI
import Shared

// MARK: - ViewModel Binder

/// LocationSelection akışının koordinatörü.
/// ViewModel'ı bağlar, effect'lere göre navigasyon yapar.
/// LocationSelectionScreen saf UI view'ı — sadece uiState + onIntent alır.
struct LocationSelectionView: View {
    @EnvironmentObject private var router: NavigationRouter

    @StateObject private var vm = koinViewModel(
        koinHelper.locationSelectionViewModel(),
        initialState: LocationSelectionUiState(
            selectedBlock: nil,
            selectedApartment: nil,
            isConfirmEnabled: false,
            isBlokDropdownOpen: false,
            isDaireDropdownOpen: false
        )
    )

    var body: some View {
        LocationSelectionScreen(
            uiState: vm.state,
            onIntent: { vm.send($0) }
        )
        .navigationBarHidden(true)
        .onEffect(from: vm) { effect in
            switch effect {
            case is LocationSelectionEffectNavigateToHome:
                router.switchToMain()
            case is LocationSelectionEffectNavigateBack:
                router.popBack()
            default:
                break
            }
        }
    }
}

// MARK: - Pure UI Screen (uiState + onIntent)

private let blokItems = ["A1", "A2", "A3", "B1", "B2", "C1"]
private let daireItems = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]

struct LocationSelectionScreen: View {
    let uiState: LocationSelectionUiState
    let onIntent: (LocationSelectionIntent) -> Void

    @Environment(\.domatColors) private var colors

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(spacing: 0) {
                // Header
                LocationSelectionHeader(onBack: { onIntent(LocationSelectionIntentGoBack.shared) })

                // İçerik
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 0) {
                        // Kilitli konum kartları
                        LocationCardRow(
                            label: "İLÇE / İL",
                            value: "Tuzla/İstanbul",
                            alpha: 0.6,
                            showConnector: true
                        )
                        LocationCardRow(
                            label: "MAHALLE",
                            value: "Aydınlı Mh.",
                            alpha: 0.8,
                            showConnector: true
                        )
                        LocationCardRow(
                            label: "SİTE / APARTMAN",
                            value: "Dumankaya Adres Lobi.",
                            alpha: 1.0,
                            showConnector: true
                        )

                        // Bağlantı çizgisi
                        HStack {
                            Spacer().frame(width: 36)
                            Rectangle()
                                .fill(colors.outlineVariant)
                                .frame(width: 2, height: 24)
                            Spacer()
                        }

                        // Blok + Daire dropdown'ları
                        HStack(spacing: 16) {
                            LocationDropdown(
                                label: "BLOK NO",
                                value: uiState.selectedBlock ?? "Seçiniz",
                                isActive: uiState.isBlokDropdownOpen || uiState.selectedBlock != nil,
                                isOpen: uiState.isBlokDropdownOpen,
                                items: blokItems,
                                selectedItem: uiState.selectedBlock,
                                onTap: { onIntent(LocationSelectionIntentToggleBlokDropdown.shared) },
                                onSelect: { item in onIntent(LocationSelectionIntentSelectBlock(block: item)) }
                            )
                            LocationDropdown(
                                label: "DAİRE NO",
                                value: uiState.selectedApartment ?? "Seçiniz",
                                isActive: uiState.isDaireDropdownOpen || uiState.selectedApartment != nil,
                                isOpen: uiState.isDaireDropdownOpen,
                                items: daireItems,
                                selectedItem: uiState.selectedApartment,
                                onTap: { onIntent(LocationSelectionIntentToggleDaireDropdown.shared) },
                                onSelect: { item in onIntent(LocationSelectionIntentSelectApartment(apartment: item)) }
                            )
                        }
                        .padding(.horizontal, 16)
                    }
                    .padding(.top, 16)
                    .padding(.bottom, 120) // bottom bar için boşluk
                }
            }

            // Bottom action bar
            VStack(spacing: 0) {
                Divider().background(colors.outlineVariant)
                Button {
                    onIntent(LocationSelectionIntentConfirm.shared)
                } label: {
                    HStack(spacing: 8) {
                        Text("Devam Et")
                            .font(DomatTypography.titleMedium)
                        Image(systemName: "arrow.right")
                            .font(.system(size: 14, weight: .semibold))
                    }
                    .foregroundStyle(colors.onPrimary)
                    .frame(maxWidth: .infinity)
                    .frame(height: 60)
                    .background(uiState.isConfirmEnabled ? colors.primary : colors.outlineVariant)
                    .clipShape(RoundedRectangle(cornerRadius: DomatShape.medium))
                }
                .disabled(!uiState.isConfirmEnabled)
                .padding(.horizontal, 16)
                .padding(.top, 17)
                .padding(.bottom, 32)
            }
            .background(colors.surface)
        }
        .background(colors.surface)
    }
}

// MARK: - Header

private struct LocationSelectionHeader: View {
    let onBack: () -> Void
    @Environment(\.domatColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBack) {
                    ZStack {
                        Circle()
                            .fill(colors.surfaceVariant)
                            .frame(width: 48, height: 48)
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundStyle(colors.onSurface)
                    }
                }

                Spacer()

                Text("Teslimat Bölgesi")
                    .font(DomatTypography.headlineSmall)
                    .foregroundStyle(colors.onSurface)

                Spacer()

                // Dengeleme için boşluk
                Spacer().frame(width: 48)
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)
            .padding(.bottom, 8)

            // Progress steps (4 adım, 2. adım aktif — 0-indexed: step 1)
            LocationProgressSteps(totalSteps: 4, currentStep: 1)
                .padding(.horizontal, 0)
                .padding(.bottom, 12)
        }
        .background(colors.surface.opacity(0.9))
        .overlay(Divider(), alignment: .bottom)
    }
}

// MARK: - Progress Steps

private struct LocationProgressSteps: View {
    let totalSteps: Int
    let currentStep: Int
    @Environment(\.domatColors) private var colors

    var body: some View {
        HStack(spacing: 12) {
            ForEach(0..<totalSteps, id: \.self) { index in
                Capsule()
                    .fill(index <= currentStep ? colors.primary : colors.outlineVariant)
                    .frame(width: index == currentStep ? 32 : 6, height: 6)
                    .animation(.spring(dampingFraction: 0.6, blendDuration: 0), value: currentStep)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
    }
}

// MARK: - Locked Location Card

private struct LocationCardRow: View {
    let label: String
    let value: String
    let alpha: Double
    let showConnector: Bool
    @Environment(\.domatColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 12) {
                // Onay ikonu kutusu
                RoundedRectangle(cornerRadius: 8)
                    .fill(colors.primary.opacity(0.15))
                    .frame(width: 40, height: 40)
                    .overlay {
                        Image(systemName: "checkmark")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(colors.primary)
                    }

                VStack(alignment: .leading, spacing: 2) {
                    Text(label)
                        .font(DomatTypography.labelLarge)
                        .foregroundStyle(colors.onSurfaceVariant)
                    Text(value)
                        .font(DomatTypography.titleLarge)
                        .foregroundStyle(colors.onSurface)
                        .lineLimit(1)
                }

                Spacer()

                Image(systemName: "lock")
                    .font(.system(size: 13))
                    .foregroundStyle(colors.onSurfaceVariant)
            }
            .padding(17)
            .background(colors.surfaceVariant.opacity(0.5))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .opacity(alpha)
            .padding(.horizontal, 16)

            if showConnector {
                HStack {
                    Spacer().frame(width: 36)
                    Rectangle()
                        .fill(colors.outlineVariant)
                        .frame(width: 2, height: 24)
                    Spacer()
                }
            }
        }
    }
}

// MARK: - Input Dropdown

private struct LocationDropdown: View {
    let label: String
    let value: String
    let isActive: Bool
    let isOpen: Bool
    let items: [String]
    let selectedItem: String?
    let onTap: () -> Void
    let onSelect: (String) -> Void

    @Environment(\.domatColors) private var colors

    var body: some View {
        ZStack(alignment: .top) {
            // Kapalı görünüm
            Button(action: onTap) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(label)
                        .font(DomatTypography.labelLarge)
                        .foregroundStyle(isActive ? colors.primary : colors.onSurfaceVariant)
                    Text(value)
                        .font(DomatTypography.titleLarge)
                        .foregroundStyle(colors.onSurface)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(isActive ? 18 : 17)
                .background(colors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .strokeBorder(
                            isActive ? colors.primary : colors.outlineVariant,
                            lineWidth: isActive ? 2 : 1
                        )
                )
                .shadow(color: .black.opacity(0.05), radius: 1, x: 0, y: 1)
                .overlay(alignment: .trailing) {
                    Image(systemName: "chevron.down")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(isActive ? colors.primary : colors.onSurfaceVariant)
                        .rotationEffect(.degrees(isOpen ? 180 : 0))
                        .animation(.easeInOut(duration: 0.2), value: isOpen)
                        .padding(.trailing, 14)
                }
            }
            .buttonStyle(.plain)

            // Açık dropdown listesi
            if isOpen {
                VStack(spacing: 0) {
                    Spacer().frame(height: 70) // dropdown başlık yüksekliği
                    ScrollView(showsIndicators: false) {
                        VStack(spacing: 0) {
                            ForEach(items, id: \.self) { item in
                                Button {
                                    onSelect(item)
                                } label: {
                                    HStack {
                                        Text(item)
                                            .font(DomatTypography.bodyLarge)
                                            .foregroundStyle(colors.onSurface)
                                        Spacer()
                                        if item == selectedItem {
                                            Image(systemName: "checkmark")
                                                .font(.system(size: 14, weight: .semibold))
                                                .foregroundStyle(colors.primary)
                                        }
                                    }
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 12)
                                    .background(item == selectedItem ? colors.primary.opacity(0.08) : Color.clear)
                                }
                                .buttonStyle(.plain)
                                Divider().background(colors.outlineVariant)
                            }
                        }
                    }
                    .frame(maxHeight: 180)
                    .background(colors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .strokeBorder(colors.primary, lineWidth: 2)
                    )
                    .shadow(color: .black.opacity(0.1), radius: 8, x: 0, y: 4)
                }
                .zIndex(10)
            }
        }
        .frame(maxWidth: .infinity)
    }
}

// MARK: - Preview

#Preview {
    NavigationStack {
        LocationSelectionView()
    }
    .environmentObject(NavigationRouter())
    .domatTheme()
}
