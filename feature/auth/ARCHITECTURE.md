# Google Sign-In Architecture (KMP + MVI + Clean Architecture)

Bu belge, DomatApp projesinde `:feature:auth` modülü içerisindeki Google Sign-In akışının KMP (Kotlin Multiplatform) ve MVI (Model-View-Intent) prensiplerine uygun olarak nasıl kurgulandığını açıklar.

## 🎯 Temel Prensip

KMP projelerinde UI işlemleri (modal açmak, Activity Context kullanmak) `shared` modülden izole edilmelidir. Google Sign-In ekranını açmak işletim sistemine (Android/iOS) özel bir yan etki (Side Effect) olduğundan, bu işlem Native UI katmanında yapılır. `shared` katman (Domain/Data/Presentation) sadece token alıp işleyen "saf" (pure) bir yapı olarak kalır.

---

## 🏗️ Mimari Akış (ASCII Diagram)

```text
+-------------------+      (1) Intent.OnGoogleLoginClicked       +------------------------+
|                   | -----------------------------------------> |                        |
|  Native UI        |                                            |  Shared KMP ViewModel  |
|  (Compose /       | <----------------------------------------- |  (Presentation Layer)  |
|   SwiftUI)        |      (2) Effect.LaunchGoogleSignIn         |                        |
|                   |                                            |                        |
|   +-----------+   |      (4) Intent.OnGoogleTokenReceived      |                        |
|   | OS Modal  |   | -----------------------------------------> |                        |
|   | (Google)  |   |                                            |                        |
|   +-----------+   |                                            +-----------+------------+
|        |          |                                                        |
|   (3) idToken     |                                                        | (5) UseCase(idToken)
+-------------------+                                                        v
                                                                 +------------------------+
                                                                 |                        |
                                                                 |  Pure Kotlin Domain    |
                                                                 |  (UseCase & Repo API)  |
                                                                 |                        |
                                                                 +-----------+------------+
                                                                             |
                                                                             | (6) loginWithGoogle(idToken)
                                                                             v
                                                                 +------------------------+
                                                                 |                        |
                                                                 |  Data Layer (Ktor)     |
                                                                 |  API / Local DB        |
                                                                 |                        |
                                                                 +------------------------+
```

---

## 📝 Kod Akışı (Referans Örnek)

### 1. Presentation Katmanı (MVI Contract) - `shared`

State (Data Class), Intent ve Effect olarak üç ana yapıya bölünür.

```kotlin
// feature/auth/presentation/src/commonMain/kotlin/.../AuthContract.kt

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface AuthIntent {
    data object OnGoogleLoginClicked : AuthIntent
    data class OnGoogleTokenReceived(val idToken: String) : AuthIntent
    data class OnGoogleAuthFailed(val errorMessage: String) : AuthIntent
}

sealed interface AuthEffect {
    data object LaunchGoogleSignIn : AuthEffect
    data object NavigateToHome : AuthEffect
    data class ShowSnackbar(val message: String) : AuthEffect
}
```

### 2. ViewModel - `shared`

ViewModel işletim sistemini bilmez. Sadece Intent alır, Effect fırlatır ve Domain'i tetikler.

```kotlin
// feature/auth/presentation/src/commonMain/kotlin/.../AuthViewModel.kt

class AuthViewModel(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase 
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AuthEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnGoogleLoginClicked -> {
                viewModelScope.launch { _effect.emit(AuthEffect.LaunchGoogleSignIn) }
            }
            is AuthIntent.OnGoogleTokenReceived -> {
                authenticateWithBackend(intent.idToken)
            }
            is AuthIntent.OnGoogleAuthFailed -> {
                _uiState.value = _uiState.value.copy(error = intent.errorMessage)
            }
        }
    }

    private fun authenticateWithBackend(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Domain katmanına saf String gönderiyoruz, Context yok!
            val result = loginWithGoogleUseCase(idToken) 
            
            result.fold(
                onSuccess = { 
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _effect.emit(AuthEffect.NavigateToHome)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                    _effect.emit(AuthEffect.ShowSnackbar("Giriş başarısız"))
                }
            )
        }
    }
}
```

### 3. Native UI Helper (Android) - `composeApp`

Android Context'i ve CredentialManager'ı sadece Compose tarafında (veya Android'e özel helper sınıflarında) tutulur.

```kotlin
// composeApp/src/main/kotlin/com/domatapp/android/auth/GoogleAuthClient.kt

class GoogleAuthClient(private val context: Context) {
    suspend fun signIn(): String? {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("SENIN_WEB_CLIENT_ID.apps.googleusercontent.com")
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = CredentialManager.create(context).getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
```

### 4. Compose Ekranı - `composeApp`

Compose ekranı mümkün olan en "aptal" (dumb) haliyle bırakılır. İş mantığı bilmez, sadece Effect dinler ve Intent basar.

```kotlin
// composeApp/src/main/kotlin/com/domatapp/android/auth/LoginScreen.kt

@Composable
fun LoginScreen(viewModel: AuthViewModel = koinViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val googleAuthClient = remember { GoogleAuthClient(context) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.LaunchGoogleSignIn -> {
                    // Effect geldiğinde helper üzerinden native UI'ı tetikle
                    val idToken = googleAuthClient.signIn()
                    if (idToken != null) {
                        viewModel.onIntent(AuthIntent.OnGoogleTokenReceived(idToken))
                    } else {
                        viewModel.onIntent(AuthIntent.OnGoogleAuthFailed("Giriş iptal edildi"))
                    }
                }
                is AuthEffect.NavigateToHome -> { /* Navigasyon */ }
                is AuthEffect.ShowSnackbar -> { /* Snackbar göster */ }
            }
        }
    }

    Column {
        if (uiState.isLoading) CircularProgressIndicator()
        
        Button(onClick = { viewModel.onIntent(AuthIntent.OnGoogleLoginClicked) }) {
            Text("Google İle Giriş Yap")
        }
    }
}
```

## Neden Bu Yaklaşımı Seçtik?

1. **Temiz Compose:** İş mantığı ve CredentialManager karmaşası UI bileşeninden uzaklaştırıldı. Compose sadece State çizer ve Intent fırlatır.
2. **KMP Prensipleri Korundu:** KMP modülü olan `:feature:auth` içerisinde `Context`, `Activity` vb. Android'e özel hiçbir bağımlılık (Dependency) tutulmaz.
3. **MVI Uyumu:** Tek yönlü veri akışı (Unidirectional Data Flow) sayesinde uygulamanın state'i öngörülebilir hale geldi.
4. **iOS Hazır:** SwiftUI tarafı hiçbir ViewModel (KMP) değişikliğine ihtiyaç duymadan aynı `AuthEffect`leri dinleyip iOS'un native Google SignIn SDK'sını tetikleyebilir.
