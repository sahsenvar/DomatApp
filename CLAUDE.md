# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DomatApp is a **Kotlin Multiplatform (KMP)** application targeting Android and iOS with strict **Feature-Based Modularization** and **Clean Architecture**. The project uses **Jetpack Compose for Android** and **100% Native SwiftUI for iOS** - UI code is NOT shared between platforms.

## Build Commands

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
:shared/                  → Umbrella Framework for iOS + AppDatabase + DI aggregation
:core:{module}/           → Infrastructure layer
  :core:common/           → Shared utilities and extensions
  :core:data/             → Data utilities and base repository patterns
  :core:domain/           → Shared domain models across features
  :core:resulting/        → Error handling (DomainError, RemoteError, LocalError, ValidationError, SerializationError)
  :core:serialization/    → Serialization abstraction (SerializationApi, custom serializers)
  :core:remote/           → Network layer (Ktor REST/WebSocket, Firebase Firestore)
  :core:config/           → Configuration layer (DataStore key-value, Firebase RemoteConfig)
  :core:navigation/       → Navigation definitions
  :core:resource/         → Shared resources
  :core:localization/     → i18n support
:feature:{name}:domain/   → 100% Pure Kotlin (UseCases, Models, Repository Interfaces)
:feature:{name}:data/     → Repository implementations, DataSources, Room Entity/DAO
:feature:{name}:presentation/ → ViewModels, StateFlow, MVI (shared between Android & iOS)
```

### DataSource Architecture (3-Layer)

Each feature's data layer has up to **3 DataSource types**:

```
feature:{name}:data/
├── datasource/
│   ├── {Name}RemoteDataSource    → @RemoteDataSource (KSP generated) - REST, WebSocket, Firestore
│   ├── {Name}LocalDataSource     → @Dao (Room DAO) - structured database operations
│   └── {Name}ConfigDataSource    → @ConfigDataSource (KSP generated) - DataStore + RemoteConfig
├── local/
│   └── entity/
│       └── {Name}Entity.kt      → @Entity (Room entity)
└── repository/
    └── {Name}RepositoryImpl.kt   → orchestrates all 3 DataSources
```

### Room Schema Ownership

Each feature module owns its Room schema (Entity + DAO). `AppDatabase` lives in `:shared` and
aggregates all feature DAOs:

```kotlin
// shared/.../database/AppDatabase.kt
@Database(entities = [AuthSessionEntity::class, /* future entities */], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authLocalDataSource(): AuthLocalDataSource
    // future DAOs added here
}
```

**Adding a new feature's database schema:**

1. Create Entity in `feature/{name}/data/local/entity/`
2. Create Room `@Dao` interface as `{Name}LocalDataSource` in `feature/{name}/data/datasource/`
3. Add `abstract fun` to `shared/.../database/AppDatabase.kt`
4. Add `single { get<AppDatabase>().{name}LocalDataSource() }` to `shared/.../di/KoinInitializer.kt`
5. Add `implementation(libs.androidx.room.runtime)` to feature's `build.gradle.kts`

### Dependency Rules

**CRITICAL:** Features use Clean Architecture with strict boundaries:
- **Domain layer**: Depends on `:core:domain` and `:core:resulting`. No other dependencies.
- **Data layer**: Depends on its own `domain`, plus `:core:remote`, `:core:config`, `:core:data`,
  `:core:resulting`.
- **Presentation layer**: Depends ONLY on its own `domain`, plus `:core:common` and `:core:navigation`. Never depends on `data`.

**Core Module Dependencies:**
- **core:serialization** → `:core:resulting` (for SerializationError)
- **core:remote** → `:core:resulting` (for RemoteError), `:core:serialization`
- **core:config** → `:core:resulting`, `:core:serialization`
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
- **LocalError**: Database errors (constraint violation, not found, corruption)
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

**core:remote** (Infrastructure Layer):
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
    cause is RemoteError.NoConnection && attempt < 3
}
.catch { exception ->
    throw exception.toAuthError() // RemoteError.ClientError(401) → AuthError.InvalidCredentials
}
```

## Remote DataSource Code Generation (KSP Annotations)

The project uses **KSP annotations** to automatically generate DataSource implementations. The KSP processor analyzes which backend types each DataSource uses and injects only the required concrete clients.

### Concrete Clients (core:remote)

- **`HttpClient`**: Ktor HttpClient for REST (GET, POST, PUT, PATCH, DELETE) and WebSocket
- **`FirebaseFirestoreClient`** (`core:remote/firestore/`): Firebase Firestore for document CRUD and
  realtime observation

### Annotations (core:remote)

**HTTP Methods:**
- `@GET(path)` - HTTP GET request
- `@POST(path)` - HTTP POST request
- `@PUT(path)` - HTTP PUT request
- `@PATCH(path)` - HTTP PATCH request
- `@DELETE(path)` - HTTP DELETE request

**WebSocket Methods:**
- `@Subscribe(path)` - WebSocket subscription (returns Flow)
- `@Send(path)` - WebSocket send/receive

**Firestore Methods:**
- `@GetDocument(collection)` - Get a single document
- `@AddDocument(collection)` - Add a new document
- `@SetDocument(collection)` - Set/overwrite a document
- `@UpdateDocument(collection)` - Update specific fields
- `@DeleteDocument(collection)` - Delete a document
- `@QueryCollection(collection)` - Query with filters
- `@ObserveDocument(collection)` - Realtime document observation
- `@ObserveCollection(collection)` - Realtime collection observation

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
        @Header("Authorization") token: String
    )

    @Subscribe("auth/events")
    fun subscribeToAuthEvents(): Flow<AuthEvent>
}

// KSP automatically generates (only required clients injected based on usage):
class AuthRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val json: Json,
) : AuthRemoteDataSource {
    override suspend fun signInWithGoogle(request: GoogleSignInRequest): RemoteUserDto {
        return httpClient.post("auth/google") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    // ... other methods
}
```

## Config DataSource Code Generation (KSP Annotations)

The `core:config` module provides configuration storage for both **local preferences** (DataStore)
and **remote feature flags** (Firebase RemoteConfig). KSP generates implementations automatically.

### Concrete Clients (core:config)

- **`DataStore<Preferences>`**: Jetpack DataStore for local key-value storage
- **`FirebaseRemoteConfig`** (GitLive `dev.gitlive.firebase.remoteconfig.FirebaseRemoteConfig`):
  Firebase Remote Config, injected directly via Koin (like `HttpClient`)

### Annotations (core:config)

**LocalConfig Methods (→ `DataStore<Preferences>`):**

- `@SaveLocalConfig(key)` - Save a value to key-value store
- `@RetrieveLocalConfig(key)` - Retrieve a single value (`suspend fun`, returns `T`). Compile-time
  error if return type is `Flow`.
- `@ObserveLocalConfig(key)` - Observe a value as `Flow<T?>`. Compile-time error if return type is
  not `Flow`.
- `@ClearLocalConfig(key)` - Remove a specific key
- `@ClearAllLocalConfig` - Clear all values

**RemoteConfig Methods (→ `FirebaseRemoteConfig`):**

- `@RetrieveRemoteConfig(key)` - Retrieve a config value (`suspend fun`, auto `fetchAndActivate()`).
  Compile-time error if return type is `Flow`.
- `@ObserveRemoteConfig(key)` - Observe config changes as `Flow<T>` (polling with auto
  `fetchAndActivate()`). Compile-time error if return type is not `Flow`.

### Usage Example

```kotlin
@ConfigDataSource(name = "auth")
interface AuthConfigDataSource {

    @SaveLocalConfig(key = "access_token")
    suspend fun saveToken(token: String)

    @RetrieveLocalConfig(key = "access_token")
    suspend fun retrieveToken(): String?

    @ObserveLocalConfig(key = "access_token")
    fun observeToken(): Flow<String?>

    @ClearLocalConfig(key = "access_token")
    suspend fun clearToken()

    @ClearAllLocalConfig
    suspend fun clearAll()
}

// KSP automatically generates:
@Single
class AuthConfigDataSourceImpl(
    @Named("auth") private val dataStore: DataStore<Preferences>
) : AuthConfigDataSource {
    override suspend fun saveToken(token: String) {
        dataStore.edit { prefs -> prefs[stringPreferencesKey("access_token")] = token }
    }
    override suspend fun retrieveToken(): String? {
        return dataStore.data.map { prefs -> prefs[stringPreferencesKey("access_token")] }.first()
    }
    override fun observeToken(): Flow<String?> {
        return dataStore.data.map { prefs -> prefs[stringPreferencesKey("access_token")] }
    }
    // ...
}
```

**Mixed Config DataSource (DataStore + RemoteConfig):**

```kotlin
@ConfigDataSource(name = "product")
interface ProductConfigDataSource {
    @SaveLocalConfig(key = "last_category")
    suspend fun saveLastCategory(category: String)

    @RetrieveRemoteConfig("new_feature_enabled")
    suspend fun isNewFeatureEnabled(): Boolean

    @ObserveRemoteConfig("promo_banner_text")
    fun observePromoBanner(): Flow<String>
}

// Generated with both clients (FirebaseRemoteConfig injected directly):
@Single
class ProductConfigDataSourceImpl(
    @Named("product") private val dataStore: DataStore<Preferences>,
    private val remoteConfig: FirebaseRemoteConfig
) : ProductConfigDataSource { ... }
```

## Local DataSource (Room DAO)

Local data sources are **Room `@Dao` interfaces directly** - no KSP code generation needed. Room's
own KSP processor generates the implementations.

```kotlin
// feature/auth/data/datasource/AuthLocalDataSource.kt
@Dao
interface AuthLocalDataSource {
    @Query("SELECT * FROM auth_session WHERE id = :id")
    suspend fun getById(id: String): AuthSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AuthSessionEntity)

    @Query("DELETE FROM auth_session")
    suspend fun deleteAll()
}
```

Room handles implementation generation. The DAO is registered in
`shared/.../database/AppDatabase.kt` and provided via Koin in `shared/.../di/KoinInitializer.kt`.

## Navigation Code Generation (KSP Annotations)

The `core:navigation` module provides annotations for auto-generating route composables and
navigation entries. KSP generates the boilerplate glue code.

### Annotations (core:navigation)

- `@NavigationScreen(route)` — Marks a @Composable as the UI screen for a Route
- `@NavigationViewModel(route)` — Marks a ViewModel as the state holder for a Route
- `@NavigationEffectHandler(route)` — (Optional) Marks a @Composable as the effect handler for a
  Route

### Generated Output

For each Route with matching `@NavigationScreen` + `@NavigationViewModel`:

- `{Name}Route.kt` — Composable that wires ViewModel -> State -> Screen (+ EffectHandler if
  annotated)
- `{Feature}PresentationEntries.kt` — `EntryProviderScope<Route>.{feature}PresentationEntries()`
  extension

### Convention

- Screen: `fun AuthScreen(uiState: AuthUiState, onIntent: (AuthIntent) -> Unit)`
- EffectHandler: `fun AuthEffectHandler(effectFlow: Flow<AuthEffect>)`
- ViewModel: `class AuthViewModel : BaseViewModel<AuthUiState, AuthIntent, AuthEffect>`

### Feature Presentation KSP Setup

Feature presentation modules using these annotations must add:

```kotlin
dependencies {
    add("kspAndroid", projects.core.processor)
}
```

Use `kspAndroid` because `@NavigationScreen` and `@NavigationEffectHandler` live in `androidMain`,
while `@NavigationViewModel` is in `commonMain` but visible during Android compilation.

## Backend Strategy (Concrete Clients + DataSource Pattern)

### Concrete Client Architecture

The project provides concrete client classes across two modules:

**core:remote** (Network):

- **`HttpClient`** (Ktor): REST (GET, POST, PUT, PATCH, DELETE) + WebSocket
- **`FirebaseFirestoreClient`** (`core:remote/firestore/`): Firebase Firestore CRUD and realtime

**core:config** (Configuration):

- **`DataStore<Preferences>`**: Local key-value storage (platform-specific factory)
- **`FirebaseRemoteConfig`** (GitLive): Firebase Remote Config, injected directly via Koin

### DataSource Pattern (Feature Layer)

Each feature defines its own DataSource interfaces:

- **`AuthRemoteDataSource`** → `@RemoteDataSource` (KSP-generated impl)
- **`AuthLocalDataSource`** → Room `@Dao` (Room-generated impl)
- **`AuthConfigDataSource`** → `@ConfigDataSource` (KSP-generated impl)

Repository implementations orchestrate between these 3 DataSources. Never use concrete clients
directly in repositories.

## Version Catalog

All dependencies are managed in `gradle/libs.versions.toml`:
- Uses single centralized file (no splitting)
- Prefixes for organization: `androidx-*`, `compose-*`, `kotlinx-*`, `firebase-*`
- Plugin dependencies for `build-logic` are also defined here

## Testing

Test source sets are currently disabled across core and feature modules. Do not automatically add test dependencies or generate test files unless explicitly requested.

## Dependency Injection (Koin Annotations)

The project uses **Koin with KSP Annotations** for dependency injection, NOT Koin DSL (except for
`databaseModule` in KoinInitializer).

### Module Setup

Each module defines a `@Module` class with `@ComponentScan`:

```kotlin
@Module
@ComponentScan("com.domatapp.core.remote")
class CoreRemoteModule
```

### Class Annotations

- **@Single**: Singleton scoped (repositories, data sources, API clients)
- **@Factory**: New instance on each injection (use cases)

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

KSP generates module code at build time. **Never use `module { }` DSL syntax** (except
`databaseModule` in `KoinInitializer` for Room).

## Key Technologies

- **KMP**: Kotlin 2.3.10, Compose Multiplatform 1.10.1
- **Android**: minSdk 30, targetSdk 36, AGP 9.0.1
- **UI**: Jetpack Compose (Android), SwiftUI (iOS)
- **Architecture**: Arrow-kt for functional programming, Coroutines + Flow
- **DI**: Koin 4.1.1 with Annotations 2.3.1 (KSP code generation)
- **Networking**: Ktor Client 3.4.1 (REST + WebSocket)
- **Database**: Room 2.7.0 (KMP)
- **Storage**: DataStore 1.2.0 (Preferences)
- **Backend**: Firebase Auth (GitLive 2.4.0), Firebase Firestore, Firebase RemoteConfig
- **Serialization**: kotlinx.serialization
- **Error Handling**: Exception-based with core:resulting module
