# Auth — Feature Dökümanı

## Genel Bakış

Kimlik doğrulama ve adres seçimi akışını barındırır. İki farklı giriş senaryosu sunar:

| Senaryo | Rota | Açıklama |
|---|---|---|
| **Tam Auth** | `AuthRoute.AuthScreen` | Google Sign-In + gerçek token doğrulama + Home'a git |
| **Onboarding Bridge** | `AuthRoute.Login` → `AuthRoute.LocationSelection` | Onboarding'den gelen kullanıcı, Google butonuna basar, konum seçer, Home'a geçer |

### Ekran Akışı

```
[Onboarding tamamlanınca]
AuthRoute.Login
    │  Google butonuna basınca
    ▼
AuthRoute.LocationSelection
    │  Blok + Daire seçilip Devam Et'e basınca
    ▼
Main.Home

[Doğrudan auth akışı]
AuthRoute.AuthScreen
    │  Google token alınınca → LoginWithGoogleUseCase
    ▼
Main.Home
```

---

## Ekranlar

### 1. LoginScreen (`AuthRoute.Login`)

Onboarding'den gelen basit giriş ekranı. Gerçek auth işlemi **yapmaz** — sadece `AuthRoute.LocationSelection`'a yönlendirir.

**Layout:**
- Hero görsel (442dp, rounded bottom corners, gradient overlay)
- "Taze & Yerel" badge + "DomatApp" başlık
- Alt bilgi metni
- Google Sign-In butonu
- ToS / Gizlilik Politikası linki (annotated string)

**MVI:**
```kotlin
// UiState
data class LoginUiState(val isLoading: Boolean = false)

// Intent
sealed interface LoginIntent {
    data object OnGoogleSignInClicked
}

// Effect
sealed interface LoginEffect {
    data object NavigateToLocationSelection
}
```

**ViewModel:** `OnGoogleSignInClicked` → `NavigateToLocationSelection` effect'i emit eder.

---

### 2. LocationSelectionScreen (`AuthRoute.LocationSelection`)

Kullanıcının site/blok/daire bilgisini seçtiği ekran.

**Layout:**
- `DomatScreenHeader` (geri butonu + `DomatProgressSteps(4 adım, 1. adım aktif)`)
- 3 kilitli `DomatLocationCard` (İlçe, Mahalle, Site — önceden set)
- 2 `DomatInputDropdown` yan yana (Blok No, Daire No)
- `DomatBottomActionBar` → `DomatPrimaryButton("Devam Et")` — her ikisi seçilince aktif

**MVI:**
```kotlin
// UiState
data class LocationSelectionUiState(
    val selectedBlock: String? = null,
    val selectedApartment: String? = null,
    val isConfirmEnabled: Boolean = false,
    val isBlokDropdownOpen: Boolean = false,
    val isDaireDropdownOpen: Boolean = false,
)

// Intent
sealed interface LocationSelectionIntent {
    data class SelectBlock(val block: String)
    data class SelectApartment(val apartment: String)
    data object ToggleBlokDropdown
    data object ToggleDaireDropdown
    data object Confirm
    data object GoBack
}

// Effect
sealed interface LocationSelectionEffect {
    data object NavigateToHome
    data object NavigateBack
}
```

**ViewModel Mantığı:**
- Blok seçilince: `isBlokDropdownOpen = false`, `isConfirmEnabled = selectedApartment != null`
- Daire seçilince: `isDaireDropdownOpen = false`, `isConfirmEnabled = selectedBlock != null`
- Dropdown'lar mutually exclusive: biri açılınca diğeri kapanır
- `Confirm` → `NavigateToHome` (`navigator.replaceAll`)
- `GoBack` → `NavigateBack` (`navigator.popBack`)

---

### 3. AuthPage (`AuthRoute.AuthScreen`)

Tam kimlik doğrulama ekranı. Gerçek Google token ile `LoginWithGoogleUseCase`'i çağırır.

**MVI:**
```kotlin
// UiState
data class AuthUiState(
    val isLoading: Boolean = false,
    val session: AuthSession? = null,
    val error: String? = null,
    val isGoogleSignInInProgress: Boolean = false,
)

// Intent
sealed class AuthIntent {
    data object OnGoogleSignInClicked
    data class OnGoogleTokenReceived(val idToken: String)
    data object OnGoogleSignInCancelled
    data object OnErrorDismissed
}

// Effect
sealed class AuthEffect {
    data object LaunchGoogleSignIn   // native UI Google dialog'u açar
    data object NavigateToHome
    data class ShowError(val message: String)
    data object Idle
}
```

**Google Sign-In Akışı:**
```
1. Kullanıcı butona basar
   → ViewModel: isGoogleSignInInProgress = true, emitEffect(LaunchGoogleSignIn)

2. Native UI (AuthEffectHandler) Google dialog'u açar
   → Token alınırsa: onIntent(OnGoogleTokenReceived(idToken))
   → İptal edilirse: onIntent(OnGoogleSignInCancelled)

3. ViewModel: loginWithGoogleUseCase(idToken) çağrısı
   → onStart: isLoading = true
   → catch: DomainError → toUiMessage() → ShowError effect
   → collect: session = ..., emitEffect(NavigateToHome)
```

**Hata Eşleştirme (`toUiMessage`):**

| DomainError | String Key |
|---|---|
| `AuthError.InvalidCredentials` | `error_invalid_credentials` |
| `AuthError.UserNotFound` | `error_user_not_found` |
| `AuthError.EmailAlreadyInUse` | `error_email_already_in_use` |
| `AuthError.AccountDisabled` | `error_account_disabled` |
| `RemoteError.NoConnection` | `error_no_connection` |
| `RemoteError.Timeout` | `error_timeout` |
| `RemoteError.ServerError(code)` | `error_server` |
| `RemoteError.ClientError(code)` | `error_client` |

---

## Navigasyon Rotaları

```kotlin
// core/navigation/src/commonMain/.../Route.kt
sealed interface AuthRoute : Route {
    data object AuthScreen        // tam auth akışı
    data object Login             // onboarding bridge
    data object LocationSelection // konum seçimi
    data class AddressValidationScreen(val address: String)
}
```

---

## KSP Üretilen Entry'ler

KSP, `@NavigationScreen` + `@NavigationViewModel` annotasyonlarından otomatik entry fonksiyonları üretir:

```kotlin
// feature/auth/presentation/build/generated/.../AuthPresentationEntries.kt
fun EntryProviderScope<Route>.authPageEntry()              // AuthRoute.AuthScreen
fun EntryProviderScope<Route>.loginScreenEntry()           // AuthRoute.Login
fun EntryProviderScope<Route>.locationSelectionScreenEntry() // AuthRoute.LocationSelection
```

Bu entry'ler `composeApp/.../navigation/AuthEntries.kt`'de bir araya getirilir:

```kotlin
fun EntryProviderScope<Route>.authPresentationEntries() {
    authPageEntry()
    loginScreenEntry()
    locationSelectionScreenEntry()
}
```

---

## Dosya Organizasyonu

```
feature/auth/presentation/src/
├── commonMain/
│   ├── model/
│   │   ├── AuthUiState.kt / AuthIntent.kt / AuthEffect.kt   # AuthScreen için
│   │   ├── login/
│   │   │   ├── LoginUiState.kt
│   │   │   ├── LoginIntent.kt
│   │   │   └── LoginEffect.kt
│   │   └── location/
│   │       ├── LocationSelectionUiState.kt
│   │       ├── LocationSelectionIntent.kt
│   │       └── LocationSelectionEffect.kt
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt           # LoginWithGoogleUseCase bağımlılığı var
│   │   ├── LoginViewModel.kt          # bağımlılık yok
│   │   └── LocationSelectionViewModel.kt
│   └── di/AuthPresentationModule.kt   # @ComponentScan ile tüm ViewModel'ları otomatik bulur
└── androidMain/screen/
    ├── AuthPage.kt                    # @NavigationScreen(AuthRoute.AuthScreen)
    ├── AuthEffectHandler.kt           # @NavigationEffectHandler — Google dialog, navigasyon
    ├── AuthTopBar.kt                  # üst bar (AuthScreen'e özel)
    ├── LoginScreen.kt                 # @NavigationScreen(AuthRoute.Login)
    └── LocationSelectionScreen.kt     # @NavigationScreen(AuthRoute.LocationSelection)
```

---

## Bağımlılık Kuralları

- `AuthViewModel` → `feature:auth:domain` (LoginWithGoogleUseCase, AuthSession)
- `LoginViewModel` ve `LocationSelectionViewModel` → domain bağımlılığı **yok**
- Presentation layer, `feature:auth:data`'ya **hiçbir zaman** bağımlı olmaz
