# Auth Presentation Layer - MVI Implementation

Bu doküman, Auth feature için MVI pattern ile tasarlanan presentation layer'ın detaylarını içerir.

## 📦 Oluşturulan Dosyalar

### 1. Core Common - BaseViewModel

**Dosya:** `core/common/src/commonMain/kotlin/.../BaseViewModel.kt`

```kotlin
abstract class BaseViewModel<UiState, Intent, Effect>(initialState: UiState) {
    val state: StateFlow<UiState>         // UI State
    val effect: Flow<Effect>              // One-time effects
    abstract fun onIntent(intent: Intent) // Intent handler
}
```

**Özellikler:**
- Generic MVI base class
- StateFlow for state management
- Channel for one-time effects (navigation, toasts)
- Protected helpers: `updateState()`, `emitEffect()`

---

### 2. Auth Presentation - Models

#### AuthUiState
```kotlin
data class AuthUiState(
    val isLoading: Boolean = false,
    val session: AuthSession? = null,
    val error: String? = null,
    val isGoogleSignInInProgress: Boolean = false
)
```

#### AuthIntent (User Actions)
```kotlin
sealed class AuthIntent {
    data object OnGoogleSignInClicked
    data class OnGoogleTokenReceived(val idToken: String)
    data object OnGoogleSignInCancelled
    data object OnErrorDismissed
}
```

#### AuthEffect (Side Effects)
```kotlin
sealed class AuthEffect {
    data object LaunchGoogleSignIn      // Launch native Google Sign-In
    data object NavigateToHome          // Navigate after success
    data class ShowError(val message: String)
}
```

---

### 3. AuthViewModel

**Dosya:** `feature/auth/presentation/src/commonMain/.../AuthViewModel.kt`

**Özellikler:**
- ✅ Exception-based error handling with `.catch`
- ✅ Loading state with `.onStart`
- ✅ DomainError to UI message mapping
- ✅ CoroutineScope management
- ✅ Effect emission for side effects

**Flow:**
```
User clicks button
  ↓
onIntent(OnGoogleSignInClicked)
  ↓
emitEffect(LaunchGoogleSignIn)
  ↓
Native UI returns idToken
  ↓
onIntent(OnGoogleTokenReceived(idToken))
  ↓
loginWithGoogleUseCase(idToken)
  .onStart { isLoading = true }
  .catch { error → show error }
  .collect { session → success }
```

---

### 4. Koin Dependency Injection

#### Modules Created:
- **AuthDomainModule** - UseCases (@Factory)
- **AuthDataModule** - Repository, DataSources (@Single)
- **AuthPresentationModule** - ViewModels (@Factory)

#### KoinInitializer (shared module)
```kotlin
object KoinInitializer {
    fun init(): KoinApplication {
        return startKoin {
            modules(
                CoreRemoteModule().module,
                AuthDomainModule().module,
                AuthDataModule().module,
                AuthPresentationModule().module,
                defaultModule // KSP generated
            )
        }
    }
}
```

**Usage in Android:**
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KoinInitializer.init() // Call once!
        setContent { App() }
    }
}
```

---

### 5. Android Compose UI

**Dosya:** `composeApp/src/androidMain/.../AuthScreen.kt`

**Features:**
- ✅ Google Sign-In button
- ✅ Loading state UI
- ✅ Error card with dismiss
- ✅ Success card (session info)
- ✅ Debug state info
- ✅ Effect handling with LaunchedEffect

**Integration:**
```kotlin
AuthScreen(
    viewModel = koinViewModel(), // Koin injection
    onGoogleSignInRequested = {
        // Launch Google Sign-In SDK
        val idToken = googleSignInClient.signIn()
        idToken // Return to ViewModel
    },
    onNavigateToHome = {
        navController.navigate("home")
    }
)
```

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│ UI Layer (Compose)                                  │
│ • AuthScreen composable                             │
│ • Collects state, observes effects                  │
│ • Sends intents to ViewModel                        │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│ Presentation Layer (AuthViewModel)                  │
│ • Extends BaseViewModel<UiState, Intent, Effect>    │
│ • Handles intents via onIntent()                    │
│ • Calls UseCases                                    │
│ • Manages state with StateFlow                      │
│ • Emits effects via Channel                         │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│ Domain Layer (LoginWithGoogleUseCase)               │
│ • Calls repository                                  │
│ • Returns Flow<AuthSession>                         │
│ • Throws DomainError on failure                     │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│ Data Layer (AuthRepository)                         │
│ • Calls DataSource                                  │
│ • retry logic, error mapping                        │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│ DataSource (Generated by KSP)                       │
│ • Uses RemoteApi                                    │
└─────────────────────────────────────────────────────┘
```

---

## 🔄 Error Flow

```
1. Ktor throws IOException
   ↓
2. DataSource propagates exception
   ↓
3. Repository catches with .retryWhen (retry 3 times)
   ↓
4. Repository maps to DomainError with .catch
   ↓
5. UseCase propagates DomainError
   ↓
6. ViewModel catches with .catch
   ↓
7. ViewModel converts to UI message:
   AuthError.InvalidCredentials → "Giriş bilgileri geçersiz"
   RemoteError.NoConnection → "İnternet bağlantısı yok"
   ↓
8. UI shows error card
```

---

## ✅ Testing Checklist

### Manual Testing Steps:

1. **Build Project:**
   ```bash
   ./gradlew :composeApp:assembleDebug
   ```

2. **Run on Device/Emulator:**
   - App launches
   - Shows "Google ile Giriş Yap" button

3. **Click Button:**
   - Loading state appears
   - Google Sign-In dialog launches (or dummy token for now)

4. **Success Flow:**
   - Session card appears with user info
   - "Navigate to home" logged

5. **Error Flow:**
   - If network error: Red error card with message
   - Dismiss button works

6. **Koin DI:**
   - ViewModel injected successfully
   - UseCase injected into ViewModel
   - Repository injected into UseCase
   - DataSource injected into Repository

---

## 🔧 Next Steps

### For Production:

1. **Integrate Real Google Sign-In:**
   ```kotlin
   // Android
   val googleSignInClient = GoogleSignIn.getClient(context, gso)
   val intent = googleSignInClient.signInIntent
   launcher.launch(intent)
   ```

2. **Add Navigation:**
   ```kotlin
   val navController = rememberNavController()
   AuthScreen(
       onNavigateToHome = { navController.navigate("home") }
   )
   ```

3. **Error Handling Improvements:**
   - Retry button for network errors
   - Snackbar instead of card for errors
   - Proper error icons

4. **Loading Improvements:**
   - Skeleton loading
   - Animated progress
   - Better UX

5. **iOS SwiftUI:**
   - Create SwiftUI AuthView
   - Use shared AuthViewModel (via KMM ViewModel wrapper)
   - Integrate Google Sign-In iOS SDK

---

## 📚 Key Files Reference

| Component | Location |
|-----------|----------|
| BaseViewModel | `core/common/.../BaseViewModel.kt` |
| AuthViewModel | `feature/auth/presentation/.../AuthViewModel.kt` |
| AuthUiState | `feature/auth/presentation/.../AuthUiState.kt` |
| AuthIntent | `feature/auth/presentation/.../AuthIntent.kt` |
| AuthEffect | `feature/auth/presentation/.../AuthEffect.kt` |
| AuthScreen | `composeApp/.../AuthScreen.kt` |
| KoinInitializer | `shared/.../KoinInitializer.kt` |
| MainActivity | `composeApp/.../MainActivity.kt` |

---

## 🎯 Benefits of This Architecture

1. **Testability:**
   - ViewModel is pure Kotlin, no Android dependencies
   - Can be tested with Kotlin Test
   - Mock UseCase for ViewModel tests

2. **Maintainability:**
   - Clear separation of concerns
   - MVI pattern makes state management predictable
   - Effects separate from state

3. **Scalability:**
   - BaseViewModel reusable for all features
   - Koin modules modular and composable
   - Easy to add new intents/effects

4. **KMP-Friendly:**
   - ViewModel in commonMain
   - UI platform-specific (Compose/SwiftUI)
   - Business logic shared

---

**Status:** ✅ Ready for Testing  
**Last Updated:** 2026-03-01
