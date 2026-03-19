# Onboarding — Mimari & Tasarım Kararları

## Renk Sistemi (moko-resources)

### Kritik: Renk Format Farkı

moko-resources kaynak dosyaları `#RRGGBB` veya `#RRGGBBAA` (şeffaflık **sonda**) formatını bekler.
Android'in native `colors.xml` formatı ise `#AARRGGBB` (şeffaflık **başta**) şeklindedir.

| Format | Örnek | Anlam |
|--------|-------|-------|
| moko kaynak | `#13EC49` | opak yeşil |
| moko kaynak (şeffaf) | `#13EC4980` | yeşil %50 şeffaf |
| Android native | `#FF13EC49` | opak yeşil |

**Hata:** Kaynak dosyaya `#FF13EC49` (Android formatı) yazılırsa moko bunu `#RRGGBBAA` olarak okur ve `#49FF13EC` (pembe, %29 opaklık) üretir.
**Çözüm:** `core/resource/src/commonMain/moko-resources/base/colors.xml` dosyasında tüm renkler `#RRGGBB` / `#RRGGBBAA` formatında tutulmalıdır.

---

## Onboarding Akışı Mimarisi

### Navigasyon Yapısı

```
Route.OnboardingRoute.Welcome  →  HorizontalPager (5 sayfa)
                                   ├── Sayfa 0: Welcome içeriği
                                   ├── Sayfa 1: Effortless içeriği
                                   ├── Sayfa 2: Pricing içeriği
                                   ├── Sayfa 3: Community içeriği
                                   └── Sayfa 4: Trust içeriği
Route.OnboardingRoute.Login    →  Ayrı navigasyon hedefi (Google Sign-In)
Route.OnboardingRoute.LocationSelection  →  Ayrı navigasyon hedefi
```

Effortless / Pricing / Community / Trust artık birer navigasyon rotası değildir. `@NavigationScreen` ve `@NavigationEffectHandler` annotasyonları kaldırılmış, her biri `internal fun OnboardingXxxPageContent()` fonksiyonuna dönüştürülmüştür.

### Paylaşılan Alt Bar

`OnboardingWelcomeScreen` içindeki `HorizontalPager`'ın altında her sayfa için ortak bir alt bar bulunur:

```
┌─────────────────────────────────┐
│          HorizontalPager        │
│         (weight = 1f)           │
├─────────────────────────────────┤
│  ● ○ ○ ○ ○   (DomatProgressDots)│
│  [ Google ile Devam Et ]        │
└─────────────────────────────────┘
```

- **Nokta animasyonu:** `animateDpAsState` (genişlik 8dp→24dp) + `animateColorAsState`
- **Aktif sayfa:** `uiState.currentPage` (ViewModel'dan, `LaunchedEffect` ile güncellenir)
- **Herhangi bir sayfada** Google butonuna basınca Login rotasına gider (onboarding atlanabilir)

---

## State Yönetimi

### Kural: UiState > remember { mutableStateOf() }

Composable içinde `remember { mutableStateOf() }` kullanılmaz. Tüm dinamik durum ViewModel'ın UiState'inden gelir.

```kotlin
// Yanlış ❌
var currentPage by remember { mutableStateOf(0) }

// Doğru ✓
// ViewModel'da: OnboardingWelcomeUiState(currentPage = 0)
// Ekranda:
LaunchedEffect(pagerState.currentPage) {
    onIntent(OnboardingWelcomeIntent.OnPageChanged(pagerState.currentPage))
}
DomatProgressDots(activeIndex = uiState.currentPage)
```

`rememberPagerState()` ve `rememberScrollState()` Compose altyapı state tutucularıdır, kullanıcı tanımlı state değildir — bunlar muaf tutulur.

---

## UiModel Paterni

### Kural

- **Screen composable'lar** (sadece `@NavigationScreen` ile işaretlenenler) → UiState alır
- **Alt composable'lar** (özel bileşenler) → UiModel veri sınıfı alır, ham primitifler değil

### Örnekler

```kotlin
// Doğru ✓ — SupplyChainRow UiModel ile çalışır
data class SupplyChainRowUiModel(
    val icon: DrawableResource,
    val variant: SupplyChainRowVariant,
    val title: String,
    val subtitle: String,
    val showConnector: Boolean = true,
)

@Composable
private fun SupplyChainRow(uiModel: SupplyChainRowUiModel) { ... }

// Yanlış ❌ — 12 ham parametre ile çağrı
SupplyChainRow(icon, iconSize, iconBgColor, title, titleColor, titleFontWeight, ...)
```

**SupplyChainRowVariant enum:** `Producer | Inactive | Consumer`
Composable, variant'tan renk/boyut/opacity gibi görsel özellikleri türetir. UiModel Compose tiplerini (Color, FontWeight) taşımaz.

```kotlin
// Doğru ✓ — variant'tan türetme
val iconBgColor = when (uiModel.variant) {
    Producer -> primary20
    Inactive -> borderDefault
    Consumer -> primary
}
```

---

## String Kaynakları

Tüm görüntülenen metinler `MR.strings.*` aracılığıyla moko-resources'tan gelir.

```kotlin
// Yanlış ❌
Text(text = "Haftalık alışverişi\nzahmetsiz hale\ngetiriyoruz")

// Doğru ✓
Text(text = stringResource(MR.strings.onboarding_effortless_title))
```

Özel durumlar:
- **Yüzde işareti:** `%%40` → strings.xml'de `%%` ile escape edilir, composable'a `%40` olarak gelir
- **Annotated string parçaları:** `buildAnnotatedString { append(stringResource(MR.strings.xyz)) }` şeklinde ayrı parçalar halinde birleştirilir
- **HTML entity:** `&amp;` → strings.xml'de `&amp;` olarak yazılır (`Taze &amp; Yerel` → "Taze & Yerel")

---

## Dosya Organizasyonu

```
feature/onboarding/presentation/src/androidMain/screen/
├── OnboardingWelcomeScreen.kt   # @NavigationScreen — HorizontalPager + paylaşılan alt bar
│                                # private fun OnboardingWelcomePageContent()
├── OnboardingEffortlessScreen.kt  # internal fun OnboardingEffortlessPageContent()
├── OnboardingPricingScreen.kt     # internal fun OnboardingPricingPageContent()
│                                  # enum SupplyChainRowVariant, data class SupplyChainRowUiModel
├── OnboardingCommunityScreen.kt   # internal fun OnboardingCommunityPageContent()
│                                  # data class CommunityHeroCardUiModel
├── OnboardingTrustScreen.kt       # internal fun OnboardingTrustPageContent()
│                                  # data class TrustFeatureUiModel
└── OnboardingLoginScreen.kt       # @NavigationScreen — Google Sign-In ekranı
```
