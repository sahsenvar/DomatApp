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
  :core:resulting/        → Error handling (DomainError, RemoteError, LocalError, ValidationError)
  :core:remote/           → Network layer (Ktor REST via Ktorfit) + the shared Json
  :core:config/           → Configuration layer (DataStore key-value, Firebase RemoteConfig)
  :core:navigation/       → Navigation definitions
  :core:resource/         → Shared resources
  :core:localization/     → i18n support
:feature:{name}:domain/   → 100% Pure Kotlin (UseCases, Models, Repository Interfaces)
:feature:{name}:data/     → Repository implementations, Sources, Room Entity/DAO
:feature:{name}:presentation/ → ViewModels, StateFlow, MVI (shared between Android & iOS)
```

### Source Architecture (3-Layer)

Each feature's data layer has up to **3 Source types**. The suffix is `*Source`, never
`*DataSource`:

```
feature:{name}:data/
├── datasource/
│   ├── {Name}RemoteSource    → Ktorfit REST interface (KSP generated impl)
│   ├── {Name}LocalSource     → @Dao (Room DAO) - structured database operations
│   └── {Name}ConfigSource    → @ConfigSource (KSP generated) - DataStore + RemoteConfig
├── local/
│   └── entity/
│       └── {Name}Entity.kt      → @Entity (Room entity)
└── repository/
    └── {Name}RepositoryImpl.kt   → orchestrates all 3 Sources
```

### Room Schema Ownership

Each feature module owns its Room schema (Entity + DAO). `AppDatabase` lives in `:shared` and
aggregates all feature DAOs:

```kotlin
// shared/.../database/AppDatabase.kt
@Database(entities = [AuthSessionEntity::class, /* future entities */], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authLocalSource(): AuthLocalSource
    // future DAOs added here
}
```

**Adding a new feature's database schema:**

1. Create Entity in `feature/{name}/data/local/entity/`
2. Create Room `@Dao` interface as `{Name}LocalSource` in `feature/{name}/data/datasource/`
3. Add `abstract fun` to `shared/.../database/AppDatabase.kt`
4. Add `single { get<AppDatabase>().{name}LocalSource() }` to `shared/.../di/KoinInitializer.kt`
5. Add `implementation(libs.localdb.room.runtime)` to feature's `build.gradle.kts`

### Dependency Rules

**CRITICAL:** Features use Clean Architecture with strict boundaries:
- **Domain layer**: Depends on `:core:domain` and `:core:resulting`. No other dependencies.
- **Data layer**: Depends on its own `domain` plus **`:core:data`, and that is all it needs** —
  `:core:data` re-exposes `:core:domain`, `:core:resulting`, `:core:remote`, `:core:config` and
  `:core:local` as `api`, so no feature re-declares them. Add a dependency here only when it is
  specific to that feature (Ktorfit, a mapping compiler, a particular SDK).
- **Presentation layer**: Depends on its own `domain` plus **`:core:presentation`** — which
  re-exposes `:core:domain`, `:core:common`, `:core:navigation`, `:core:resource`, `:core:design`
  (Android), `lifecycle-viewmodel` and the Koin ViewModel/Compose artifacts as `api`, so no feature
  re-declares them. `:core:navigation` brings the Navigation 3 runtime on Android. Add a dependency
  here only when it is specific to that feature (e.g. Credential Manager / Google Identity, which
  only `feature:auth:presentation` needs). **Never depends on `data`.**

**Core Module Dependencies:**
- **core:remote** → `:core:resulting` (for RemoteError). Owns the single `Json` instance.
- **core:config** → `:core:resulting`
- **core:data** → `api` on `:core:domain`, `:core:resulting`, `:core:remote`, `:core:config`,
  `:core:local`. Deliberately `api`, not `implementation`: this is the single place the
  "what every feature data module needs" rule is written down. Its only consumers are
  `feature:{name}:data` modules, so nothing leaks outside the data layer.
- **core:presentation** → `api` on `:core:domain`, `:core:common`, `:core:navigation`,
  `:core:resource`, `:core:design` (androidMain). Same rationale as `core:data`: the
  "what every feature presentation module needs" rule lives here, once.
- **core:navigation** → `api` on the Navigation 3 runtime (androidMain only).
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
6. **Repository** (data layer) uses `AuthRemoteSource` (Firebase or Ktor implementation)

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

### Serialization

There is **no `core:serialization` module**. It was removed: the `SerializationApi` /
`KotlinxSerializationApi` abstraction it was supposed to hold never existed in code, and the one
thing it really provided — a configured `kotlinx.serialization.json.Json` — is now a `@Single` in
`CoreRemoteModule`, next to the ContentNegotiation and Ktorfit code that consumes it.

Use `kotlinx.serialization` directly. Do not reintroduce a wrapper interface around it.

### Exception Mapping Flow

```
Ktor Exception (ClientRequestException, TimeoutException, etc.)
  ↓ [core:remote maps to]
RemoteError (RemoteError.Timeout, RemoteError.ClientError(401), etc.)
  ↓ [both errors flow to feature:data]

RemoteError
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
    val dto = remoteSource.signInWithGoogle(idToken)
    emit(dto.toDomain())
}
.retryWhen { cause, attempt ->
    cause is RemoteError.NoConnection && attempt < 3
}
.catch { exception ->
    throw exception.toAuthError() // RemoteError.ClientError(401) → AuthError.InvalidCredentials
}
```

## Remote Source Code Generation (Ktorfit)

REST Sources are defined as plain Kotlin interfaces annotated with
[Ktorfit](https://github.com/Foso/Ktorfit) HTTP annotations. Ktorfit's KSP processor generates the
implementation and a `Ktorfit.create{InterfaceName}()` extension function.

The project previously used a hand-rolled `@RemoteDataSource` KSP processor in `core:processor`.
That system has been removed in favour of Ktorfit, which is actively maintained, supports every KMP
target, and gives real compile-time checking of paths and parameters.

### Concrete Clients (core:remote)

- **`HttpClient`** (`rest/provideHttpClient.kt`): the single Ktor client — Supabase default headers,
  `ContentNegotiation(json)`, logging, and `RemoteError` mapping via `HttpResponseValidator`
- **`Ktorfit`** (`rest/provideKtorfit.kt`): wraps that same `HttpClient`, so every generated REST
  call inherits the identical header/serialization/error pipeline
- **`FirebaseFirestoreClient`** (`core:remote/firestore/`): Firebase Firestore for document CRUD and
  realtime observation

### Annotations (de.jensklingenberg.ktorfit.http)

**HTTP methods:** `@GET`, `@POST`, `@PUT`, `@PATCH`, `@DELETE`, `@HEAD`, `@OPTIONS`, `@HTTP`

**Parameters:** `@Body`, `@Query`, `@QueryMap`, `@QueryName`, `@Path`, `@Header`, `@HeaderMap`,
`@Headers`, `@Url`, `@Field`, `@FieldMap`, `@Part`, `@PartMap`, `@Tag`, `@ReqBuilder`

**Modifiers:** `@FormUrlEncoded`, `@Multipart`, `@Streaming`

### Base URL Rule

`provideKtorfit` sets `baseUrl("https://$supabaseHost/")`. Ktorfit requires the base URL to end with
`/`, so **interface paths must NOT start with a leading slash** (`"auth/v1/token"`, not
`"/auth/v1/token"`).

### Usage Example

```kotlin
// feature/auth/data/datasource/AuthRemoteSource.kt
interface AuthRemoteSource {

    @POST("auth/v1/token")
    suspend fun signInWithIdToken(
        @Query("grant_type") grantType: String,
        @Body body: GoogleSignInRemoteModel
    ): AuthSessionRemoteModel

    @GET("rest/v1/profiles/{id}")
    suspend fun getProfile(@Path("id") id: String): UserProfileRemoteModel

    @POST("auth/v1/logout")
    suspend fun logout()
}
```

### Koin Wiring

Ktorfit generates `Ktorfit.createAuthRemoteSource()` into
`build/generated/ksp/metadata/commonMain/kotlin/`. Bind it in the feature's `@Module`:

```kotlin
@Module(includes = [AuthDomainModule::class])
class AuthDataModule {

    @Factory
    fun provideAuthRemoteSource(
        ktorfit: Ktorfit
    ): AuthRemoteSource = ktorfit.createAuthRemoteSource()
}
```

### Gradle Setup

The official Ktorfit Gradle plugin **is** applied, with its compiler plugin switched off:

```kotlin
plugins {
    alias(libs.plugins.ktorfit)
}

ktorfit {
    compilerPluginVersion.set("-")   // "-" disables the Ktorfit compiler plugin entirely
}

dependencies {
    commonMainImplementation(libs.network.ktorfit.libLight)
    commonMainImplementation(libs.network.ktorfit.annotations)

    // The plugin hardcodes ktorfit-ksp 2.7.3; declare the catalog version so Gradle's
    // newest-wins resolution keeps the processor on the same version as the runtime.
    add("kspCommonMainMetadata", libs.network.ktorfit.ksp)
}
```

The Gradle plugin registers `ktorfit-ksp` on `kspCommonMainMetadata` and on the per-target `ksp*`
configurations, passes the `Ktorfit_*` KSP options, and orders compile tasks after
`kspCommonMainKotlinMetadata`. Running the processor on the per-target configurations is harmless:
it deliberately skips generating for interfaces declared in `commonMain`.

Two things to know about this setup:

- **The compiler plugin is off on purpose.** Its only job is rewriting the reified
  `ktorfit.create<T>()` call, and that function is `@Deprecated` in Ktorfit 2.7.5 ("the plan is to
  get rid of the plugin"). With `kotlin.compiler.allWarningsAsErrors=true` it could not be called
  here regardless. Keeping it off also means no Kotlin-version-coupled compiler artifact is loaded
  into the build. **Always use the generated `ktorfit.createMySource()` extension**; the
  reified `ktorfit.create<MySource>()` form is not available.
- **The plugin's srcDir must not be double-registered.** It adds
  `<buildDir>/generated/ksp/metadata/commonMain/kotlin` to `commonMain`, and `DiConventionPlugin`
  adds the same directory. Gradle collapses srcDirs only when they resolve to the identical `File`,
  so `DiConventionPlugin` must name that exact path and not an ancestor such as
  `build/generated/ksp/metadata`.

`ktorfit-lib-light` is used instead of `ktorfit-lib` because the project supplies its own Ktor
engines (OkHttp on Android, Darwin on iOS); the light artifact brings only `ktor-client-core`.
Ktorfit must stay on a release built against the project's Ktor major.minor (2.7.4+ -> Ktor 3.5.x).

### WebSocket and Firestore Sources

There is currently **no annotation/codegen system for WebSocket or Firestore Sources**. The
previous annotations (`@Subscribe`, `@Send`, `@GetDocument`, `@ObserveCollection`, …) were removed
because nothing in the codebase used them. Until a pattern is settled, write realtime and Firestore
Sources by hand as thin facades over `HttpClient` (WebSockets plugin) or
`FirebaseFirestoreClient`, keeping the same interface + `@Single`/`@Factory` Koin binding shape as
the generated REST ones.

## Config Source Code Generation (KSP Annotations)

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
@ConfigSource(name = "auth")
interface AuthConfigSource {

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
class AuthConfigSourceImpl(
    @Named("auth") private val dataStore: DataStore<Preferences>
) : AuthConfigSource {
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

**Mixed Config Source (DataStore + RemoteConfig):**

```kotlin
@ConfigSource(name = "product")
interface ProductConfigSource {
    @SaveLocalConfig(key = "last_category")
    suspend fun saveLastCategory(category: String)

    @RetrieveRemoteConfig("new_feature_enabled")
    suspend fun isNewFeatureEnabled(): Boolean

    @ObserveRemoteConfig("promo_banner_text")
    fun observePromoBanner(): Flow<String>
}

// Generated with both clients (FirebaseRemoteConfig injected directly):
@Single
class ProductConfigSourceImpl(
    @Named("product") private val dataStore: DataStore<Preferences>,
    private val remoteConfig: FirebaseRemoteConfig
) : ProductConfigSource { ... }
```

## Object Mapping (KMapper library)

Object mapping is **not** an in-repo module. The project consumes **KMapper 2.2.2**
(`io.github.sahsenvar`), an external KSP library: compile-time, type-safe, null-safe mappers
between Remote / Domain / UI models.

Full reference: **https://kmapper.gitbook.io/docs** — do not duplicate it here.

### Setup

Version catalog entries: `mapping-kmapper-core`, `mapping-kmapper-annotations`,
`mapping-kmapper-compiler` (`kmapper = "2.2.2"`).

```kotlin
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    commonMainImplementation(libs.mapping.kmapper.core)
    commonMainImplementation(libs.mapping.kmapper.annotations)
    add("kspCommonMainMetadata", libs.mapping.kmapper.compiler)
}
```

Only modules that **declare** mappings need the compiler; modules that merely call generated
functions need just the runtime. Apply `alias(libs.plugins.ksp)` in the module that registers
the compiler — no convention plugin applies KSP for you. Once KSP is applied, the
`domatapp.kmp.di` convention plugin wires
`kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")` and the
`dependsOn("kspCommonMainKotlinMetadata")` ordering, so generated mappers are visible to all
targets.

### Declaring a mapping

Annotations live in `com.sahsenvar.kmapper.annotations`. Put `@MapTo` on the **wire/source**
model; every nested pair needs its own `@MapTo`.

```kotlin
@Serializable
@MapTo(AuthSessionDomainModel::class)
data class AuthSessionRemoteModel(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String?,
    val user: AuthUserRemoteModel        // nested: AuthUserRemoteModel also has @MapTo
) : RemoteModel
```

Fields match by name; extra source fields are ignored. Absence follows the type — a null source
value fills a nullable target with `null`, a defaulted target with its constructor default, and
otherwise fails with `RequiredFieldMissing`.

### Calling generated mappers — project convention

Generated functions are named `to<Target>Result()` and return **`kotlin.Result<T>`**. They are
generated into the **receiver's** package.

DomatApp's error architecture is exception-based (see *Error Handling Architecture*), so call
sites unwrap immediately with `.getOrThrow()` and let the existing `.catch { throw it.toAuthError() }`
chain handle it:

```kotlin
emit(response.toAuthSessionDomainModelResult().getOrThrow())
```

Do **not** propagate `Result<T>` up through repository or domain layers. Migrating the project to
a `Result`-based error architecture is a separate, future decision.

### Annotation quick table

| Need | Write |
|------|-------|
| different field names | `@FieldMap("targetName")` on the **source** field |
| exclude a field | `@IgnoreMap` |
| default must not mask missing wire data | `@IgnoreDefaultValue` on the target field |
| custom conversion for one field | `@ConvertWith(use = MyConverter::class)` |
| field too important to absorb a bad value | `@ConvertWith(onFail = OnFail.Throw)` |
| drop broken list elements | `@ConvertWith(onFail = OnFail.Skip)` (collections only) |
| invariant on a value | `@Validate(NotBlankValidator::class)` |
| register converters module-wide | `@KMapperConfig(converters = [...])` on any object, once per module |

Gotchas: `@FieldMap` / `@ConvertWith` are read from the **source field of the generated
direction**. Lossy conversions (e.g. `Long -> Int`) are compile errors by design — write an
explicit converter if your domain guarantees safety. kotlinx-datetime and `kotlin.time.Duration`
converters are core built-ins; other types have optional `kmapper-converters-*` add-ons.

## Local Source (Room DAO)

Local data sources are **Room `@Dao` interfaces directly** - no KSP code generation needed. Room's
own KSP processor generates the implementations.

```kotlin
// feature/auth/data/datasource/AuthLocalSource.kt
@Dao
interface AuthLocalSource {
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
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    add("kspAndroid", projects.core.processor)
}
```

Use `kspAndroid` because `@NavigationScreen` and `@NavigationEffectHandler` live in `androidMain`,
while `@NavigationViewModel` is in `commonMain` but visible during Android compilation.

## Backend Strategy (Concrete Clients + Source Pattern)

### Concrete Client Architecture

The project provides concrete client classes across two modules:

**core:remote** (Network):

- **`HttpClient`** (Ktor): the single, fully configured Ktor client (headers, ContentNegotiation,
  logging, `RemoteError` mapping) — see `provideHttpClient`
- **`Ktorfit`**: built on top of that `HttpClient` by `provideKtorfit` — used to create REST
  Source implementations
- **`FirebaseFirestoreClient`** (`core:remote/firestore/`): Firebase Firestore CRUD and realtime

**core:config** (Configuration):

- **`DataStore<Preferences>`**: Local key-value storage (platform-specific factory)
- **`FirebaseRemoteConfig`** (GitLive): Firebase Remote Config, injected directly via Koin

### Source Pattern (Feature Layer)

Each feature defines its own Source interfaces:

- **`AuthRemoteSource`** → Ktorfit interface (KSP-generated `createAuthRemoteSource()`)
- **`AuthLocalSource`** → Room `@Dao` (Room-generated impl)
- **`AuthConfigSource`** → `@ConfigSource` (KSP-generated impl)

Repository implementations orchestrate between these 3 Sources. Never use concrete clients
directly in repositories.

## Version Catalog

All dependencies are managed in `gradle/libs.versions.toml`:
- Uses single centralized file (no splitting)
- Prefixes for organization: `androidx-*`, `compose-*`, `kotlinx-*`, `firebase-*`
- Plugin dependencies for `build-logic` are also defined here

## Testing

Test source sets are currently disabled across core and feature modules. Do not automatically add test dependencies or generate test files unless explicitly requested.

## Dependency Injection (Koin Annotations)

The project uses **Koin Annotations processed by the Koin Kotlin compiler plugin** (Koin 4.2+),
NOT KSP and NOT the Koin DSL.

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
- **@KoinViewModel**: ViewModels. Import it from `org.koin.core.annotation`, **not**
  `org.koin.android.annotation` — it moved packages in Koin Annotations 4.2.

### Build Setup

**Do not wire DI by hand.** `domatapp.kmp.di` (`DiConventionPlugin`) applies the Koin compiler
plugin (`io.insert-koin.compiler.plugin`) and adds `koin-core` / `koin-annotations` to `commonMain`
and `koin-android` to `androidMain` for every module that uses it:

```kotlin
plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
}
```

Because the compiler plugin is a `KotlinCompilerPluginSupportPlugin`, one `pluginManager.apply` in
the convention plugin covers every compilation — commonMain metadata plus each Android/iOS target.
There are no generated source files to register and nothing to order build tasks around.

Modules outside the convention plugin that consume Koin (`:shared`) apply
`alias(libs.plugins.koinCompiler)` directly.

### Loading modules — one accessor per Gradle module

The compiler plugin generates the `module()` accessor **only inside the compilation that declares
the `@Module` class**. `CoreRemoteModule().module()` therefore does not resolve from `:shared`.
Every module that declares a `@Module` exposes its own accessor next to it:

```kotlin
@Module
@ComponentScan("com.domatapp.core.remote")
class CoreRemoteModule

fun coreRemoteModule(): KoinModule = CoreRemoteModule().module()
```

`shared/.../di/KoinInitializer.kt` then calls those functions. Import
`org.koin.core.module.Module as KoinModule` — the unaliased name collides with the `@Module`
annotation.

Two things that differ from Koin's published migration guide, both verified by compiling and running
against Koin 4.2.2 + compiler plugin 1.2.1:

- `module` is a generated **function**, not a property: write `MyModule().module()`.
- There is no `import org.koin.ksp.generated.module` any more — that package is gone, and the import
  is an unresolved reference.
- `startKoin<MyApp>()` with `@KoinApplication` does not exist in koin-core 4.2.2, and
  `@Configuration` did not auto-register modules. Load modules explicitly via the accessors.

`@Module(includes = [OtherModule::class])` **does** work across Gradle modules — only the `module()`
accessor is compilation-local.

### koinCompiler settings used here

| Setting | Value | Why |
|---|---|---|
| `logSeverity` | `"info"` | `gradle.properties` sets `kotlin.compiler.allWarningsAsErrors=true`; the plugin's informational output defaults to WARNING severity and would fail the build. |
| `versionCheckSeverity` | `"info"` | Same reason, for the "unverified Kotlin version" notice. |
| `compileSafety` | `false` in `DiConventionPlugin`, `true` in `:shared` | Per-module graph validation reports dependencies a single Gradle module cannot see, because modules compose via `@Module(includes = [...])`. `:shared` owns `startKoin` and sees the whole graph, so full validation runs there. This replaces the old `KOIN_CONFIG_CHECK` KSP argument. |

Because `:shared` contains `startKoin`, the plugin auto-enables `strictSafety` on it, which makes
`:shared`'s Kotlin compile task always re-run. That is deliberate on Koin's side — DSL lambda bodies
are not part of any declaration's ABI, so incremental compilation would otherwise skip
re-validation. Other modules stay fully incremental.

### KSP is still used — just not for DI

`core:processor` (mapping, config and navigation code generation) and `ktorfit-ksp` still run under
KSP. Only modules that actually register one of those processors apply `alias(libs.plugins.ksp)`.

## Key Technologies

- **KMP**: Kotlin 2.4.10 (capped by SKIE 0.10.14), Compose Multiplatform 1.12.0
- **Android**: minSdk 30, targetSdk 37, compileSdk 37, AGP 9.4.0, Gradle 9.7.1
- **Codegen**: KSP 2.3.11 (`core:processor` only — DI no longer uses it)
- **UI**: Jetpack Compose (Android), SwiftUI (iOS)
- **Architecture**: Coroutines + Flow (Arrow-kt is in the catalog but unused)
- **DI**: Koin 4.2.2 with Annotations 4.2.2 (Kotlin compiler plugin, `io.insert-koin.compiler.plugin` 1.2.1)
- **Networking**: Ktor Client 3.5.2 (REST + WebSocket), Ktorfit 2.7.5 (REST codegen via KSP)
- **Database**: Room 2.8.5 (KMP)
- **Storage**: DataStore 1.2.1 (Preferences)
- **Backend**: Firebase Auth (GitLive 2.7.0), Firebase Firestore, Firebase RemoteConfig
- **Serialization**: kotlinx.serialization
- **Error Handling**: Exception-based with core:resulting module
- **Object Mapping**: KMapper 2.2.2 (`io.github.sahsenvar`) - external KSP compile-time mapper
