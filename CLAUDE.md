# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DomatApp is a **Kotlin Multiplatform (KMP)** application targeting Android and iOS with strict **Feature-Based Modularization** and **Clean Architecture**. The project uses **Jetpack Compose for Android** and **100% Native SwiftUI for iOS** - UI code is NOT shared between platforms.

## Build Commands
/
### Android
```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Run tests (when enabled)
./gradlew test

# Check dependencies
./gradlew :composeApp:dependencies
```

### iOS
Open `/iosApp` directory in Xcode or use the IDE's run configuration. The iOS app consumes the `Shared.framework` built from the `:shared` module.

## Architecture

### Module Structure & Responsibilities

The project follows a strict layered architecture:

```
:composeApp/              → Android app entry point (Compose UI)
:shared/                  → Umbrella Framework for iOS (exports core + feature modules)
:core:{module}/           → Infrastructure layer
  :core:common/           → Shared utilities and extensions
  :core:data/             → Data utilities and base repository patterns
  :core:domain/           → Shared domain models across features
  :core:resulting/        → Error handling (DomainError, RemoteError, ValidationError, SerializationError)
  :core:serialization/    → Serialization abstraction (SerializationApi, custom serializers)
  :core:remote/           → Network layer (Ktor REST/WebSocket, Firebase Firestore/RemoteConfig)
  :core:local/            → Local storage abstractions
  :core:navigation/       → Navigation definitions
  :core:resource/         → Shared resources
  :core:localization/     → i18n support
:feature:{name}:domain/   → 100% Pure Kotlin (UseCases, Models, Repository Interfaces)
:feature:{name}:data/     → Repository implementations, DataSources (Firebase/Ktor)
:feature:{name}:presentation/ → ViewModels, StateFlow, MVI (shared between Android & iOS)
```

### Dependency Rules

**CRITICAL:** Features use Clean Architecture with strict boundaries:
- **Domain layer**: Depends on `:core:domain` and `:core:resulting`. No other dependencies.
- **Data layer**: Depends on its own `domain`, plus `:core:remote`, `:core:local`, `:core:data`, `:core:resulting`.
- **Presentation layer**: Depends ONLY on its own `domain`, plus `:core:common` and `:core:navigation`. Never depends on `data`.

**Core Module Dependencies:**
- **core:serialization** → `:core:resulting` (for SerializationError)
- **core:remote** → `:core:resulting` (for RemoteError), `:core:serialization`
- **core:local** → `:core:serialization` (for storage serialization)
- **core:data** → `:core:domain`, `:core:resulting`
- **core:resulting** → No dependencies (base module for error handling)

### Convention Plugins

All KMP library modules MUST use the custom convention plugin:

```kotlin
plugins {
    alias(libs.plugins.domatapp.kmp.library)
}
```

This plugin (located in `build-logic/`) automatically:
- Applies KMP and Android Library plugins
- Configures iOS targets (iosArm64, iosSimulatorArm64)
- Sets `namespace` based on module path (`:feature:auth:data` → `com.domatapp.feature.auth.data`)
- Sets `compileSdk = 36`, `minSdk = 30`

**Do NOT manually add** `namespace`, `compileSdk`, or `minSdk` in module build files when using this plugin.

### iOS Framework Strategy

Only `:shared` module exports an iOS framework (`Shared.framework`). Individual feature modules do NOT generate separate frameworks. The `:composeApp` module has NO iOS targets - it's Android-only.

## Authentication Architecture (MVI + Side Effects)

Google Sign-In implementation follows the pattern documented in `feature/auth/ARCHITECTURE.md`:

1. **Native UI** (Compose/SwiftUI) handles OS-specific modal dialogs
2. **Shared ViewModel** (presentation layer) emits `AuthEffect.LaunchGoogleSignIn`
3. **Native UI** observes effects, launches Google Sign-In, receives `idToken`
4. **Native UI** sends `AuthIntent.OnGoogleTokenReceived(idToken)` back to ViewModel
5. **ViewModel** calls `LoginWithGoogleUseCase(idToken)`
6. **Repository** (data layer) uses `AuthRemoteDataSource` (Firebase or Ktor implementation)

This keeps the `shared` module pure - no Android `Context` or iOS framework dependencies.

## Error Handling Architecture (Exception-Based)

The project uses **exception-based error handling** with a strict mapping chain from infrastructure to domain errors.

### Module: core:resulting

Central error handling module containing:
- **DomainError**: Base sealed class for all domain errors
- **RemoteError**: Infrastructure-level remote API errors (timeout, no connection, HTTP errors)
- **SerializationError**: Serialization/deserialization errors (encoding, decoding, type mismatch)
- **ValidationError**: Input validation errors (future use)

### Module: core:serialization

Serialization abstraction module containing:
- **SerializationApi**: Interface for serialization/deserialization
- **KotlinxSerializationApi**: kotlinx.serialization implementation
- **SerializationExceptionMapper**: Maps library exceptions to SerializationError
- **Custom Serializers**: Instant, UUID, BigDecimal, etc. (future use)

### Exception Mapping Flow

```
kotlinx.serialization Exception (MissingFieldException, SerializationException, etc.)
  ↓ [core:serialization maps to]
SerializationError (SerializationError.DecodingError, SerializationError.MissingFieldError, etc.)
  ↓ [thrown as DomainError]

Ktor Exception (ClientRequestException, TimeoutException, etc.)
  ↓ [core:remote maps to]
RemoteError (RemoteError.Timeout, RemoteError.ClientError(401), etc.)
  ↓ [both errors flow to feature:data]

SerializationError + RemoteError
  ↓ [feature:data maps to]
AuthError (AuthError.InvalidCredentials, AuthError.UserNotFound, etc.)
  ↓ [feature:domain/presentation]
ViewModel.catch { }
```

### Implementation Pattern

**core:remote** (Infrastructure Layer - `KtorRestClient`):
```kotlin
// Automatically maps Ktor exceptions to RemoteError
suspend fun <T> get(/*...*/): T = try {
    client.get(/*...*/)
} catch (e: Exception) {
    throw e.toRemoteError() // ConnectTimeoutException → RemoteError.Timeout
}
```

**feature:auth:data** (Data Layer):
```kotlin
// Maps RemoteError to feature-specific AuthError
override fun login(idToken: String): Flow<AuthSession> = flow {
    val dto = remoteDataSource.signInWithGoogle(idToken)
    emit(dto.toDomain())
}
.retryWhen { cause, attempt ->
    // Retry on remote connection errors
    cause is RemoteError.NoConnection && attempt < 3
}
.catch { exception ->
    throw exception.toAuthError() // RemoteError.ClientError(401) → AuthError.InvalidCredentials
}
```

**feature:auth:domain** (Domain Layer):
```kotlin
sealed class AuthError : DomainError {
    data object InvalidCredentials
    data object UserNotFound
    data object EmailAlreadyInUse
}
```

### Benefits

- ✅ **No Code Duplication**: Ktor→RemoteError mapping happens once in core:remote
- ✅ **Clean Architecture**: Infrastructure errors don't leak to domain layer
- ✅ **Type Safety**: Compile-time error checking
- ✅ **Retry/Resilience**: Flow operators work naturally with exceptions
- ✅ **Feature-Specific**: Each feature maps RemoteError to its own domain errors

## Remote DataSource Code Generation (KSP Annotations)

The project uses **KSP annotations** to automatically generate DataSource implementations. The KSP processor analyzes which backend types each DataSource uses and injects only the required concrete clients.

### Concrete Clients (core:remote)

- **`KtorRestClient`**: Ktor HttpClient for REST (GET, POST, PUT, PATCH, DELETE)
- **`KtorSocketClient`**: Ktor WebSocket for real-time communication
- **`FirebaseFirestoreClient`**: Firebase Firestore for document CRUD and realtime observation
- **`FirebaseRemoteConfigClient`**: Firebase Remote Config for feature flags and configuration

### Annotations

**HTTP Methods (→ `KtorRestClient`):**
- `@GET(path)` - HTTP GET request
- `@POST(path)` - HTTP POST request
- `@PUT(path)` - HTTP PUT request
- `@PATCH(path)` - HTTP PATCH request
- `@DELETE(path)` - HTTP DELETE request

**WebSocket Methods (→ `KtorSocketClient`):**
- `@Subscribe(path)` - WebSocket subscription (returns Flow)
- `@Send(path)` - WebSocket send/receive

**Firestore Methods (→ `FirebaseFirestoreClient`):**
- `@GetDocument(collection)` - Get a single document
- `@AddDocument(collection)` - Add a new document
- `@SetDocument(collection)` - Set/overwrite a document
- `@UpdateDocument(collection)` - Update specific fields
- `@DeleteDocument(collection)` - Delete a document
- `@QueryCollection(collection)` - Query with filters
- `@ObserveDocument(collection)` - Realtime document observation
- `@ObserveCollection(collection)` - Realtime collection observation

**Remote Config Methods (→ `FirebaseRemoteConfigClient`):**
- `@FetchRemoteConfig` - Fetch and activate remote config
- `@GetRemoteConfig(key)` - Get a config value
- `@ObserveRemoteConfig(key)` - Observe config changes

**Parameters:**
- `@Body` - Request body (will be serialized)
- `@Query(name)` - Query parameter
- `@Header(name)` - Single header
- `@HeaderMap` - Map of headers
- `@Path(name)` - Path parameter (replaces {name} in path)
- `@DocumentId` - Firestore document ID
- `@Field(name)` - Firestore field for updates
- `@WhereEqualTo(field)`, `@WhereIn(field)`, etc. - Firestore query filters
- `@OrderBy(field, direction)` - Firestore ordering
- `@Limit(value)` - Firestore query limit

### Usage Example

```kotlin
@RemoteDataSource
interface AuthRemoteDataSource {

    @POST("auth/google")
    suspend fun signInWithGoogle(
        @Body request: GoogleSignInRequest
    ): RemoteUserDto

    @GET("auth/session")
    suspend fun getSession(
        @Header("Authorization") token: String
    ): RemoteUserDto

    @DELETE("auth/session")
    suspend fun logout(
        @Header("Authorization") token: String,
        @HeaderMap additionalHeaders: Map<String, String>
    )

    @Subscribe("auth/events")
    fun subscribeToAuthEvents(): Flow<AuthEvent>
}

// KSP automatically generates (only REST + Socket clients injected based on usage):
class AuthRemoteDataSourceImpl(
    private val restClient: KtorRestClient,
    private val socketClient: KtorSocketClient,
) : AuthRemoteDataSource {
    override suspend fun signInWithGoogle(request: GoogleSignInRequest): RemoteUserDto {
        return restClient.post(
            path = "auth/google",
            body = request,
            headers = emptyMap(),
            responseType = RemoteUserDto::class
        )
    }
    // ... other methods
}
```

**Mixed Backend DataSource (REST + Firestore):**
```kotlin
@RemoteDataSource
interface ProductRemoteDataSource {
    @GET("products/{id}")
    suspend fun getProductFromApi(@Path("id") id: String): ProductDto

    @GetDocument("products")
    suspend fun getProductFromFirestore(@DocumentId id: String): ProductDto
}

// Generated with both clients:
class ProductRemoteDataSourceImpl(
    private val restClient: KtorRestClient,
    private val firestoreClient: FirebaseFirestoreClient,
) : ProductRemoteDataSource { ... }
```

**Benefits:**
- ✅ **No Boilerplate**: Implementation automatically generated
- ✅ **Type-Safe**: Compile-time validation
- ✅ **Minimal Dependencies**: Only required clients are injected per DataSource
- ✅ **Mixed Backends**: REST, WebSocket, Firestore, and RemoteConfig can coexist in one DataSource

## Backend Strategy (Concrete Clients + DataSource Pattern)

### Concrete Client Architecture

The `core:remote` module provides concrete client classes for each backend type. There are no abstraction interfaces - each client is used directly:

- **`KtorRestClient`** (`core:remote/rest/`): Ktor HttpClient for REST (GET, POST, PUT, PATCH, DELETE)
- **`KtorSocketClient`** (`core:remote/socket/`): Ktor WebSocket for real-time communication
- **`FirebaseFirestoreClient`** (`core:remote/firestore/`): Firebase Firestore CRUD and realtime
- **`FirebaseRemoteConfigClient`** (`core:remote/remoteconfig/`): Firebase Remote Config

All clients are `@Single` annotated and discovered by Koin via `@ComponentScan`.

### DataSource Pattern (Feature Layer)

Each feature defines its own DataSource interface with `@RemoteDataSource` annotation:

- **Interface**: `AuthRemoteDataSource` (in `feature:auth:data`)
- **Implementation**: `AuthRemoteDataSourceImpl` is KSP-generated, injecting only the concrete clients it needs

**Example:**
```kotlin
// KSP-generated implementation injects only KtorRestClient (since only REST annotations are used)
class AuthRemoteDataSourceImpl(
    private val restClient: KtorRestClient
) : AuthRemoteDataSource {
    override suspend fun signInWithGoogle(request: GoogleSignInRequest): RemoteUserDto {
        return restClient.post(
            path = "auth/google",
            body = request,
            headers = emptyMap(),
            responseType = RemoteUserDto::class
        )
    }
}
```

Repository implementations MUST only orchestrate between `RemoteDataSource` and `LocalDataSource` interfaces. Never use concrete clients directly in repositories.

## Version Catalog

All dependencies are managed in `gradle/libs.versions.toml`:
- Uses single centralized file (no splitting)
- Prefixes for organization: `androidx-*`, `compose-*`, `kotlinx-*`, `firebase-*`
- Plugin dependencies for `build-logic` are also defined here

## Testing

Test source sets are currently disabled across core and feature modules. Do not automatically add test dependencies or generate test files unless explicitly requested.

## Dependency Injection (Koin Annotations)

The project uses **Koin with KSP Annotations** for dependency injection, NOT Koin DSL.

### Module Setup

Each module defines a `@Module` class with `@ComponentScan`:

```kotlin
@Module
@ComponentScan("com.domatapp.core.remote")
class CoreRemoteModule {
    @Single
    fun provideHttpClient(): HttpClient { /*...*/ }
}
```

### Class Annotations

- **@Single**: Singleton scoped (repositories, data sources, API clients)
- **@Factory**: New instance on each injection (use cases)

**Example:**
```kotlin
@Single
class KtorRestClient(
    private val client: HttpClient,
    private val serializer: SerializationApi
) { /*...*/ }

@Factory
class LoginWithGoogleUseCase(
    private val repository: AuthRepository
) { /*...*/ }
```

### KSP Configuration

All modules using Koin must include:

```kotlin
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    kspCommonMainMetadata(libs.koin.ksp.compiler)
}
```

KSP generates module code at build time. **Never use `module { }` DSL syntax.**

## Key Technologies

- **KMP**: Kotlin 2.3.10, Compose Multiplatform 1.10.1
- **Android**: minSdk 30, targetSdk 36, AGP 9.0.1
- **UI**: Jetpack Compose (Android), SwiftUI (iOS)
- **Architecture**: Arrow-kt for functional programming, Coroutines + Flow
- **DI**: Koin 3.5.3 with Annotations 1.3.1 (KSP code generation)
- **Networking**: Ktor Client 2.3.8 (REST + WebSocket)
- **Backend**: Firebase Auth (GitLive 2.4.0) with Ktor migration path
- **Serialization**: kotlinx.serialization 1.8.0
- **Error Handling**: Exception-based with core:resulting module