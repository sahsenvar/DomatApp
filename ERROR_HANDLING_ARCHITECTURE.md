# Error Handling Architecture - DomatApp

Bu doküman, DomatApp projesinde DataSource'tan ViewModel'a kadar uzanan **end-to-end error handling** ve **code generation** stratejisini detaylı bir şekilde açıklar.

**Temel Yaklaşım:** Exception-based error handling with Flow

---

## 📋 İçindekiler

1. [Temel Kararlar ve Mental Model](#temel-kararlar-ve-mental-model)
2. [Flow-Based Architecture](#flow-based-architecture)
3. [Error Type Design (Exception-Based)](#error-type-design-exception-based)
4. [DataSource Abstraction Strategy](#datasource-abstraction-strategy)
5. [RemoteApi Design (Delegation Pattern)](#remoteapi-design-delegation-pattern)
6. [Annotation-Based Code Generation](#annotation-based-code-generation)
7. [KSP Processor Implementation](#ksp-processor-implementation)
8. [OpenAPI Generator Integration](#openapi-generator-integration)
9. [Serialization Abstraction](#serialization-abstraction)
10. [Error Mapping Strategy](#error-mapping-strategy)
11. [Dependency Injection (Koin)](#dependency-injection-koin)
12. [Complete Flow Example](#complete-flow-example)
13. [Implementation Roadmap](#implementation-roadmap)
14. [Future: Streaming Use Cases (Either Pattern)](#future-streaming-use-cases-either-pattern)

---

## 1. Temel Kararlar ve Mental Model

### 1.1 Repository'ler Flow Döndürür

**Karar:** Repository fonksiyonları `Flow<T>` döndürür (suspend fun değil).

**Neden?**
- **Retry/Resilience:** Flow operatörleri (retry, retryWhen) ile kolayca retry logic eklenebilir
- **Cache Mechanism:** shareIn, stateIn ile cache stratejileri uygulanabilir
- **Multiple Emissions:** Offline-first pattern (cache → fresh) kolayca implement edilir
- **Cancellation:** Flow'un built-in cancellation support'u
- **Reactive:** UI state'e reactive binding

**Command vs Query Ayrımı:**
```kotlin
// COMMAND (One-shot): Login, Logout, Update
fun loginWithGoogle(idToken: String): Flow<AuthSession>

// QUERY (Observable): GetProfile, ObserveSession
fun observeUserProfile(userId: String): Flow<UserProfile>
```

### 1.2 Error Handling: Exception-Based

**Karar:** Exception-based error handling kullanılır. DomainError'lar `Exception`'dan türer.

**Neden?**
- ✅ Flow'un native retry/catch operatörleri doğal çalışır
- ✅ KMP'de Exception kullanımı sorunsuz (kotlin-stdlib-common)
- ✅ Single-emit use case'lerde (login, logout, fetch) terminal error yeterli
- ✅ Daha az boilerplate, daha Kotlin idiomatik
- ✅ Type-safe pattern matching (sealed class)
- ✅ Offline-first pattern'de cached data emit edildikten sonra error fırlatılabilir

**DomainError Design:**
```kotlin
// core:domain
sealed class DomainError(message: String? = null) : Exception(message)

// Generic network errors
sealed class NetworkError(message: String? = null) : DomainError(message) {
    data object NoConnection : NetworkError("No internet connection")
    data object Timeout : NetworkError("Request timeout")
    data class ServerError(val code: Int, override val message: String?) : NetworkError(message)
}

// Feature-specific errors (feature:auth:domain)
sealed class AuthError(message: String? = null) : DomainError(message) {
    data object InvalidCredentials : AuthError("Invalid credentials")
    data object UserNotFound : AuthError("User not found")
    data object EmailAlreadyInUse : AuthError("Email already in use")
    data class Unknown(override val message: String?) : AuthError(message)
}

// Fallback
data class UnknownError(override val message: String?) : DomainError(message)
```

**KMP Compatibility:**
Exception Kotlin stdlib'in bir parçası ve KMP common code'da sorunsuz kullanılır. Her platform kendi implementation'ını sağlar (JVM: java.lang.Exception, iOS: NSException mapping, JS: Error).

**Future Note:** WebSocket, SSE gibi long-running stream'lerde recoverable error gerekirse `Either<DomainError, T>` pattern'i kullanılabilir. Şu anda Auth feature için exception-based yeterli.

### 1.3 Katman Sorumlulukları

```
┌─────────────────────────────────────────────────────────────┐
│ PRESENTATION LAYER (ViewModel)                              │
│ • Flow<T> tüketir, catch ile error handle eder             │
│ • onStart ile loading state yönetir                        │
│ • Domain error'ları UI-friendly message'lara çevirir        │
│ • UseCase'leri çağırır                                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ DOMAIN LAYER (UseCase)                                      │
│ • Flow<T> pass-through veya business logic ekler           │
│ • Repository'yi çağırır                                     │
│ • DomainError'ları propagate eder (exception throw)        │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ DATA LAYER (Repository)                                     │
│ • Flow<T> döndürür (exception fırlatabilir)                 │
│ • Retry logic uygular (retryWhen)                           │
│ • Exception'ları domain error'lara map eder (catch)         │
│ • DataSource'ları orchestrate eder                          │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ DATA LAYER (DataSource - Generated by KSP)                 │
│ • RemoteApi arayüzünü kullanır                              │
│ • Kütüphane-specific exception'lar fırlatabilir             │
│ • Annotation'larla tanımlanır, KSP implementation üretir    │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│ INFRASTRUCTURE LAYER (core:remote / core:local)            │
│ • RemoteApi: Protocol-specific implementations (delegation) │
│ • RestApi, SocketApi, GraphQLApi ayrı ayrı                 │
│ • Kütüphane-specific implementation'lar (Ktor, Room, etc.) │
│ • Serialization işlemlerini halleder                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Flow-Based Architecture

### 2.1 Repository Flow Pattern (Exception-Based)

```kotlin
// feature:auth:data
class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    override fun loginWithGoogle(idToken: String): Flow<AuthSession> = flow {
        // 1. Remote call
        val dto = remoteDataSource.signInWithGoogle(idToken)

        // 2. Local'e kaydet
        localDataSource.saveSession(dto.toLocal())

        // 3. Domain model'e çevir ve emit et
        emit(dto.toDomain())
    }
    .retryWhen { cause, attempt ->
        // Sadece network hatalarında retry
        if (cause is NetworkError && attempt < 3) {
            delay(1000 * (attempt + 1)) // Exponential backoff
            true
        } else false
    }
    .catch { exception ->
        // Exception'ı domain error'a map et ve fırlat
        throw exception.toDomainError()
    }

    // Query pattern: Observable with cache
    override fun observeAuthSession(): Flow<AuthSession?> =
        localDataSource.observeSession()
            .map { it?.toDomain() }
            .shareIn(
                scope = repositoryScope,
                started = SharingStarted.WhileSubscribed(5000),
                replay = 1
            )

    // Offline-first pattern: Cache then fresh
    override fun getUserProfile(userId: String): Flow<UserProfile> = flow {
        // Emit 1: Cached data (fast)
        val cached = localDataSource.getProfile(userId)
        if (cached != null) {
            emit(cached.toDomain())
        }

        // Emit 2: Fresh data (or exception)
        val fresh = remoteDataSource.getProfile(userId) // May throw
        localDataSource.saveProfile(fresh)
        emit(fresh.toDomain())
    }
    .catch { exception ->
        // Eğer cached data emit edildiyse, kullanıcı onu görmüş olur
        // Exception fırlatılınca flow terminate olur ama cached data zaten işlendi
        throw exception.toDomainError()
    }
}
```

### 2.2 UseCase Pass-Through Pattern

```kotlin
// feature:auth:domain
class LoginWithGoogleUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(idToken: String): Flow<AuthSession> =
        repository.loginWithGoogle(idToken)
    // UseCase sadece pass-through, business logic yoksa direkt return
}

// Business logic varsa:
class GetUserProfileUseCase(
    private val repository: AuthRepository,
    private val analyticsRepository: AnalyticsRepository
) {
    operator fun invoke(userId: String): Flow<UserProfile> =
        repository.getUserProfile(userId)
            .onEach { profile ->
                // Business logic: Analytics tracking
                analyticsRepository.trackProfileView(profile.id)
            }
    // Exception'lar repository'den propagate olur
}
```

### 2.3 ViewModel Consumption Pattern (Exception-Based)

```kotlin
// feature:auth:presentation
class AuthViewModel(
    private val loginUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnGoogleTokenReceived -> {
                viewModelScope.launch {
                    loginUseCase(intent.idToken)
                        .onStart {
                            _state.update { it.copy(isLoading = true) }
                        }
                        .catch { exception ->
                            val error = exception as DomainError
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = error.toUiMessage()
                                )
                            }
                        }
                        .collect { session ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    session = session,
                                    error = null
                                )
                            }
                        }
                }
            }
        }
    }
}

// Extension: DomainError to UI Message
fun DomainError.toUiMessage(): String = when (this) {
    is AuthError.InvalidCredentials -> "Giriş bilgileri geçersiz"
    is AuthError.UserNotFound -> "Kullanıcı bulunamadı"
    is AuthError.EmailAlreadyInUse -> "Bu e-posta adresi zaten kullanımda"
    is NetworkError.NoConnection -> "İnternet bağlantısı yok"
    is NetworkError.Timeout -> "İstek zaman aşımına uğradı"
    is NetworkError.ServerError -> "Sunucu hatası ($code)"
    else -> message ?: "Bilinmeyen bir hata oluştu"
}
```

---

## 3. Error Type Design (Exception-Based)

### 3.1 Domain Error Hierarchy

```kotlin
// core:domain - Base error
sealed class DomainError(message: String? = null) : Exception(message)

// core:domain - Network errors (generic, shared across features)
sealed class NetworkError(message: String? = null) : DomainError(message) {
    data object NoConnection : NetworkError("No internet connection")
    data object Timeout : NetworkError("Request timeout")
    data class ServerError(val code: Int, override val message: String?) : NetworkError(message)
    data class HttpError(val statusCode: Int, override val message: String?) : NetworkError(message)
}

// feature:auth:domain - Auth-specific errors
sealed class AuthError(message: String? = null) : DomainError(message) {
    data object InvalidCredentials : AuthError("Invalid credentials")
    data object UserNotFound : AuthError("User not found")
    data object EmailAlreadyInUse : AuthError("Email already in use")
    data object AccountDisabled : AuthError("Account has been disabled")
    data class Unknown(override val message: String?) : AuthError(message)
}

// core:domain - Generic fallback
data class UnknownError(override val message: String?) : DomainError(message)
```

### 3.2 Exception to DomainError Mapping

```kotlin
// feature:auth:data - Extension function
private fun Exception.toDomainError(): DomainError {
    return when (this) {
        // Already a domain error (from repository layer)
        is DomainError -> this

        // Firebase-specific
        is FirebaseAuthException -> when (code) {
            "auth/invalid-credential" -> AuthError.InvalidCredentials
            "auth/user-not-found" -> AuthError.UserNotFound
            "auth/email-already-in-use" -> AuthError.EmailAlreadyInUse
            "auth/user-disabled" -> AuthError.AccountDisabled
            "auth/network-request-failed" -> NetworkError.NoConnection
            else -> AuthError.Unknown(message)
        }

        // Ktor-specific (HTTP client)
        is ClientRequestException -> when (response.status.value) {
            401 -> AuthError.InvalidCredentials
            404 -> AuthError.UserNotFound
            409 -> AuthError.EmailAlreadyInUse
            in 500..599 -> NetworkError.ServerError(response.status.value, message)
            else -> NetworkError.HttpError(response.status.value, message)
        }

        // Generic network errors
        is UnknownHostException -> NetworkError.NoConnection
        is SocketTimeoutException -> NetworkError.Timeout
        is IOException -> NetworkError.NoConnection

        // Fallback
        else -> UnknownError(message ?: "Unknown error occurred")
    }
}
```

### 3.3 Usage in Repository

```kotlin
override fun loginWithGoogle(idToken: String): Flow<AuthSession> = flow {
    // ... flow body
}
.retryWhen { cause, attempt ->
    // Retry only on network errors
    when {
        cause is NetworkError && attempt < 3 -> {
            delay(1000 * (attempt + 1))
            true
        }
        else -> false
    }
}
.catch { exception ->
    // Map to domain error and re-throw
    throw exception.toDomainError()
}
```

---

## 4. DataSource Abstraction Strategy

### 4.1 Problem Statement

**Amaç:** DataSource'lar Ktor, Retrofit, Firebase gibi kütüphaneleri **bilmemeli**. Bunun yerine:
- `core:remote` modülünde `RemoteApi` (generic abstraction)
- `core:local` modülünde `LocalApi` (generic abstraction)
- DataSource'lar sadece bu arayüzleri kullanmalı

**Avantajları:**
1. Kütüphane değişimi kolay (Ktor → Retrofit)
2. DataSource'lar pure ve testable
3. Centralized network/db logic
4. Backend-agnostic (REST, GraphQL, WebSocket, Firebase hepsi aynı interface)

---

## 5. RemoteApi Design (Delegation Pattern)

### 5.1 Interface Separation (Protocol-Based)

**Karar:** RemoteApi'yi protokollere göre ayır, Kotlin delegation pattern kullan.

**Neden?**
- ✅ **Separation of Concerns**: REST, WebSocket, GraphQL logic'i ayrı
- ✅ **Single Responsibility**: Her implementation kendi işine odaklanır
- ✅ **Testability**: Her protokol ayrı test edilebilir
- ✅ **Modularity**: Yeni protokol eklemek kolay (gRPC, MQTT, vb.)
- ✅ **Flexibility**: Ktor REST + Apollo GraphQL gibi mix-and-match mümkün

### 5.2 Interface Definitions

```kotlin
// core:remote

// ================== REST API ==================
interface RestApi {
    suspend fun <T : Any> get(
        path: String,
        queryParams: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        responseType: KClass<T>
    ): T

    suspend fun <T : Any> post(
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        responseType: KClass<T>
    ): T

    suspend fun <T : Any> put(
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        responseType: KClass<T>
    ): T

    suspend fun <T : Any> patch(
        path: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap(),
        responseType: KClass<T>
    ): T

    suspend fun <T : Any> delete(
        path: String,
        queryParams: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        responseType: KClass<T>
    ): T
}

// ================== WebSocket API ==================
interface SocketApi {
    fun <T : Any> subscribe(
        path: String,
        messageType: KClass<T>
    ): Flow<T>

    suspend fun <T : Any> send(
        path: String,
        message: Any,
        responseType: KClass<T>
    ): T

    fun disconnect(path: String)
}

// ================== GraphQL API ==================
interface GraphQLApi {
    suspend fun <T : Any> query(
        query: String,
        variables: Map<String, Any> = emptyMap(),
        operationName: String? = null,
        responseType: KClass<T>
    ): T

    suspend fun <T : Any> mutate(
        mutation: String,
        variables: Map<String, Any> = emptyMap(),
        operationName: String? = null,
        responseType: KClass<T>
    ): T

    fun <T : Any> subscribe(
        subscription: String,
        variables: Map<String, Any> = emptyMap(),
        messageType: KClass<T>
    ): Flow<T>
}

// ================== Unified RemoteApi (Delegation) ==================
interface RemoteApi : RestApi, SocketApi, GraphQLApi

class RemoteApiImpl(
    restApi: RestApi,
    socketApi: SocketApi,
    graphQLApi: GraphQLApi
) : RemoteApi,
    RestApi by restApi,        // Delegation!
    SocketApi by socketApi,    // Delegation!
    GraphQLApi by graphQLApi   // Delegation!
```

**Delegation Pattern Açıklaması:**
- `RestApi by restApi`: RestApi interface'inin tüm metodları `restApi` nesnesine yönlendirilir
- `SocketApi by socketApi`: SocketApi metodları `socketApi` nesnesine yönlendirilir
- Bu sayede `RemoteApiImpl` herhangi bir metod implementation'ı yazmadan, hem RestApi hem SocketApi hem GraphQLApi'yi implement eder

### 5.3 Protocol Implementations

```kotlin
// ================== Ktor REST Implementation ==================
class KtorRestApi(
    private val client: HttpClient,
    private val serializer: SerializationAdapter
) : RestApi {

    override suspend fun <T : Any> get(
        path: String,
        queryParams: Map<String, Any>,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): T {
        val response = client.get(path) {
            queryParams.forEach { (key, value) -> parameter(key, value) }
            headers.forEach { (key, value) -> header(key, value) }
        }
        return serializer.deserialize(response.bodyAsText(), responseType)
    }

    override suspend fun <T : Any> post(
        path: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): T {
        val response = client.post(path) {
            headers.forEach { (key, value) -> header(key, value) }
            body?.let {
                contentType(ContentType.Application.Json)
                setBody(serializer.serialize(it))
            }
        }
        return serializer.deserialize(response.bodyAsText(), responseType)
    }

    override suspend fun <T : Any> put(
        path: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): T {
        val response = client.put(path) {
            headers.forEach { (key, value) -> header(key, value) }
            body?.let {
                contentType(ContentType.Application.Json)
                setBody(serializer.serialize(it))
            }
        }
        return serializer.deserialize(response.bodyAsText(), responseType)
    }

    override suspend fun <T : Any> patch(
        path: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): T {
        val response = client.patch(path) {
            headers.forEach { (key, value) -> header(key, value) }
            body?.let {
                contentType(ContentType.Application.Json)
                setBody(serializer.serialize(it))
            }
        }
        return serializer.deserialize(response.bodyAsText(), responseType)
    }

    override suspend fun <T : Any> delete(
        path: String,
        queryParams: Map<String, Any>,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): T {
        val response = client.delete(path) {
            queryParams.forEach { (key, value) -> parameter(key, value) }
            headers.forEach { (key, value) -> header(key, value) }
        }
        return serializer.deserialize(response.bodyAsText(), responseType)
    }
}

// ================== Ktor WebSocket Implementation ==================
class KtorSocketApi(
    private val client: HttpClient,
    private val serializer: SerializationAdapter
) : SocketApi {

    private val connections = mutableMapOf<String, DefaultClientWebSocketSession>()

    override fun <T : Any> subscribe(
        path: String,
        messageType: KClass<T>
    ): Flow<T> = flow {
        client.webSocket(path) {
            connections[path] = this

            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val message = serializer.deserialize(frame.readText(), messageType)
                    emit(message)
                }
            }
        }
    }.onCompletion {
        connections.remove(path)
    }

    override suspend fun <T : Any> send(
        path: String,
        message: Any,
        responseType: KClass<T>
    ): T {
        val connection = connections[path]
            ?: throw IllegalStateException("No active WebSocket connection for $path")

        connection.send(Frame.Text(serializer.serialize(message)))

        val response = connection.incoming.receive() as Frame.Text
        return serializer.deserialize(response.readText(), responseType)
    }

    override fun disconnect(path: String) {
        connections.remove(path)?.cancel()
    }
}

// ================== Apollo GraphQL Implementation (Optional) ==================
class ApolloGraphQLApi(
    private val apolloClient: ApolloClient
) : GraphQLApi {

    override suspend fun <T : Any> query(
        query: String,
        variables: Map<String, Any>,
        operationName: String?,
        responseType: KClass<T>
    ): T {
        // Apollo implementation
        TODO("Implement using Apollo Client")
    }

    override suspend fun <T : Any> mutate(
        mutation: String,
        variables: Map<String, Any>,
        operationName: String?,
        responseType: KClass<T>
    ): T {
        TODO("Implement using Apollo Client")
    }

    override fun <T : Any> subscribe(
        subscription: String,
        variables: Map<String, Any>,
        messageType: KClass<T>
    ): Flow<T> {
        TODO("Implement using Apollo Client")
    }
}

// ================== No-Op Implementation (When protocol not used) ==================
object NoOpGraphQLApi : GraphQLApi {
    override suspend fun <T : Any> query(
        query: String,
        variables: Map<String, Any>,
        operationName: String?,
        responseType: KClass<T>
    ): T = throw UnsupportedOperationException("GraphQL not configured")

    override suspend fun <T : Any> mutate(
        mutation: String,
        variables: Map<String, Any>,
        operationName: String?,
        responseType: KClass<T>
    ): T = throw UnsupportedOperationException("GraphQL not configured")

    override fun <T : Any> subscribe(
        subscription: String,
        variables: Map<String, Any>,
        messageType: KClass<T>
    ): Flow<T> = throw UnsupportedOperationException("GraphQL not configured")
}
```

### 5.4 LocalApi Design

```kotlin
// core:local
interface LocalApi {

    // ================== CRUD Operations ==================
    suspend fun <T : Any> insert(
        table: String,
        entity: T
    )

    suspend fun <T : Any> insertAll(
        table: String,
        entities: List<T>
    )

    suspend fun <T : Any> update(
        table: String,
        entity: T
    )

    suspend fun <T : Any> delete(
        table: String,
        entity: T
    )

    suspend fun <T : Any> query(
        table: String,
        predicate: Map<String, Any> = emptyMap(),
        entityType: KClass<T>
    ): List<T>

    fun <T : Any> observe(
        table: String,
        predicate: Map<String, Any> = emptyMap(),
        entityType: KClass<T>
    ): Flow<List<T>>

    // ================== Key-Value Storage ==================
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)
    fun observeString(key: String): Flow<String?>

    // ================== Transaction Support ==================
    suspend fun <R> transaction(block: suspend () -> R): R
}
```

---

## 6. Annotation-Based Code Generation

### 6.1 Annotation Design

```kotlin
// core:remote - annotations package

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RemoteDataSource(
    val baseUrl: String = ""
)

// HTTP Methods
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class GET(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class POST(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class PUT(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class PATCH(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class DELETE(val path: String)

// WebSocket
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Subscribe(val path: String)

// GraphQL
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Query(val query: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Mutation(val mutation: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class GraphQLSubscription(val subscription: String)

// Parameter annotations
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Body

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Path(val name: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class QueryParam(val name: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Header(val name: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Field(val name: String)
```

### 6.2 Usage Example

```kotlin
// feature:auth:data - DataSource interface
@RemoteDataSource
interface AuthRemoteDataSource {

    @POST("auth/google")
    suspend fun signInWithGoogle(
        @Body idToken: String
    ): RemoteUserDto

    @GET("auth/profile/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): UserProfileDto

    @PUT("auth/profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body profile: UpdateProfileRequest
    ): UserProfileDto

    @DELETE("auth/session")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Unit

    // WebSocket example
    @Subscribe("notifications/{userId}")
    fun subscribeToNotifications(
        @Path("userId") userId: String
    ): Flow<NotificationDto>

    // GraphQL example (optional)
    @Query("""
        query GetUser(${'$'}id: ID!) {
            user(id: ${'$'}id) {
                id
                email
                profile { name avatar }
            }
        }
    """)
    suspend fun getUserGraphQL(
        @Field("id") userId: String
    ): UserDto
}
```

### 6.3 Generated Implementation (by KSP)

```kotlin
// Generated: feature/auth/data/build/generated/ksp/commonMain/kotlin/AuthRemoteDataSourceImpl.kt
class AuthRemoteDataSourceImpl(
    private val remoteApi: RemoteApi
) : AuthRemoteDataSource {

    override suspend fun signInWithGoogle(idToken: String): RemoteUserDto {
        return remoteApi.post(
            path = "auth/google",
            body = mapOf("idToken" to idToken),
            headers = emptyMap(),
            responseType = RemoteUserDto::class
        )
    }

    override suspend fun getProfile(userId: String, token: String): UserProfileDto {
        return remoteApi.get(
            path = "auth/profile/$userId",
            queryParams = emptyMap(),
            headers = mapOf("Authorization" to token),
            responseType = UserProfileDto::class
        )
    }

    override suspend fun updateProfile(
        userId: String,
        profile: UpdateProfileRequest
    ): UserProfileDto {
        return remoteApi.put(
            path = "auth/profile/$userId",
            body = profile,
            headers = emptyMap(),
            responseType = UserProfileDto::class
        )
    }

    override suspend fun logout(token: String) {
        remoteApi.delete(
            path = "auth/session",
            queryParams = emptyMap(),
            headers = mapOf("Authorization" to token),
            responseType = Unit::class
        )
    }

    override fun subscribeToNotifications(userId: String): Flow<NotificationDto> {
        return remoteApi.subscribe(
            path = "notifications/$userId",
            messageType = NotificationDto::class
        )
    }

    override suspend fun getUserGraphQL(userId: String): UserDto {
        return remoteApi.query(
            query = """
                query GetUser(${'$'}id: ID!) {
                    user(id: ${'$'}id) {
                        id
                        email
                        profile { name avatar }
                    }
                }
            """,
            variables = mapOf("id" to userId),
            responseType = UserDto::class
        )
    }
}
```

---

## 7. KSP Processor Implementation

### 7.1 Processor Setup

```kotlin
// build-logic/ksp-processor/build.gradle.kts
plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.21")
    implementation("com.squareup:kotlinpoet:1.16.0")
    implementation("com.squareup:kotlinpoet-ksp:1.16.0")
}
```

### 7.2 Processor Implementation (Simplified)

```kotlin
// build-logic/ksp-processor/src/main/kotlin/RemoteDataSourceProcessor.kt
class RemoteDataSourceProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(
            RemoteDataSource::class.qualifiedName!!
        )

        symbols.filterIsInstance<KSClassDeclaration>().forEach { classDeclaration ->
            if (classDeclaration.classKind != ClassKind.INTERFACE) {
                logger.error("@RemoteDataSource can only be applied to interfaces", classDeclaration)
                return@forEach
            }

            generateImplementation(classDeclaration)
        }

        return emptyList()
    }

    private fun generateImplementation(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val interfaceName = classDeclaration.simpleName.asString()
        val implClassName = "${interfaceName}Impl"

        val fileSpec = FileSpec.builder(packageName, implClassName)
            .addImport("kotlinx.coroutines.flow", "Flow")
            .addType(
                TypeSpec.classBuilder(implClassName)
                    .addSuperinterface(ClassName(packageName, interfaceName))
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("remoteApi", ClassName("com.domatapp.core.remote", "RemoteApi"))
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("remoteApi", ClassName("com.domatapp.core.remote", "RemoteApi"))
                            .initializer("remoteApi")
                            .addModifiers(KModifier.PRIVATE)
                            .build()
                    )
                    .apply {
                        classDeclaration.getAllFunctions().forEach { function ->
                            addFunction(generateFunction(function))
                        }
                    }
                    .build()
            )
            .build()

        val file = codeGenerator.createNewFile(
            Dependencies(true, classDeclaration.containingFile!!),
            packageName,
            implClassName
        )

        file.bufferedWriter().use { fileSpec.writeTo(it) }
    }

    private fun generateFunction(function: KSFunctionDeclaration): FunSpec {
        // Implementation omitted for brevity
        // See full implementation in Section 7.3
        TODO("Parse annotations and generate function body")
    }
}

class RemoteDataSourceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RemoteDataSourceProcessor(
            environment.codeGenerator,
            environment.logger
        )
    }
}
```

### 7.3 Processor Registration

```
// build-logic/ksp-processor/src/main/resources/META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider
com.domatapp.buildlogic.ksp.RemoteDataSourceProcessorProvider
```

**Not:** Full KSP implementation detayları Section 7.2'deki TODO kısmında implement edilecek. Annotation parsing, path substitution, query/header parameter handling detayları burada.

---

## 8. OpenAPI Generator Integration

### 8.1 Workflow

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Backend Developer                                            │
│    • Swagger/OpenAPI spec hazırlar                              │
│    • https://api.domatapp.com/swagger.json                      │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│ 2. Download OpenAPI Spec                                        │
│    ./gradlew downloadApiSpec                                    │
│    → Saves to: api-specs/auth-api.json                          │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│ 3. OpenAPI Generator (Custom Mustache Templates)               │
│    openapi-generator generate \                                 │
│      -i api-specs/auth-api.json \                               │
│      -g kotlin \                                                │
│      -t templates/datasource-mustache/ \                        │
│      -o feature/auth/data/src/commonMain/kotlin/generated       │
│                                                                 │
│    Generates:                                                   │
│    • DTOs (RemoteUserDto, UserProfileDto, etc.)                 │
│    • DataSource interface (AuthRemoteDataSource)                │
│      with @GET, @POST, @Path, @Body annotations                 │
└───────────────────────────┬─────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│ 4. KSP Processor                                                │
│    • Reads @RemoteDataSource annotation                         │
│    • Generates AuthRemoteDataSourceImpl                         │
│    • Uses RemoteApi for actual network calls                    │
└───────────────────────────┬─────────────────────────────────────┐
                            │
┌───────────────────────────▼─────────────────────────────────────┐
│ 5. Koin DI                                                      │
│    • Injects generated implementation                           │
│    • Repository uses AuthRemoteDataSource                       │
└─────────────────────────────────────────────────────────────────┘
```

### 8.2 Gradle Tasks

```kotlin
// build.gradle.kts (root)
tasks.register("downloadApiSpec") {
    group = "openapi"
    description = "Download latest OpenAPI spec from backend"

    doLast {
        val specUrl = "https://api.domatapp.com/swagger.json"
        val outputFile = file("api-specs/auth-api.json")

        outputFile.parentFile.mkdirs()

        exec {
            commandLine("curl", "-o", outputFile.absolutePath, specUrl)
        }

        println("Downloaded API spec to: ${outputFile.absolutePath}")
    }
}

tasks.register("generateApiClient") {
    group = "openapi"
    description = "Generate DataSource interfaces and DTOs from OpenAPI spec"

    dependsOn("downloadApiSpec")

    doLast {
        exec {
            commandLine(
                "openapi-generator", "generate",
                "-i", "api-specs/auth-api.json",
                "-g", "kotlin",
                "-t", "templates/datasource-mustache/",
                "-o", "feature/auth/data/src/commonMain/kotlin/generated",
                "--additional-properties=packageName=com.domatapp.feature.auth.data.generated"
            )
        }
    }
}
```

### 8.3 Benefits

1. **Zero Manual Work:** Backend spec değişince: `./gradlew generateApiClient`
2. **Type Safety:** DTO'lar ve endpoint'ler compile-time'da kontrol ediliyor
3. **Consistency:** Tüm DataSource'lar aynı pattern'i kullanıyor
4. **Documentation:** OpenAPI comments Kotlin KDoc olarak generate ediliyor

---

## 9. Serialization Abstraction

### 9.1 SerializationAdapter Interface

```kotlin
// core:remote
interface SerializationAdapter {
    fun <T : Any> serialize(value: T): String
    fun <T : Any> deserialize(json: String, type: KClass<T>): T
    fun <T : Any> serializeToByteArray(value: T): ByteArray
    fun <T : Any> deserializeFromByteArray(bytes: ByteArray, type: KClass<T>): T
}
```

### 9.2 Kotlinx.Serialization Implementation

```kotlin
// core:remote
class KotlinxSerializationAdapter(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
) : SerializationAdapter {

    override fun <T : Any> serialize(value: T): String {
        @Suppress("UNCHECKED_CAST")
        val serializer = serializersModule.serializer(value::class) as KSerializer<T>
        return json.encodeToString(serializer, value)
    }

    override fun <T : Any> deserialize(json: String, type: KClass<T>): T {
        val serializer = serializersModule.serializer(type)
        @Suppress("UNCHECKED_CAST")
        return this.json.decodeFromString(serializer, json) as T
    }

    override fun <T : Any> serializeToByteArray(value: T): ByteArray {
        return serialize(value).encodeToByteArray()
    }

    override fun <T : Any> deserializeFromByteArray(bytes: ByteArray, type: KClass<T>): T {
        return deserialize(bytes.decodeToString(), type)
    }
}
```

---

## 10. Error Mapping Strategy

### 10.1 Layer-by-Layer Mapping (Exception-Based)

```kotlin
// Layer 1: Infrastructure (core:remote)
// Kütüphane-specific exception'lar fırlatılır
class KtorRestApi(...) : RestApi {
    override suspend fun <T : Any> post(...): T {
        try {
            return client.post(...).body()
        } catch (e: Exception) {
            // Ktor exception'larını fırlat (map etme!)
            throw e
        }
    }
}

// Layer 2: DataSource (feature:auth:data - Generated)
// Exception'ları fırlatır, map etmez
class AuthRemoteDataSourceImpl(...) : AuthRemoteDataSource {
    override suspend fun signInWithGoogle(idToken: String): RemoteUserDto {
        return remoteApi.post(...) // Exception fırlatabilir
    }
}

// Layer 3: Repository (feature:auth:data)
// Exception'ları domain error'lara map eder
class AuthRepositoryImpl(...) : AuthRepository {
    override fun loginWithGoogle(idToken: String): Flow<AuthSession> = flow {
        val dto = remoteDataSource.signInWithGoogle(idToken) // Exception fırlatabilir
        localDataSource.saveSession(dto.toLocal())
        emit(dto.toDomain())
    }
    .retryWhen { cause, attempt ->
        // Network hatalarında retry (domain error'a bakmadan önce)
        if (cause is IOException && attempt < 3) {
            delay(1000 * (attempt + 1))
            true
        } else false
    }
    .catch { exception ->
        // Exception'ı domain error'a çevir ve fırlat
        throw exception.toDomainError()
    }
}

// Layer 4: UseCase (feature:auth:domain)
// Exception'ları propagate eder
class LoginWithGoogleUseCase(...) {
    operator fun invoke(idToken: String): Flow<AuthSession> =
        repository.loginWithGoogle(idToken)
    // Exception'lar otomatik propagate olur
}

// Layer 5: ViewModel (feature:auth:presentation)
// Exception'ları catch ile handle eder
class AuthViewModel(...) {
    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnGoogleTokenReceived -> {
                viewModelScope.launch {
                    loginUseCase(intent.idToken)
                        .onStart { _state.update { it.copy(isLoading = true) } }
                        .catch { exception ->
                            val error = exception as DomainError
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = error.toUiMessage()
                                )
                            }
                        }
                        .collect { session ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    session = session
                                )
                            }
                        }
                }
            }
        }
    }
}
```

---

## 11. Dependency Injection (Koin)

### 11.1 Module Structure (Delegation Pattern)

```kotlin
// core:remote - Infrastructure
val coreRemoteModule = module {

    // Serialization
    single<SerializationAdapter> { KotlinxSerializationAdapter() }

    // HTTP Client
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) { json() }
            install(Logging) { level = LogLevel.BODY }
            install(WebSockets)
            defaultRequest {
                url("https://api.domatapp.com/")
            }
        }
    }

    // Individual protocol implementations
    single<RestApi> { KtorRestApi(get(), get()) }
    single<SocketApi> { KtorSocketApi(get(), get()) }
    single<GraphQLApi> { NoOpGraphQLApi } // Or: ApolloGraphQLApi(get())

    // Unified RemoteApi (delegation!)
    single<RemoteApi> {
        RemoteApiImpl(
            restApi = get(),
            socketApi = get(),
            graphQLApi = get()
        )
    }
}

// core:local - Infrastructure
val coreLocalModule = module {
    single<LocalApi> { RoomLocalApi(get()) }
    single { /* Room database config */ }
}

// feature:auth:data - Data layer
val authDataModule = module {
    // Generated DataSource implementation
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(get()) }
    single<AuthLocalDataSource> { AuthLocalDataSourceImpl(get()) }

    // Repository
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}

// feature:auth:domain - Domain layer
val authDomainModule = module {
    factory { LoginWithGoogleUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetProfileUseCase(get()) }
}

// feature:auth:presentation - Presentation layer
val authPresentationModule = module {
    viewModel { AuthViewModel(get(), get(), get()) }
}
```

### 11.2 App Initialization

```kotlin
// composeApp (Android) or shared (iOS)
fun initKoin() {
    startKoin {
        modules(
            coreRemoteModule,
            coreLocalModule,
            authDataModule,
            authDomainModule,
            authPresentationModule
        )
    }
}
```

---

## 12. Complete Flow Example

### 12.1 From UI Event to Success/Error (Exception-Based)

```kotlin
// 1. USER ACTION (Android Compose)
@Composable
fun LoginScreen(viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    Button(onClick = {
        viewModel.onIntent(AuthIntent.OnGoogleLoginClicked)
    }) {
        Text("Sign in with Google")
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.LaunchGoogleSignIn -> {
                    val token = googleAuthClient.signIn()
                    if (token != null) {
                        viewModel.onIntent(AuthIntent.OnGoogleTokenReceived(token))
                    }
                }
            }
        }
    }
}

// 2. VIEWMODEL (Presentation Layer)
class AuthViewModel(
    private val loginUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnGoogleTokenReceived -> {
                viewModelScope.launch {
                    loginUseCase(intent.idToken)
                        .onStart {
                            _state.update { it.copy(isLoading = true) }
                        }
                        .catch { exception ->
                            val error = exception as DomainError
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = error.toUiMessage()
                                )
                            }
                        }
                        .collect { session ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    session = session
                                )
                            }
                            _effect.emit(AuthEffect.NavigateToHome)
                        }
                }
            }
        }
    }
}

// 3. USECASE (Domain Layer)
class LoginWithGoogleUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(idToken: String): Flow<AuthSession> =
        repository.loginWithGoogle(idToken)
}

// 4. REPOSITORY (Data Layer)
class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    override fun loginWithGoogle(idToken: String): Flow<AuthSession> = flow {
        // Call DataSource
        val dto = remoteDataSource.signInWithGoogle(idToken)

        // Save to local
        localDataSource.saveSession(dto.toLocal())

        // Map to domain and emit
        emit(dto.toDomain())
    }
    .retryWhen { cause, attempt ->
        if (cause is IOException && attempt < 3) {
            delay(1000 * (attempt + 1))
            true
        } else false
    }
    .catch { exception ->
        throw exception.toDomainError()
    }
}

// 5. DATASOURCE (Data Layer - Generated by KSP)
class AuthRemoteDataSourceImpl(
    private val remoteApi: RemoteApi
) : AuthRemoteDataSource {
    override suspend fun signInWithGoogle(idToken: String): RemoteUserDto {
        return remoteApi.post(
            path = "auth/google",
            body = mapOf("idToken" to idToken),
            responseType = RemoteUserDto::class
        )
    }
}

// 6. REMOTE API (Infrastructure - Ktor via Delegation)
class KtorRestApi(
    private val client: HttpClient,
    private val serializer: SerializationAdapter
) : RestApi {
    override suspend fun <T : Any> post(...): T {
        val response = client.post(path) {
            headers.forEach { (k, v) -> header(k, v) }
            body?.let {
                contentType(ContentType.Application.Json)
                setBody(serializer.serialize(it))
            }
        }
        return serializer.deserialize(response.bodyAsText(), responseType)
    }
}
```

### 12.2 Error Flow Example (Exception-Based)

```kotlin
// Scenario: Network error occurs

// 1. KTOR throws exception
class KtorRestApi(...) {
    override suspend fun <T : Any> post(...): T {
        try {
            return client.post(...).body()
        } catch (e: UnresolvedAddressException) {
            throw e // IOException
        }
    }
}

// 2. DATASOURCE propagates exception
class AuthRemoteDataSourceImpl(...) {
    override suspend fun signInWithGoogle(...): RemoteUserDto {
        return remoteApi.post(...) // IOException fırlatılır
    }
}

// 3. REPOSITORY catches and retries, then maps to domain error
class AuthRepositoryImpl(...) {
    override fun loginWithGoogle(...): Flow<AuthSession> = flow {
        val dto = remoteDataSource.signInWithGoogle(...) // IOException
        emit(dto.toDomain())
    }
    .retryWhen { cause, attempt ->
        if (cause is IOException && attempt < 3) {
            delay(1000 * (attempt + 1))
            true // Retry!
        } else false
    }
    .catch { exception ->
        // After 3 retries, map to domain error
        throw exception.toDomainError() // IOException → NetworkError.NoConnection
    }
}

// 4. USECASE propagates exception
class LoginWithGoogleUseCase(...) {
    operator fun invoke(...): Flow<AuthSession> =
        repository.loginWithGoogle(...) // NetworkError.NoConnection propagates
}

// 5. VIEWMODEL handles error
class AuthViewModel(...) {
    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnGoogleTokenReceived -> {
                viewModelScope.launch {
                    loginUseCase(intent.idToken)
                        .catch { exception -> // NetworkError.NoConnection
                            val error = exception as DomainError
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = "İnternet bağlantısı yok"
                                )
                            }
                        }
                        .collect { ... }
                }
            }
        }
    }
}
```

---

## 13. Implementation Roadmap

### Phase 1: Foundation (1-2 weeks)
- [ ] Create `core:remote` module
- [ ] Define `RestApi`, `SocketApi`, `GraphQLApi` interfaces
- [ ] Implement `RemoteApi` with delegation pattern
- [ ] Implement `KtorRestApi`, `KtorSocketApi`, `NoOpGraphQLApi`
- [ ] Create `SerializationAdapter` interface and Kotlinx.Serialization implementation
- [ ] Create domain error hierarchy in `core:domain`
  - [ ] `DomainError` sealed class extends Exception
  - [ ] `NetworkError` sealed class
  - [ ] Generic `UnknownError`
- [ ] Create `core:local` module with `LocalApi` interface

### Phase 2: Annotation & KSP (2-3 weeks)
- [ ] Design annotation system
  - [ ] `@RemoteDataSource`, `@GET`, `@POST`, `@PUT`, `@PATCH`, `@DELETE`
  - [ ] `@Subscribe` for WebSocket
  - [ ] `@Query`, `@Mutation` for GraphQL
  - [ ] Parameter annotations: `@Body`, `@Path`, `@QueryParam`, `@Header`, `@Field`
- [ ] Create KSP processor module in `build-logic/ksp-processor`
- [ ] Implement HTTP method generation
  - [ ] Path parameter substitution (`{userId}` → `$userId`)
  - [ ] Query parameter mapping
  - [ ] Header mapping
  - [ ] Body serialization
- [ ] Implement WebSocket subscription generation
- [ ] Implement GraphQL query/mutation generation (optional)
- [ ] Add Koin integration for generated implementations
- [ ] Write tests for KSP processor

### Phase 3: Auth Feature Migration (1 week)
- [ ] Create `AuthError` sealed class in `feature:auth:domain`
- [ ] Create `AuthRemoteDataSource` interface with annotations
- [ ] Generate implementation via KSP
- [ ] Update `AuthRepository` to use new DataSource
- [ ] Implement `toDomainError()` extension function for auth errors
- [ ] Migrate `LoginWithGoogleUseCase` to exception-based flow
- [ ] Update `AuthViewModel` to use `.catch` instead of Either
- [ ] Test exception-based error handling end-to-end

### Phase 4: OpenAPI Integration (2-3 weeks)
- [ ] Research OpenAPI Generator + Mustache templates
- [ ] Create custom Mustache templates for DataSource generation
  - [ ] Template for interface with annotations
  - [ ] Template for DTOs
- [ ] Create Gradle tasks
  - [ ] `downloadApiSpec` task
  - [ ] `generateApiClient` task
- [ ] Generate Auth API from Swagger spec
- [ ] Validate generated code matches manual implementation
- [ ] Document OpenAPI workflow in README

### Phase 5: WebSocket & GraphQL Support (Optional, 2-3 weeks)
- [ ] Implement `KtorSocketApi` fully
- [ ] Test WebSocket subscription with real server
- [ ] Add `@Subscribe` annotation and KSP generation
- [ ] Implement `ApolloGraphQLApi` (if needed)
- [ ] Add `@Query`, `@Mutation` annotations and KSP generation
- [ ] Test GraphQL with real server

### Phase 6: Testing & Documentation (1-2 weeks)
- [ ] Write unit tests for error mapping
- [ ] Write integration tests for Repository layer
- [ ] Create mock `RemoteApi` for DataSource tests
- [ ] Document architecture in this file
- [ ] Create migration guide for other features
- [ ] Team training session

---

## 14. Future: Streaming Use Cases (Either Pattern)

### 14.1 When to Use Either Instead of Exception

Exception-based approach (current) works great for:
- ✅ Single-emit use cases (login, logout, one-time fetch)
- ✅ Offline-first with terminal error (cached data → fresh or error)
- ✅ Simple CRUD operations

**However**, for long-running streams with **recoverable errors**, use `Either<DomainError, T>`:

### 14.2 WebSocket / SSE Use Cases

```kotlin
// Example: Chat messages stream
fun observeChatMessages(roomId: String): Flow<Either<DomainError, ChatMessage>> = flow {
    webSocket.connect("chat/$roomId")
        .collect { rawMessage ->
            try {
                val message = parseMessage(rawMessage)
                emit(message.right()) // Success
            } catch (e: Exception) {
                emit(e.toDomainError().left()) // Error, but flow continues!
            }
        }
}

// ViewModel
chatMessagesUseCase(roomId).collect { result ->
    result.fold(
        ifLeft = { error ->
            // Show error toast, but keep displaying previous messages
            showError(error.toUiMessage())
        },
        ifRight = { message ->
            // Add new message to list
            _messages.update { it + message }
        }
    )
}
```

### 14.3 Retry with Feedback

```kotlin
// Example: Data sync with retry feedback
fun syncData(): Flow<Either<DomainError, SyncProgress>> = flow {
    repeat(3) { attempt ->
        emit(SyncProgress.Syncing(attempt + 1).right()) // Show progress

        try {
            val result = remoteDataSource.syncAll()
            emit(SyncProgress.Complete(result).right())
            return@flow // Success, exit
        } catch (e: Exception) {
            if (attempt < 2) {
                emit(e.toDomainError().left()) // Show error, but retry
                delay(1000 * (attempt + 1))
            } else {
                emit(SyncProgress.Failed(e.toDomainError()).right())
            }
        }
    }
}

// ViewModel shows: "Syncing attempt 1... Error. Retrying... Syncing attempt 2..."
```

### 14.4 Partial Success Scenarios

```kotlin
// Example: Sync multiple resources, some may fail
fun syncAllResources(): Flow<Either<DomainError, SyncResult>> = flow {
    // Sync users
    try {
        syncUsers()
        emit(SyncResult.UsersSynced.right())
    } catch (e: Exception) {
        emit(e.toDomainError().left()) // Users failed, but continue
    }

    // Sync posts
    try {
        syncPosts()
        emit(SyncResult.PostsSynced.right())
    } catch (e: Exception) {
        emit(e.toDomainError().left()) // Posts failed, but continue
    }

    emit(SyncResult.Complete.right())
}
```

### 14.5 Implementation Note

When implementing Either pattern for streaming:
1. Use Arrow's `Either` type: `Either<DomainError, T>`
2. Helper extension:
   ```kotlin
   fun <T> T.right(): Either<DomainError, T> = Either.Right(this)
   fun DomainError.left(): Either<DomainError, Nothing> = Either.Left(this)
   ```
3. ViewModel uses `.collect` with `.fold(ifLeft, ifRight)`

---

## Decisions Made

### ✅ Exception-Based Error Handling
- **Decision:** Use exception-based approach for Auth feature
- **Rationale:** Single-emit use cases, terminal error sufficient, less boilerplate
- **Status:** Primary approach

### ✅ RemoteApi Delegation Pattern
- **Decision:** Split RemoteApi into RestApi, SocketApi, GraphQLApi with delegation
- **Rationale:** Separation of concerns, modularity, testability
- **Status:** Finalized

### ✅ DomainError as Exception
- **Decision:** `sealed class DomainError : Exception`
- **Rationale:** KMP compatible, Flow retry/catch works naturally, type-safe pattern matching
- **Status:** Finalized

### ✅ DTO Location
- **Decision:** DTOs live in `feature:xxx:data` module
- **Rationale:** `core:remote` should not know about domain-specific DTOs
- **Status:** Finalized

### ✅ Serialization Library
- **Decision:** kotlinx.serialization (abstracted via SerializationAdapter)
- **Rationale:** KMP-first, but swappable if needed
- **Status:** Finalized

---

## References

- [Kotlin Flow Guide](https://kotlinlang.org/docs/flow.html)
- [KSP Documentation](https://kotlinlang.org/docs/ksp-overview.html)
- [Arrow-kt Documentation](https://arrow-kt.io/)
- [OpenAPI Generator](https://openapi-generator.tech/)
- [Ktor Client Documentation](https://ktor.io/docs/client.html)
- [Kotlin Delegation Pattern](https://kotlinlang.org/docs/delegation.html)

---

**Son Güncelleme:** 2026-03-01
**Yazar:** DomatApp Team
**Status:** 🟢 Design Finalized - Ready for Implementation