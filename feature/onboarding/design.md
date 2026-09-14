# Onboarding — Feature Dökümanı

## Genel Bakış

Kullanıcının uygulamayı ilk açtığında gördüğü tanıtım akışıdır. Tek bir navigasyon rotasından (`OnboardingRoute.Welcome`) oluşur; içerik 5 sayfalık `HorizontalPager` ile sunulur.

### Ekran Akışı

```
OnboardingRoute.Welcome  (5-sayfalık HorizontalPager)
    │
    │  [Devam Et butonuna basınca]
    │
    ▼
AuthRoute.Login          (Google Sign-In — auth modülünde)
    │
    │  [Google ile giriş yapınca]
    │
    ▼
AuthRoute.LocationSelection  (Konum seçimi — auth modülünde)
    │
    │  [Devam Et'e basınca]
    │
    ▼
Main.Home
```

> Login ve LocationSelection ekranları **auth modülündedir** (`feature/auth/presentation`). Onboarding sadece `AuthRoute.Login`'e navigate eder.

---

## Pager Yapısı

```
OnboardingWelcomeScreen
├── HorizontalPager (weight = 1f)
│   ├── Page 0: OnboardingWelcomePageContent()
│   ├── Page 1: OnboardingPricingPageContent()
│   ├── Page 2: OnboardingCommunityPageContent()
│   ├── Page 3: OnboardingTrustPageContent()
│   └── Page 4: OnboardingEffortlessPageContent()
└── OnboardingBottomBar (sabit — pager kayarken hareket etmez)
    ├── DomatProgressDots (5 nokta, aktif = yeşil 24×6dp pill)
    └── DomatPrimaryMediumButton (metin sayfaya göre değişir)
```

### Sayfa Sıralaması — OnboardingPage Enum

```kotlin
// feature/onboarding/presentation/src/commonMain/.../model/welcome/OnboardingPage.kt
enum class OnboardingPage(val index: Int) {
    WELCOME(0), PRICING(1), COMMUNITY(2), TRUST(3), EFFORTLESS(4);

    fun next(): OnboardingPage?   // son sayfada null döner → NavigateToLogin
    companion object { fun fromIndex(index: Int): OnboardingPage }
}
```

Sıralamayı değiştirmek için sadece `index` değerleri güncellenir — Screen ve ViewModel otomatik adapte olur.

### Buton Metinleri (strings.xml)

| Sayfa | String Key | Metin |
|---|---|---|
| WELCOME | `onboarding_btn_welcome` | Devam Et |
| PRICING | `onboarding_btn_pricing` | Hmm.. Güzelmiş. Başka? |
| COMMUNITY | `onboarding_btn_community` | Uygunsa kalitesi kötü müdür? |
| TRUST | `onboarding_btn_trust` | Ben pazardan alıyorum 😬 |
| EFFORTLESS | `onboarding_btn_effortless` | Süpermiş! Hadi başlayalım |

---

## MVI — OnboardingWelcome

### UiState
```kotlin
data class OnboardingWelcomeUiState(
    val currentPage: OnboardingPage = OnboardingPage.WELCOME,
    val targetPage: OnboardingPage? = null,   // pager scroll animasyonu için
)
```

### Intent
```kotlin
sealed interface OnboardingWelcomeIntent {
    data object OnContinueClicked   // buton tıklaması
    data object OnScrollConsumed    // scroll animasyonu tamamlandı → targetPage = null
    data class OnPageChanged(val page: Int)  // pager swipe
}
```

### Effect
```kotlin
sealed interface OnboardingWelcomeEffect {
    data object NavigateToLogin   // son sayfadan sonra
}
```

### ViewModel Mantığı
- `OnContinueClicked`: `currentPage.next()` varsa `targetPage` set eder (scroll animasyonu); yoksa `NavigateToLogin` emitler.
- `OnPageChanged`: `OnboardingPage.fromIndex(page)` ile `currentPage` güncellenir.
- `OnScrollConsumed`: `targetPage = null` yapılır.

### Pager Scroll Akışı (Effect yerine UiState)
```kotlin
// Screen'de:
LaunchedEffect(uiState.targetPage) {
    uiState.targetPage?.let { page ->
        pagerState.animateScrollToPage(page.index)
        onIntent(OnboardingWelcomeIntent.OnScrollConsumed)
    }
}
```
Scroll bir UI altyapı işlemi olduğu için Effect yerine `UiState.targetPage` tercih edilir.

---

## Dosya Organizasyonu

```
feature/onboarding/presentation/src/
├── commonMain/
│   ├── model/welcome/
│   │   ├── OnboardingPage.kt          # enum — sayfa sıralaması
│   │   ├── OnboardingWelcomeUiState.kt
│   │   ├── OnboardingWelcomeIntent.kt
│   │   └── OnboardingWelcomeEffect.kt
│   └── viewmodel/
│       └── OnboardingWelcomeViewModel.kt
└── androidMain/screen/
    ├── OnboardingWelcomeScreen.kt     # @NavigationScreen + HorizontalPager + EffectHandler
    ├── OnboardingWelcomePageContent   # private — sadece WelcomeScreen içinden çağrılır
    ├── OnboardingPricingScreen.kt     # internal fun OnboardingPricingPageContent()
    │                                  # enum SupplyChainRowVariant, SupplyChainRowUiModel
    ├── OnboardingCommunityScreen.kt   # internal fun OnboardingCommunityPageContent()
    │                                  # data class CommunityHeroCardUiModel
    ├── OnboardingTrustScreen.kt       # internal fun OnboardingTrustPageContent()
    │                                  # data class TrustFeatureUiModel
    ├── OnboardingEffortlessScreen.kt  # internal fun OnboardingEffortlessPageContent()
    └── OnboardingBottomBar.kt         # internal fun OnboardingBottomBar(uiModel, onContinue)
                                       # data class OnboardingBottomBarUiModel
```

---

## State Yönetimi Kuralları

### UiState > remember { mutableStateOf() }

```kotlin
// ❌ Yanlış
var currentPage by remember { mutableStateOf(0) }

// ✓ Doğru
LaunchedEffect(pagerState.currentPage) {
    onIntent(OnboardingWelcomeIntent.OnPageChanged(pagerState.currentPage))
}
```

`rememberPagerState()` ve `rememberScrollState()` Compose altyapı state'leridir — bu kuraldan muaftır.

### UiModel Paterni

- `@NavigationScreen` composable'lar → `UiState` alır
- Alt composable'lar → `data class XxxUiModel` alır, ham primitifler değil
- `UiModel` Compose tipleri (`Color`, `FontWeight`) taşımaz — variant/enum üzerinden türetilir

---

## Kaynak Sistemi

| Tür | Kullanım | Kaynak Dosya |
|---|---|---|
| **Color** | `colorResource(DomatColors.primary)` | `core/resource/.../moko-resources/base/colors.xml` |
| **Typography** | `MaterialTheme.typography.displayMedium` | `core/design/.../typography/DomatTypographyScale.kt` |
| **String** | `stringResource(MR.strings.onboarding_btn_welcome)` | `core/resource/.../moko-resources/base/strings.xml` |
| **Image (SVG)** | `painterResource(MR.images.ic_google)` | `core/resource/.../moko-resources/images/*.svg` |
| **Image (PNG)** | `painterResource(MR.images.img_welcome_neighborhood)` | `core/resource/.../moko-resources/images/*@1x.png` |

### Kritik Kurallar
- `colorResource(DomatColors.*)` kullan, `MaterialTheme.colorScheme.*` **kullanma** (Material You dynamic color riski)
- `MR.images.*` kullan, `Res.drawable.*` **kullanma**
- PNG dosyalarına `@1x` / `@2x` suffix zorunlu, aksi halde moko ignore eder
- `compose.resources { generateResClass = never }` her modülde tanımlı olmalı

### Canvas Yasağı — Modifier.drawBehind Kullan

Compose'da `Canvas { }` composable **yasaktır**. Şu alternatifleri kullan:

| İhtiyaç | Çözüm |
|---|---|
| Dashed/dotted kenarlık | `Modifier.drawBehind { }` — composable node oluşturmaz, crash riski yok |
| SVG ikonu | Figma'dan SVG export → `moko-resources/images/*.svg` |
| Diğer görseller | Standart Compose layout bileşenleri |

**Örnek — noktalı daire kenarlık** (`OnboardingTrustScreen.kt`):
```kotlin
val dottedBorderColor = colorResource(R.color.malachite_20)
Box(
    modifier = Modifier
        .size(280.dp)
        .clip(CircleShape)
        .background(colorResource(R.color.malachite_10))
        .drawBehind {
            val insetPx = 16.dp.toPx()
            val strokeWidthPx = 2.dp.toPx()
            val radius = (size.minDimension / 2f) - insetPx - (strokeWidthPx / 2f)
            drawCircle(
                color = dottedBorderColor,
                radius = radius,
                style = Stroke(
                    width = strokeWidthPx,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
                ),
            )
        },
    contentAlignment = Alignment.Center,
)
```

> ⚠️ **`painterResource()` ile Android XML `<shape>` drawable kullanma.** `DrawablePainter` implements `RememberObserver` — `HorizontalPager` prefetch mekanizması ile çakışır → `Cannot disable reuse from root` crash'i.
>
> `stroke-dasharray` VectorDrawable'da desteklenmez — SVG doğrudan moko'ya eklense bile dashes kaybolur.

### moko-resources Renk Format Kuralı
`colors.xml` dosyasında `#RRGGBB` / `#RRGGBBAA` formatı kullanılır. Android native `#AARRGGBB` formatı **yasaktır** — moko bunu yanlış okur (örn. `#FF13EC49` → pembe üretir).
