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
                                                                 |  Data Layer            |
                                                                 |  (Repo & DataSources)  |
                                                                 |                        |
                                                                 +------------------------+
```

---

## 💎 Ortak Katmanlar (Domain & Presentation)

Bu katmanlar, backend'den bağımsız olarak projenin `shared` kısmında yer alır.

### 1. Domain Layer (`:feature:auth:domain`)
%100 Pure Kotlin. Hiçbir kütüphane bağımlılığı yoktur.

```kotlin
// Model
data class AuthSession(val uid: String, val email: String)

// Repository Interface
interface AuthRepository {
    suspend fun loginWithGoogle(idToken: String): Result<AuthSession>
}

// UseCase
class LoginWithGoogleUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String) = repository.loginWithGoogle(idToken)
}
```

### 2. Presentation Layer (`:feature:auth:presentation`)
MVI Contract ve ViewModel. her iki platform tarafından ortak kullanılır.

```kotlin
// Contract
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val session: AuthSession? = null
)

sealed interface AuthIntent {
    data object OnGoogleLoginClicked : AuthIntent
    data class OnGoogleTokenReceived(val idToken: String) : AuthIntent
    data class OnGoogleAuthFailed(val errorMessage: String) : AuthIntent
}

sealed interface AuthEffect {
    data object LaunchGoogleSignIn : AuthEffect
    data object NavigateToHome : AuthEffect
}

// ViewModel
class AuthViewModel(private val loginUseCase: LoginWithGoogleUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AuthEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnGoogleLoginClicked -> viewModelScope.launch { _effect.emit(AuthEffect.LaunchGoogleSignIn) }
            is AuthIntent.OnGoogleTokenReceived -> login(intent.idToken)
            is AuthIntent.OnGoogleAuthFailed -> _uiState.update { it.copy(error = intent.errorMessage) }
        }
    }

    private fun login(idToken: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        loginUseCase(idToken).fold(
            onSuccess = { session ->
                _uiState.update { it.copy(isLoading = false, session = session) }
                _effect.emit(AuthEffect.NavigateToHome)
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        )
    }
}
```

---

## 🛠️ Yaklaşım 1: Özel Backend (Ktor API)
Kendi sunucunuza `idToken` gönderip JWT aldığınız senaryo.

```kotlin
// 1. Remote DataSource Interface
interface AuthRemoteDataSource {
    suspend fun authenticate(idToken: String): AuthResponseDto
}

// 2. Ktor Implementation
class KtorAuthRemoteDataSource(private val client: HttpClient) : AuthRemoteDataSource {
    override suspend fun authenticate(idToken: String): AuthResponseDto {
        return client.post("auth/google") {
            setBody(AuthRequestDto(idToken))
        }.body()
    }
}

// 3. Repository Impl
class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource
) : AuthRepository {
    override suspend fun loginWithGoogle(idToken: String): Result<AuthSession> = runCatching {
        val response = remote.authenticate(idToken)
        local.saveToken(response.token)
        AuthSession(response.userId, response.email)
    }
}
```

---

## 🛠️ Yaklaşım 2: Firebase Backend (Hızlı Çözüm / Mevcut Strateji)
Firebase Authentication SDK'sını (GitLive KMP) kullandığınız senaryo.

```kotlin
// 1. Remote DataSource Interface (Aynı kalır, KMP gücü!)
interface AuthRemoteDataSource {
    suspend fun signInWithGoogle(idToken: String): RemoteUserDto
}

// 2. Firebase Implementation (GitLive SDK)
class FirebaseAuthRemoteDataSource(private val firebaseAuth: FirebaseAuth) : AuthRemoteDataSource {
    override suspend fun signInWithGoogle(idToken: String): RemoteUserDto {
        val credential = GoogleAuthProvider.credential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential)
        val user = result.user ?: throw Exception("Login failed")
        return RemoteUserDto(user.uid, user.email ?: "")
    }
}

// 3. Repository Impl
class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource
) : AuthRepository {
    override suspend fun loginWithGoogle(idToken: String): Result<AuthSession> = runCatching {
        val remoteUser = remote.signInWithGoogle(idToken)
        local.saveSession(remoteUser.toLocal()) // DataStore'a kaydet
        AuthSession(remoteUser.uid, remoteUser.email)
    }
}
```

---

## 📱 Native UI Katmanı (Aksiyonun Başladığı Yer)

### Android (`composeApp`)
```kotlin
// UI Helper
class GoogleAuthClient(private val context: Context) {
    suspend fun signIn(): String? {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetGoogleIdOption.Builder().setServerClientId("CLIENT_ID").build())
            .build()
        val result = CredentialManager.create(context).getCredential(context, request)
        return (result.credential as? CustomCredential)?.let { 
            GoogleIdTokenCredential.createFrom(it.data).idToken 
        }
    }
}

// Compose Screen
@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val context = LocalContext.current
    val googleClient = remember { GoogleAuthClient(context) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is AuthEffect.LaunchGoogleSignIn) {
                val token = googleClient.signIn()
                if (token != null) viewModel.onIntent(AuthIntent.OnGoogleTokenReceived(token))
            }
        }
    }
    
    Button(onClick = { viewModel.onIntent(AuthIntent.OnGoogleLoginClicked) }) {
        Text("Google Sign In")
    }
}
```

### iOS (`iosApp`)
```swift
struct LoginView: View {
    @StateObject var viewModel: AuthViewModel // KMP ViewModel Wrapper

    var body: some View {
        Button("Google Sign In") {
            GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, _ in
                if let token = result?.user.idToken?.tokenString {
                    viewModel.onIntent(AuthIntent.OnGoogleTokenReceived(idToken: token))
                }
            }
        }.onReceive(viewModel.effect) { effect in
            if effect is AuthEffectLaunchGoogleSignIn {
                // iOS'ta butona basınca direkt tetiklenebilir veya buradan fırlatılabilir
            }
        }
    }
}
```

---

## 🏆 Neden Bu Mimari?

1.  **Backend Agnostic:** Repository sadece `AuthRemoteDataSource` interface'ini görür. Backend yarın Firebase'den Ktor'a geçtiğinde Repository kodu değişmez.
2.  **MVI & Side Effects:** Google penceresini açmak bir "yan etki"dir. ViewModel sadece "Pencereyi aç" emri fırlatır, sonucu UI'dan geri bekler.
3.  **Test Edilebilirlik:** DataSource'lar mock'lanarak tüm iş mantığı %100 test edilebilir.
4.  **Platform Saflığı:** `shared` modülünde hiçbir Android veya iOS kütüphanesi (Context, Activity vb.) bulunmaz.
