# GitHub Copilot Instructions for DomatApp

This document guides AI assistants (Copilot, Claude, etc.) working on the DomatApp codebase. For detailed guidance on specific tools, see `CLAUDE.md` and `GEMINI.md`.

## Quick Start

**Project Type:** Kotlin Multiplatform (KMP) with feature-based modularization
- **Android:** Jetpack Compose (in `:composeApp`)
- **iOS:** 100% Native SwiftUI (consumes `Shared.framework` from `:shared`)

**Key Architecture Rule:** UI code is NOT shared between platforms. The `composeApp` module has no iOS targets.

## Build & Run Commands

### Android
```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Run all Android tests (currently disabled, enable if needed)
./gradlew test

# Check Android app dependencies
./gradlew :composeApp:dependencies

# Build a single module
./gradlew :core:remote:build
./gradlew :feature:auth:domain:build
```

### iOS
- Open `/iosApp` in Xcode and run from IDE
- The `Shared.framework` is automatically built from the `:shared` module when you build the iOS app

### Gradle & Build Properties
- **Kotlin Version:** 2.3.10
- **AGP:** 9.0.1
- **Compose Multiplatform:** 1.10.1
- **Min SDK (Android):** 30 | **Target SDK:** 36
- **iOS Targets:** iosArm64, iosSimulatorArm64
- **Configuration Cache:** Enabled (fast builds on second run)
- **KSP:** Enabled for annotation processing (Koin DI, code generation)
- **Strict Mode:** `allWarningsAsErrors=true` — all compiler warnings fail the build

## Architecture & Module Structure

### Modularization Pattern: Feature-Based + Layered
```
:composeApp/                    Android app entry (Compose UI only)
:shared/                        iOS Umbrella Framework (exports core + features)
:core:{module}/                 Infrastructure Layer
  :core:remote/                 Network (Ktor REST/WebSocket)
  :core:local/                  Local storage (Room, SQLDelight, DataStore)
  :core:serialization/          Serialization abstraction (kotlinx.serialization)
  :core:resulting/              Error types (DomainError, RemoteError, SerializationError)
  :core:domain/                 Shared domain models
  :core:data/                   Base repository patterns
  :core:common/                 Utils, extensions, formatters
  :core:navigation/             Route definitions
  :core:resource/               Assets, fonts, icons
  :core:locale/                 i18n/localization
  :core:presentation/           Base ViewModels, UI state patterns
:feature:{name}:{layer}/        Business features
  ├── domain/                   100% pure Kotlin (UseCases, Models, Repository Interfaces)
  ├── data/                     Repository implementations, DataSources, DTOs
  └── presentation/             ViewModels, StateFlow, MVI (shared for Android/iOS)
:build-logic/                   Convention plugins (enforces consistent Gradle configs)
```

### Dependency Rules (STRICT - Violations Break Architecture)

**Feature Domain Layer** (`feature:auth:domain`)
- ✅ Depends on: `:core:domain`, `:core:resulting`
- ❌ Never imports: `data`, `presentation`, Android/iOS classes, DTOs

**Feature Data Layer** (`feature:auth:data`)
- ✅ Depends on: Own `domain`, `:core:remote`, `:core:local`, `:core:data`, `:core:resulting`, `:core:serialization`
- ❌ Never imports: `presentation`, Android `Context`, iOS frameworks

**Feature Presentation Layer** (`feature:auth:presentation`)
- ✅ Depends on: Own `domain`, `:core:common`, `:core:navigation`
- ❌ Never imports: `data`, Android/iOS specific classes, DTOs from data layer

**Core Module Dependencies**
- `core:resulting` → No dependencies (base module)
- `core:serialization` → `core:resulting` (for SerializationError)
- `core:remote` → `core:resulting`, `core:serialization`
- `core:local` → `core:serialization`
- `core:data` → `core:domain`, `core:resulting`

### Convention Plugin (Mandatory for New Modules)

All new KMP library modules **MUST** use:
```kotlin
plugins {
    alias(libs.plugins.domatapp.kmp.library)
}
```

This plugin automatically:
- Applies KMP + Android Library plugins
- Configures iOS targets
- Generates `namespace` from module path (e.g., `:feature:auth:data` → `com.domatapp.feature.auth.data`)
- Sets `compileSdk = 36`, `minSdk = 30`

**Never manually add** `namespace`, `compileSdk`, or `minSdk` when using this plugin.

## Error Handling (Exception-Based)

### Error Type Hierarchy
```
DomainError (base sealed class)
  ├─ RemoteError (infrastructure errors from Ktor)
  ├─ SerializationError (serialization failures)
  └─ ValidationError (input validation)
```

### Exception Mapping Flow
```
Ktor Exception (TimeoutException, ClientRequestException, etc.)
  ↓ [core:remote]
RemoteError (RemoteError.Timeout, RemoteError.ClientError(401), etc.)
  ↓ [feature:auth:data]
AuthError (AuthError.InvalidCredentials, AuthError.UserNotFound, etc.)
  ↓ [feature:auth:presentation/ViewModel]
UI responds to sealed error type
```

### Implementation Pattern
**In core:remote:**
```kotlin
override suspend fun <T> get(/*...*/): T = try {
    client.get(/*...*/)
} catch (e: Exception) {
    throw e.toRemoteError()  // Maps Ktor exceptions to RemoteError
}
```

**In feature:auth:data:**
```kotlin
override fun login(idToken: String): Flow<AuthSession> = flow {
    emit(remoteDataSource.signInWithGoogle(idToken).toDomain())
}.catch { exception ->
    throw exception.toAuthError()  // Maps RemoteError to AuthError
}
```

**In feature:auth:domain:**
```kotlin
sealed class AuthError : DomainError {
    data object InvalidCredentials
    data object UserNotFound
    data object EmailAlreadyInUse
}
```

## Dependency Injection (Koin Annotations)

Uses **Koin with KSP Annotations** (NOT Koin DSL).

### Module Setup
```kotlin
@Module
@ComponentScan("com.domatapp.core.remote")
class CoreRemoteModule {
    @Single
    fun provideHttpClient(): HttpClient { /*...*/ }
}
```

### Class Annotations
- `@Single`: Singleton (repositories, data sources, API clients)
- `@Factory`: New instance per injection (use cases)

### KSP Configuration
All modules using Koin require:
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

## RemoteDataSource Code Generation (KSP Annotations)

DataSources are auto-generated from annotated interfaces using KSP.

### HTTP Annotations
```kotlin
@RemoteDataSource
interface AuthRemoteDataSource {
    @GET("auth/user/{id}")
    suspend fun getUser(@Path("id") id: String): RemoteUserDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): RemoteUserDto

    @PUT("auth/profile")
    suspend fun updateProfile(
        @Body request: ProfileUpdateRequest,
        @Header("Authorization") token: String
    ): RemoteUserDto

    @DELETE("auth/session")
    suspend fun logout(@Header("Authorization") token: String)
}
```

### WebSocket Annotations
```kotlin
@Subscribe("auth/events")
fun subscribeToAuthEvents(): Flow<AuthEvent>

@Send("auth/events")
suspend fun sendAuthEvent(event: AuthEvent)
```

### Parameter Annotations
- `@Body` — Request body (will be serialized)
- `@Query(name)` — Query parameter
- `@Header(name)` — Single header
- `@HeaderMap` — Map of headers
- `@Path(name)` — Path parameter (replaces `{name}` in path)

## Remote API Strategy

`core:remote` provides a unified `RemoteApi` interface:

```kotlin
// Protocol-specific interfaces
interface RestApi { suspend fun <T> get(/*...*/): T }
interface SocketApi { fun <T> subscribe(/*...*/): Flow<T> }

// Unified interface
interface RemoteApi : RestApi, SocketApi

// Delegation implementation (in core:remote module)
@Single
class RemoteApiImpl(
    restApi: RestApi,       // KtorRestApi
    socketApi: SocketApi    // KtorSocketApi
) : RemoteApi,
    RestApi by restApi,
    SocketApi by socketApi
```

**Key Rule:** Repositories MUST only use DataSource interfaces, never `RemoteApi` directly.

## Version Catalog

All dependencies in `gradle/libs.versions.toml` (single file, never split):
- Organized with prefixes: `androidx-*`, `compose-*`, `kotlinx-*`, `firebase-*`, `koin-*`
- Plugin definitions also included

## Key Technologies

| Technology | Version | Notes |
|-----------|---------|-------|
| **Kotlin** | 2.3.10 | All-warnings-as-errors enabled |
| **Compose Multiplatform** | 1.10.1 | Android + iOS UI |
| **Jetpack Compose** | Latest | Android UI layer |
| **SwiftUI** | Native | iOS UI layer (100% separate) |
| **Ktor Client** | 2.3.8 | REST + WebSocket |
| **Firebase Auth** | GitLive KMP SDK | Initial backend (Ktor migration path ready) |
| **kotlinx.serialization** | 1.8.0 | All serialization |
| **Koin** | 3.5.3 with Annotations 1.3.1 | DI with KSP code generation |
| **Arrow-kt** | Latest | Functional programming |

## Testing

Test source sets (`commonTest`, `androidInstrumentedTest`, etc.) are **currently disabled** across core and feature modules.
- Do NOT automatically add test dependencies
- Do NOT generate test files unless explicitly requested
- When enabled, follow Clean Architecture patterns: separate tests for domain/data/presentation layers

## Common Tasks

### Create a New Feature Module
1. Add module to `settings.gradle.kts`
2. Create directory structure: `feature/{name}/{domain,data,presentation}/`
3. Each submodule uses: `plugins { alias(libs.plugins.domatapp.kmp.library) }`
4. Define dependency chain: `domain` → `data` (has `domain`) → `presentation` (has `domain`)

### Add a New Core Utility
1. Add to appropriate `core:*` module or create new if needed
2. Ensure it has no feature/business-logic dependencies
3. Add KSP module setup if it provides injectable classes

### Add a Remote Endpoint
1. Define `@RemoteDataSource` interface in `feature:*/data`
2. Use KSP-generated implementation automatically
3. Feature repository orchestrates between DataSource and LocalDataSource
4. Map `RemoteError` to feature-specific domain errors in data layer

### ViewModels & StateFlow (iOS/Android Compatibility)
- All ViewModels in `feature:*/presentation` use `StateFlow<UiState>` and `SharedFlow<UiEffect>`
- Never use Compose-specific classes (like `androidx.compose.runtime.State`) in shared modules
- Ensure Swift/Combine can easily consume the flows

## Architectural Checks Before Making Changes

❓ **If modifying a feature:** Are dependencies respecting layer boundaries?
❓ **If adding infrastructure code:** Should it go in a core module instead?
❓ **If creating ViewModels:** Are they using platform-agnostic Flow APIs?
❓ **If adding Network code:** Is it behind a DataSource interface?
❓ **If handling errors:** Is the error properly mapped at each layer?

## Important Project Files

| File | Purpose |
|------|---------|
| `CLAUDE.md` | Detailed architecture guide for Claude AI |
| `GEMINI.md` | Developer persona & operating instructions for Gemini CLI |
| `build-logic/` | Convention plugins enforcing consistent Gradle configs |
| `gradle/libs.versions.toml` | Single centralized dependency catalog |
| `settings.gradle.kts` | All module definitions |
| `composeApp/build.gradle.kts` | Android app configuration |
| `shared/build.gradle.kts` | iOS Umbrella Framework configuration |

## Notes for AI Assistants

- **Proactive Warnings:** If a proposed change violates architecture (e.g., presentation depending on data), flag it before implementing
- **Configuration Cache:** First build is slow, subsequent builds are fast due to Gradle configuration cache
- **iOS Framework:** Only `:shared` generates an iOS framework; individual feature modules do not
- **No Manual Boilerplate:** Use KSP annotations and convention plugins to avoid code generation
- **SwiftUI Compatibility:** Avoid `@Composable` or Compose-specific APIs in KMP `shared` or `presentation` modules
