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
    val icon: ImageResource,          // dev.icerock.moko.resources.ImageResource
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

## Image / Drawable Sistemi

### Kural: Her şey MR.images üzerinden

Projede Compose Resources (`Res.drawable.*`) kullanılmaz. Tüm görseller moko-resources `MR.images.*` üzerinden alınır.

```kotlin
// Yanlış ❌ — Compose Resources
import domatapp.feature.onboarding.presentation.generated.resources.Res
import org.jetbrains.compose.resources.painterResource

painterResource(Res.drawable.ic_arrow_back)

// Doğru ✓ — moko-resources
import com.domatapp.core.resource.MR
import dev.icerock.moko.resources.compose.painterResource

painterResource(MR.images.ic_arrow_back)
```

### Image Tipi

UiModel içinde image referansı tutulurken:

```kotlin
// Yanlış ❌
import org.jetbrains.compose.resources.DrawableResource
val icon: DrawableResource

// Doğru ✓
import dev.icerock.moko.resources.ImageResource
val icon: ImageResource
```

### Kaynak Dosya Konumu ve Format Kuralları

Tüm görseller `core/resource/src/commonMain/moko-resources/images/` altına eklenir.

| Format | Davranış |
|--------|----------|
| `.svg` | Doğrudan `images/` içine koyulur |
| `.png` / `.jpg` | Dosya adına density suffix eklenir: `img_name@1x.png`, `img_name@2x.png` |

**PNG Kritik Kural:** PNG dosyaları `@1x` / `@2x` / `@3x` suffix'i olmadan koyulursa moko-resources "unknown scale" hatası verir ve Android drawable olarak üretilmez.

```
images/
├── ic_arrow_back.svg              ✓ doğru
├── img_welcome@1x.png             ✓ doğru (@1x suffix zorunlu)
├── img_welcome.png                ✗ yanlış (scale bilinmiyor, ignore edilir)
└── 1x/img_welcome.png             ✗ yanlış (subdirectory çalışmıyor)
```

### VectorDrawable XML → SVG Dönüşümü

Android VectorDrawable XML formatı (`<vector xmlns:android=...>`) moko-resources tarafından desteklenmez. SVG formatına dönüştürülmesi gerekir.

| VectorDrawable | SVG |
|----------------|-----|
| `<vector android:width android:height android:viewportWidth android:viewportHeight>` | `<svg width height viewBox="0 0 W H">` |
| `<path android:pathData android:fillColor>` | `<path d fill>` |
| `<group android:translateX android:translateY>` | `<g transform="translate(x, y)">` |
| `#AARRGGBB` renk (Android) | `#RRGGBB` + `fill-opacity` (SVG) |

Dönüşüm scripti: `/tmp/convert_drawables.py`

### Compose Resources Üretimini Engelleme

`composeMultiplatform` plugin'i olan her modülde `Res.kt` üretimi kapatılır:

```kotlin
// Her modülün build.gradle.kts dosyasına eklenir
compose.resources {
    generateResClass = never
}
```

`composeResources/` klasörü oluşturulmaz. `compose.components.resources` bağımlılığı eklenmez.

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
