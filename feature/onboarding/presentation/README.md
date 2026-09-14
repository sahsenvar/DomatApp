# Module: :feature:onboarding:presentation

## 🎯 Purpose (Amaç)
Onboarding ekranlarının UI mantığını barındırır.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Presentation
- **Patterns:** MVI

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:onboarding:presentation]
                   |
                   +--> [:feature:onboarding:domain]
                   +--> [:core:common]
                   v
             [:core:navigation]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Adım bazlı geçişleri yönetir.

---

## Compose Kuralları

### Renk
```kotlin
// ✓ Doğru
colorResource(DomatColors.primary)
colorResource(DomatColors.textSecondary)

// ✗ Yanlış — MaterialTheme.colorScheme dynamic color riski taşır
MaterialTheme.colorScheme.primary
```

### Typography
```kotlin
// ✓ Doğru — MaterialTheme.typography üzerinden
Text(style = MaterialTheme.typography.displayMedium)
Text(style = MaterialTheme.typography.bodyLarge)

// Font ailesi DomatTheme içinde MR.fonts.nunito_sans_regular olarak otomatik set edilir.
```

### String
```kotlin
// ✓ Doğru
stringResource(MR.strings.onboarding_btn_welcome)

// ✗ Yanlış — hardcoded string yasaktır
Text(text = "Devam Et")
```

### Image / Icon
```kotlin
// ✓ Doğru
import dev.icerock.moko.resources.compose.painterResource
painterResource(MR.images.ic_google)

// ✗ Yanlış — Compose Resources kullanılmaz
import org.jetbrains.compose.resources.painterResource
painterResource(Res.drawable.ic_google)
```

### UiModel tipi içinde image referansı
```kotlin
// ✓ Doğru
import dev.icerock.moko.resources.ImageResource
val icon: ImageResource

// ✗ Yanlış
import org.jetbrains.compose.resources.DrawableResource
val icon: DrawableResource
```

### State yönetimi
```kotlin
// ✓ Doğru — tüm dinamik durum UiState'ten gelir
LaunchedEffect(pagerState.currentPage) {
    onIntent(OnboardingWelcomeIntent.OnPageChanged(pagerState.currentPage))
}

// ✗ Yanlış — composable içinde remember { mutableStateOf() } kullanılmaz
var currentPage by remember { mutableStateOf(0) }
```

`rememberPagerState()` ve `rememberScrollState()` Compose altyapı state'leridir, bu kuraldan muaftır.

### Pager scroll (ViewModel → Composable)
```kotlin
// UiState'e targetPage: OnboardingPage? eklenir
// ViewModel: updateState { it.copy(targetPage = next) }

// Screen'de:
LaunchedEffect(uiState.targetPage) {
    uiState.targetPage?.let { page ->
        pagerState.animateScrollToPage(page.index)
        onIntent(OnboardingWelcomeIntent.OnScrollConsumed)
    }
}
// Effect yerine UiState.targetPage tercih edilir — scroll UI altyapısıdır.
```

### Sayfa sıralaması
```kotlin
// OnboardingPage enum'u sıralamayı yönetir
when (OnboardingPage.fromIndex(page)) {
    OnboardingPage.WELCOME    -> OnboardingWelcomePageContent()
    OnboardingPage.PRICING    -> OnboardingPricingPageContent()
    OnboardingPage.COMMUNITY  -> OnboardingCommunityPageContent()
    OnboardingPage.TRUST      -> OnboardingTrustPageContent()
    OnboardingPage.EFFORTLESS -> OnboardingEffortlessPageContent()
}
// Magic number (0, 1, 2...) kullanılmaz.
```

### compose.resources kapatma
Her `composeMultiplatform` plugin'i kullanan modülde `Res.kt` üretimi kapatılır:
```kotlin
compose.resources {
    generateResClass = never
}
```
