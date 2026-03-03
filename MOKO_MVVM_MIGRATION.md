# Moko MVVM Migration - Complete

DomatApp presentation layer has been successfully migrated to **Moko MVVM** for better Kotlin Multiplatform support and automatic lifecycle management.

## 📦 What Changed

### 1. Dependencies Added

**`gradle/libs.versions.toml`:**
```toml
[versions]
moko-mvvm = "0.16.1"

[libraries]
moko-mvvm-core = { module = "dev.icerock.moko:mvvm-core", version.ref = "moko-mvvm" }
moko-mvvm-flow = { module = "dev.icerock.moko:mvvm-flow", version.ref = "moko-mvvm" }
moko-mvvm-compose = { module = "dev.icerock.moko:mvvm-flow-compose", version.ref = "moko-mvvm" }
```

**`core/common/build.gradle.kts`:**
```kotlin
commonMain {
    dependencies {
        implementation(libs.moko.mvvm.core)    // Core ViewModel class
        implementation(libs.moko.mvvm.flow)    // StateFlow/Flow support
        implementation(libs.kotlinx.coroutines.core)
    }
}
```

**`feature/auth/presentation/build.gradle.kts`:**
```kotlin
androidMain {
    dependencies {
        implementation(libs.moko.mvvm.compose)  // Compose integration
    }
}
```

**`composeApp/build.gradle.kts`:**
```kotlin
dependencies {
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}
```

---

### 2. BaseViewModel Updated

**Before (Pure Kotlin with Manual Scope):**
```kotlin
abstract class BaseViewModel<UiState, Intent, Effect>(
    initialState: UiState
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // ... state, effect management
}
```

**After (Moko MVVM with Automatic Scope):**
```kotlin
import dev.icerock.moko.mvvm.viewmodel.ViewModel

abstract class BaseViewModel<UiState, Intent, Effect>(
    initialState: UiState
) : ViewModel() {
    // viewModelScope now inherited from Moko's ViewModel
    // Automatic lifecycle management
    // ... state, effect management
}
```

**Benefits:**
- ✅ `viewModelScope` automatically provided by Moko MVVM
- ✅ Automatic cancellation on `onCleared()`
- ✅ iOS SwiftUI compatibility
- ✅ Platform-specific lifecycle handling

---

### 3. AuthViewModel Simplified

**Removed:**
```kotlin
// ❌ Manual scope creation - NO LONGER NEEDED
private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

// ❌ Manual cleanup - NO LONGER NEEDED
fun onCleared() {
    viewModelScope.cancel()
}
```

**Kept:**
```kotlin
class AuthViewModel(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : BaseViewModel<AuthUiState, AuthIntent, AuthEffect>(
    initialState = AuthUiState()
) {
    // ✅ Uses inherited viewModelScope
    override fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnGoogleSignInClicked -> handleGoogleSignInClicked()
            // ...
        }
    }

    private fun handleGoogleTokenReceived(idToken: String) {
        viewModelScope.launch {  // ✅ Uses Moko's viewModelScope
            loginWithGoogleUseCase(idToken)
                .onStart { /* ... */ }
                .catch { /* ... */ }
                .collect { /* ... */ }
        }
    }
}
```

---

## 🏗️ Architecture After Migration

```
┌─────────────────────────────────────────────────────┐
│ UI Layer (Compose/SwiftUI)                          │
│ • AuthScreen (Compose for Android)                  │
│ • AuthView (SwiftUI for iOS) - Future               │
│ • Uses koinViewModel() or getViewModel()            │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│ BaseViewModel (Moko MVVM)                           │
│ • extends dev.icerock.moko.mvvm.viewmodel.ViewModel │
│ • Provides viewModelScope automatically             │
│ • Handles lifecycle on both platforms               │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│ AuthViewModel (Presentation Layer)                  │
│ • Uses inherited viewModelScope                     │
│ • MVI pattern (State, Intent, Effect)               │
│ • Exception-based error handling                    │
└───────────────────┬─────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────┐
│ Domain Layer (UseCases)                             │
│ • LoginWithGoogleUseCase                            │
│ • Returns Flow<AuthSession>                         │
└─────────────────────────────────────────────────────┘
```

---

## 🎯 Key Benefits

### 1. **KMP-First Design**
- ✅ ViewModel works on **both Android and iOS**
- ✅ No Android `Context` or iOS framework dependencies in shared code
- ✅ Platform-specific lifecycle handled automatically

### 2. **iOS SwiftUI Support**
```swift
// Future iOS implementation
struct AuthView: View {
    @StateObject private var viewModel = AuthViewModel()

    var body: some View {
        // SwiftUI can observe Moko ViewModel's state
        // Moko provides ObservableObject wrapper
    }
}
```

### 3. **Automatic Lifecycle Management**
- ✅ `viewModelScope` automatically cancelled on clear
- ✅ No manual `onCleared()` needed
- ✅ Memory leak prevention built-in

### 4. **MVI Pattern Still Intact**
- ✅ StateFlow for UI state
- ✅ Channel for one-time effects
- ✅ Intent-based user actions
- ✅ Exception-based error handling

---

## 🔄 Migration Checklist

- [x] Add Moko MVVM dependencies to version catalog
- [x] Update `core/common` build.gradle.kts
- [x] Update `feature/auth/presentation` build.gradle.kts
- [x] Update `composeApp` build.gradle.kts with Koin Compose
- [x] Modify `BaseViewModel` to extend Moko's `ViewModel`
- [x] Remove manual `viewModelScope` from `AuthViewModel`
- [x] Remove `onCleared()` method from `AuthViewModel`
- [x] Update imports (remove manual CoroutineScope imports)
- [x] Fix `DomainError` instantiation issue in AuthViewModel
- [x] Move Koin initialization to platform-specific code (MainActivity)
- [x] **Build project successfully ✅**
- [ ] Test Google Sign-In flow
- [ ] Create iOS SwiftUI view (future)

---

## 🧪 Testing Instructions

### 1. Build Project
```bash
./gradlew :composeApp:assembleDebug
```

### 2. Verify ViewModel Injection
- Koin should inject `AuthViewModel` with `@Factory`
- ViewModel should work with `koinViewModel()` in Compose
- No manual scope management errors

### 3. Test MVI Flow
```
1. User clicks "Google ile Giriş Yap"
   ↓
2. Loading state appears
   ↓
3. Google Sign-In launches (mocked for now)
   ↓
4. Success: Session card shows
   ↓
5. Error: Red error card shows
```

### 4. Verify Lifecycle
- Rotate device → State preserved
- Leave and return to screen → ViewModel recreated properly
- Background app → Coroutines cancelled on onCleared

---

## 📊 Before vs After Comparison

| Aspect | Before (Pure Kotlin) | After (Moko MVVM) |
|--------|---------------------|-------------------|
| **Scope Management** | Manual `CoroutineScope(SupervisorJob() + Dispatchers.Main)` | Automatic `viewModelScope` |
| **Cleanup** | Manual `onCleared()` + `viewModelScope.cancel()` | Automatic via Moko |
| **iOS Support** | ❌ Not lifecycle-aware | ✅ Full SwiftUI support |
| **Platform Specifics** | Expect/Actual needed | ✅ Handled by Moko |
| **Dependencies** | None (pure Kotlin) | Moko MVVM (small lib) |
| **Code Complexity** | More boilerplate | Less boilerplate |

---

## 🚀 Next Steps

### Immediate:
1. **Build and test** the current Android implementation
2. Verify no regressions in Google Sign-In flow
3. Check memory leaks with Android Profiler

### Future (iOS):
1. Create `iosApp/AuthView.swift`
2. Use Moko's `createSwiftFlow()` to observe state
3. Handle effects in SwiftUI
4. Integrate iOS Google Sign-In SDK

---

## 💡 Important Notes

### Koin Integration
- Moko ViewModel works seamlessly with Koin
- Use `@Factory` for ViewModels (as before)
- `koinViewModel()` works with Moko ViewModels on Android
- For iOS: Use Koin's iOS framework with Moko wrapper

### ViewModelScope
- **Don't create manual scopes anymore**
- Use `viewModelScope.launch { }` directly
- Moko handles cancellation automatically
- Scope context: `Dispatchers.Main` by default

### State Management
- StateFlow works exactly the same
- Channel works exactly the same
- MVI pattern unchanged

---

## 📚 Documentation

- **Moko MVVM GitHub**: https://github.com/icerockdev/moko-mvvm
- **Moko MVVM Flow**: https://github.com/icerockdev/moko-mvvm#flow-integration
- **iOS Integration**: https://github.com/icerockdev/moko-mvvm#ios-integration

---

## 🔧 Koin Initialization Strategy

### Problem
KSP-generated Koin modules from one KMP library are not visible to consuming modules at compile time. The `:shared` module couldn't import `org.koin.ksp.generated.*` from its dependencies.

### Solution
**Move Koin initialization to platform-specific entry points** where all dependencies are on the classpath.

#### Android (MainActivity.kt)
```kotlin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Koin with all KSP-generated modules
        startKoin {
            androidContext(this@MainActivity)
            modules(defaultModule)  // KSP generates this from all @Module/@Single/@Factory
        }

        setContent { App() }
    }
}
```

#### iOS (Future - App.swift)
```swift
import Shared

@main
struct MyApp: App {
    init() {
        // Initialize Koin for iOS
        KoinKt.doInitKoin()
    }
}
```

### Benefits
- ✅ All KSP-generated code is visible at the app entry point
- ✅ Platform-specific context injection (e.g., `androidContext()`)
- ✅ Simpler shared module (no DI initialization code)
- ✅ Each platform controls its own DI setup

---

**Status:** ✅ Migration Complete - Build Successful ✅
**Last Updated:** 2026-03-01
**Migrated By:** Claude Code (Sonnet 4.5)
