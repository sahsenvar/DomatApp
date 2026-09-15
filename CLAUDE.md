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
  :core:remote/           → Network layer (Ktor REST via KtorfitX) + the shared Json
  :core:config/           → Preferences platform bridge (`preferencesContext()`) + DataStore deps
  :core:navigation/       → Gezgin navigation graph (@NavGraph routes + declared edges)
  :core:resource/         → Shared resources
  :core:localization/     → i18n support
  :core:analytics/        → Provider-agnostic event tracking facade. Deliberately empty - no
                             provider SDK chosen yet. Do not add a dependency here speculatively;
                             ask which provider before writing anything into this module.
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
│   ├── {Name}RemoteSource    → KtorfitX @Api interface (KSP generated impl)
│   ├── {Name}LocalSource     → @Dao (Room DAO) - structured database operations
│   └── {Name}ConfigSource    → @Preferences (KspPreferences KSP generated) - DataStore
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
  specific to that feature (KtorfitX, a mapping compiler, a particular SDK).
- **Presentation layer**: Depends on its own `domain` plus **`:core:presentation`** — which
  re-exposes `:core:domain`, `:core:common`, `:core:navigation`, `:core:resource`, `:core:design`
  (Android), `lifecycle-viewmodel` and the Koin ViewModel/Compose artifacts as `api`, so no feature
  re-declares them. `:core:navigation` brings Gezgin, and with it the Navigation 3 runtime, on
  Android. Add a dependency
  here only when it is specific to that feature (e.g. Credential Manager / Google Identity, which
  only `feature:auth:presentation` needs). **Never depends on `data`.**

**Core Module Dependencies:**
- **core:remote** → `:core:resulting` (for RemoteError). Owns the single `Json` instance.
- **core:config** → `api` on the KspPreferences annotations/runtime and the two DataStore
  artifacts. No sources of its own beyond the `preferencesContext()` `expect`/`actual` pair.
- **core:data** → `api` on `:core:domain`, `:core:resulting`, `:core:remote`, `:core:config`,
  `:core:local`. Deliberately `api`, not `implementation`: this is the single place the
  "what every feature data module needs" rule is written down. Its only consumers are
  `feature:{name}:data` modules, so nothing leaks outside the data layer.
- **core:presentation** → `api` on `:core:domain`, `:core:common`, `:core:navigation`,
  `:core:resource`, `:core:design` (androidMain). Same rationale as `core:data`: the
  "what every feature presentation module needs" rule lives here, once.
- **core:navigation** → `api` on `gezgin-core` (androidMain only; `gezgin-core` has no iOS klib,
  and it is what pulls in the Navigation 3 runtime).
- **core:resulting** → No dependencies (base module for error handling)
- **core:analytics** → No dependencies. Empty scaffold, not yet consumed by anything.

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
`CoreRemoteModule`, next to the ContentNegotiation and KtorfitX code that consumes it.

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

## Remote Source Code Generation (KtorfitX)

REST Sources are plain Kotlin interfaces annotated with
[KtorfitX](https://github.com/annotation-engine/ktorfitx) (`cn.ktorfitx.multiplatform.annotation.*`).
Its KSP processor generates `impls.{Name}Impl` plus a `Ktorfitx.{name}` **extension property** to
obtain it.

```kotlin
@Api
interface AuthRemoteSource {

    @POST("auth/v1/token")
    suspend fun signInWithIdToken(
        @Query("grant_type") grantType: String,
        @Body body: GoogleSignInRemoteModel
    ): AuthSessionRemoteModel
}
```

```kotlin
// AuthDataModule
@Factory
fun provideAuthRemoteSource(ktorfitx: Ktorfitx): AuthRemoteSource = ktorfitx.authRemoteSource
//                                                                           ^ generated extension
//   import com.domatapp.feature.auth.data.datasource.impls.authRemoteSource
```

### Why KtorfitX and not Ktorfit

Ktorfit has no WebSocket annotations, so WebSocket Sources had to be hand-written. KtorfitX covers
REST **and** WebSocket in one annotation family (`@Api` / `@GET` / `@POST` / `@WebSocket`), which was
the original architectural goal. The trade-off is accepted deliberately: KtorfitX is a much smaller,
younger project than Ktorfit, its documentation is primarily Chinese and marked "under construction".

### Gradle setup

```kotlin
plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfitx)   // cn.ktorfitx.multiplatform
}

ktorfitx {
    websockets { enabled = true }
}
```

The plugin adds `multiplatform-annotation` + `multiplatform-core` (and `multiplatform-websockets`
when enabled) to `commonMain`, registers `multiplatform-ksp` on every `ksp*` configuration, and
orders the per-target KSP tasks after `kspCommonMainKotlinMetadata`. It does all of this inside
`afterEvaluate`, so — unlike Ktorfit's plugin — **the order of the `plugins {}` block does not
matter**. Do not declare the KtorfitX runtime artifacts yourself in a module that applies the
plugin; only `:core:remote`, which builds the `Ktorfitx` instance without applying the plugin,
declares `multiplatform-core` directly.

### Two hard constraints

- **Ktor version is pinned exactly.** The plugin calls
  `checkDependency("io.ktor", "ktor-client-core")` and **fails the build** unless the declared
  version equals the Ktor version KtorfitX was built against — 3.4.2 for KtorfitX 3.4.2-3.3.3. The
  version string is literally `<ktor>-<ktorfitx>`. Bumping Ktor requires a matching KtorfitX release.
- **The checked dependencies must be declared in the module itself**, not inherited transitively.
  `feature:auth:data` therefore declares `ktor-client-core` and (because websockets are enabled)
  `ktor-client-websockets` even though `:core:data` already exposes Ktor transitively.

### The HttpClient is built by KtorfitX, not handed to it

Ktorfit wrapped an existing `HttpClient`. **KtorfitX builds its own** — `KtorfitxConfig` has no way
to accept a pre-built client, and its only configurable overload,
`httpClient(engineFactory) { }`, requires naming the engine explicitly rather than letting Ktor's
service loader pick one. So:

- `core:remote` declares `internal expect fun KtorfitxConfig.platformHttpClient(...)`, with
  `actual`s selecting OkHttp on Android and Darwin on iOS.
- The whole pipeline (Supabase headers, ContentNegotiation, logging, the `RemoteError` mapping)
  lives inside that block in `provideKtorfitx`.
- The app-wide `HttpClient` single is `ktorfitx.config.httpClient`, so direct Ktor callers share the
  exact same client as the generated `@Api` implementations.

### WebSocket and Firestore Sources

There is currently **no annotation/codegen system for WebSocket or Firestore Sources, and no
concrete Firestore client class either** — `core:remote` has no `firestore/` directory. The
previous annotations (`@Subscribe`, `@Send`, `@GetDocument`, `@ObserveCollection`, …) were removed
because nothing in the codebase used them. Until a pattern is settled, write realtime and Firestore
Sources by hand as thin facades over `HttpClient` (WebSockets plugin) or the GitLive
`dev.gitlive.firebase.firestore.FirebaseFirestore` client directly (kept via the
`backend-firebase-firestore` catalog alias even at zero current usage), keeping the same interface +
`@Single`/`@Factory` Koin binding shape as the generated REST ones.

## Config Source Code Generation (KspPreferences library)

Local preference storage is **not** an in-repo annotation system. The project consumes
**KspPreferences 2.0.0** (`io.github.semenciuccosmin`), an external KSP library that generates a
DataStore-backed implementation for every `@Preferences`-annotated interface.

Full reference: **https://github.com/SemenciucCosmin/KspPreferences** — do not duplicate it here.

There is **no RemoteConfig support**. The `@RetrieveRemoteConfig` / `@ObserveRemoteConfig`
annotations, the `FirebaseRemoteConfigClient` and the `dev.gitlive:firebase-config` dependency were
removed: nothing in the codebase ever used them. Feature flags are an open design question, not an
existing capability — do not reintroduce a wrapper for them speculatively.

### What `core:config` still owns

Only the platform handle KspPreferences needs, plus the dependency declarations feature modules
inherit:

- `expect fun preferencesContext(): Any?` (`com.domatapp.core.config.preferences`) — returns the
  Android application `Context`, and `null` on iOS, where KspPreferences ignores the argument and
  derives the path from `NSDocumentDirectory`.
- `PreferencesContextHolder.context` (androidMain) — assigned in `DomatApplication.onCreate()`
  **before** `initKoin()`.
- `api` on `persistence-kspPreferences-annotations` and the two DataStore artifacts, so feature
  data modules (and the code generated into them) get them through `:core:data`.

`core:config` declares **no Koin module**. Each `@Preferences` Source is provided by its own
feature module.

### Annotations

Class-level (both required):

- `@Preferences(name)` — the DataStore file name
- `@ConstructedBy(XConstructor::class)` — points at the `expect object` KSP fills in

Accessor annotations, one per function: `@Get` (suspending point-in-time read, returns `T`),
`@GetFlow` (non-suspending reactive read, returns `Flow<T>`), `@Set` (suspending write, returns
`Unit`, exactly one parameter), `@Clear` (no parameters, returns `Unit` — clears the **entire**
store, not one key, and takes no value-type annotation). `@Get`/`@GetFlow`/`@Set` additionally need
a value-type annotation on the same function: `@StringPreference(key, defaultValue)` and the
`Boolean` / `Int` / `Long` / `Float` / `Double` / `Object` equivalents.

### Usage Example

```kotlin
private const val PREFERENCES_NAME = "auth"
private const val KEY_ACCESS_TOKEN = "access_token"

@Preferences(name = PREFERENCES_NAME)
@ConstructedBy(AuthConfigSourceConstructor::class)
interface AuthConfigSource {

    @Set
    @StringPreference(key = KEY_ACCESS_TOKEN, defaultValue = DEFAULT_ACCESS_TOKEN)
    suspend fun saveToken(value: String)

    @Get
    @StringPreference(key = KEY_ACCESS_TOKEN, defaultValue = DEFAULT_ACCESS_TOKEN)
    suspend fun retrieveToken(): String

    @GetFlow
    @StringPreference(key = KEY_ACCESS_TOKEN, defaultValue = DEFAULT_ACCESS_TOKEN)
    fun observeToken(): Flow<String>

    @Clear
    suspend fun clearAll()

    companion object {
        const val DEFAULT_ACCESS_TOKEN = ""
    }
}
```

```kotlin
// Declared by hand in commonMain; KSP emits the `actual object` per target.
expect object AuthConfigSourceConstructor : PreferencesConstructor<AuthConfigSource>
```

```kotlin
// AuthDataModule — @Single, never @Factory: a second instance would open a second DataStore
// over the same file.
@Single
fun provideAuthConfigSource(): AuthConfigSource =
    PreferencesFactory.create(AuthConfigSourceConstructor, preferencesContext())
```

### Gradle setup

```kotlin
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    // Runtime + annotations arrive through :core:data -> :core:config. Only the compiler
    // registration is per-module, and it is per *target*, not kspCommonMainMetadata.
    add("kspAndroid", libs.persistence.kspPreferences.compiler)
    add("kspIosArm64", libs.persistence.kspPreferences.compiler)
    add("kspIosSimulatorArm64", libs.persistence.kspPreferences.compiler)
}
```

### Four things that will bite you

- **Register the compiler per target, never on `kspCommonMainMetadata`.** The processor emits an
  `actual object`, and an `actual` cannot be generated into the common metadata compilation. The
  library's own sample does the same.
- **Name every `@Set` parameter `value`.** The generated override hard-codes that name, and a
  renamed override parameter is a Kotlin warning — which this build turns into an error via
  `kotlin.compiler.allWarningsAsErrors=true`.
- **Primitive preferences are non-nullable.** `@Get` generates `?: defaultValue`, so "absent" is the
  declared default, not `null`. Declaring `String?` compiles (the override narrows) but the value
  will never actually be null — model absence with an explicit default constant instead.
- **`@Clear` is all-or-nothing.** There is no single-key clear. If a store holds several keys and
  you need to drop one, write `set(defaultValue)` and treat the default as absence.

Also note: `PreferencesFactory.create<T>()` — the reflection-based overload — is Android/JVM only;
its iOS `actual` throws at runtime. Always use `@ConstructedBy` plus the
`create(constructor, context)` overload.

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

## Navigation (Gezgin library)

Navigation is **not** in-repo codegen. The `core:navigation` annotations
(`@NavigationScreen` / `@NavigationViewModel` / `@NavigationEffectHandler` / `@TopBar` /
`@BottomBar`), the `NavigationProcessor` in `core:processor`, the hand-rolled global `Navigator`
interface, `MainViewModel`'s back stack and `LocalNavigator` were all removed and replaced by
**[Gezgin](https://github.com/sahsenvar/Gezgin)** (`io.github.sahsenvar`, artifacts `gezgin-core`
and `gezgin-processor`), an annotation + KSP navigation layer on top of AndroidX Navigation 3.
`:core:processor` no longer exists — navigation was its last remaining processor.

The library README is the reference; do not duplicate it here.

### The one idea

The graph is a `sealed interface` tree and **edges are declared per route**. KSP generates a typed
`<X>Navigator` exposing *only* that route's declared edges, so navigating somewhere undeclared is a
compile error rather than a runtime "route not found".

```kotlin
// core/navigation/src/androidMain/.../DomatGraph.kt
@NavGraph
sealed interface AuthGraph : Route {

    @GoTo(LocationSelectionRoute::class)
    @ReplaceTo(
        target = MainGraph.HomeRoute::class,
        clearUpTo = OnboardingGraph.OnboardingWelcomeRoute::class,
        inclusive = true,
    )
    data object LoginRoute : AuthGraph
}
```

`LoginNavigator` therefore has exactly `goToLocationSelection()`, `replaceToHome()` and the
implicit `back()`. `@ReplaceTo` with `clearUpTo` = the start destination is how the old
`Navigator.replaceAll(...)` is expressed.

### Annotation quick table

| Need | Write (on the **route**, in `:core:navigation`) |
|------|--------------------------------------------------|
| group routes | `@NavGraph` on the sealed interface |
| a sub-flow torn down as one unit | `@FlowGraph` (+ `ResultFlow<T>` for a typed result) |
| push | `@GoTo(Target::class)` → `goToTarget()` |
| irreversible transition | `@ReplaceTo(Target::class, clearUpTo = X::class, inclusive = true)` → `replaceToTarget()` |
| pop to a specific route | `@BackTo(Target::class)` → `backToTarget()` |
| pop to the graph's start | `@BackToStart` → `backToStart()` |
| ask a sub-flow for a value | `@GoForResult(Target::class)` → `launchTarget()` + `targetResults` |
| swallow back | `@NoBack` (root is exempt) |
| single-step pop | nothing — generated for every non-`@NoBack` route |

| Need | Write (in the **feature presentation** module, `androidMain`) |
|------|---------------------------------------------------------------|
| the UI | `@Screen(Route::class)` (also `@Dialog` / `@BottomSheet` / `@FullscreenModal`) |
| the ViewModel | `@ViewModelOf(Route::class)` provider (project-defined, see below) |
| side effects | `@Effects(Route::class)` provider (project-defined, see below) |

### `@ScreenWrapper` — the app owns what a screen *is*

Gezgin does not resolve ViewModels, collect state or define a container. **One** `@ScreenWrapper`
composable does, for the whole app — `DomatScreenRoot` in
`core/presentation/src/androidMain/.../screen/DomatScreenRoot.kt`. Its parameters are slots marked
`@FilledBy(Marker::class)`, and each marker is a project-defined annotation meta-annotated
`@ScreenSlot`, declared in a sibling `Annotation.kt` next to the wrapper rather than inline in the
same file — the project convention for any project-defined annotation going forward, not just
these two:

```kotlin
@ScreenSlot @Repeatable annotation class ViewModelOf(val route: KClass<out Route>)
@ScreenSlot @Repeatable annotation class Effects(val route: KClass<out Route>)

@ScreenWrapper
@Composable
fun <S : Any, I : Any, E : Any> DomatScreenRoot(
    @FilledBy(ViewModelOf::class) viewModel: @Composable () -> BaseViewModel<S, I, E>,
    @FilledBy(Effects::class) onEffect: (E, DomatEffectScope, (I) -> Unit) -> Unit,
    @FilledBy(Screen::class) content: @Composable ColumnScope.(S, (I) -> Unit) -> Unit,
) { /* container, state collection and effect policy live here */ }
```

Per screen, one provider per marker. The processor unifies their signatures against the slots and
generates a `provideXEntry()` that calls the wrapper with them — replacing the `{Name}Route.kt`
files the old processor wrote:

```kotlin
// feature/auth/presentation/src/androidMain/.../screen/login/LoginBindings.kt
@ViewModelOf(AuthGraph.LoginRoute::class)
@Composable
fun loginViewModel(): LoginViewModel = koinViewModel()

@Effects(AuthGraph.LoginRoute::class)                       // NOT @Composable
fun handleLoginEffect(
    effect: LoginEffect,
    scope: DomatEffectScope,
    onIntent: (LoginIntent) -> Unit,
    nav: LoginNavigator,                                    // role, supplied by Gezgin
) = when (effect) {
    LoginEffect.NavigateToLocationSelection -> nav.goToLocationSelection()
    LoginEffect.NavigateToHome -> nav.replaceToHome()
    /* ... */
}
```

`BaseViewModel<S, I, E>` is unchanged — Gezgin knows none of these types.

**Roles vs slot parameters.** A provider's parameters split in two: the exact route type, that
route's navigator and `GezginSheetController` are *roles* Gezgin supplies; everything else must
match the slot's function type exactly, in order. Because `@Effects` providers are plain functions
they cannot read composition locals, which is why `DomatEffectScope` carries the `Context`, a
`CoroutineScope` and `showMessage` (the app snackbar).

### Host wiring

```kotlin
val navigator = rememberNavigator(
    start = OnboardingGraph.OnboardingWelcomeRoute,
    topology = gezginTopology,          // generated into :core:navigation
    json = gezginJson,                  // generated into :core:navigation
    restoreKey = "domat-root",
    onRootBack = { finish() },
)
GezginDisplay(navigator = navigator, transitions = DomatNavTransitions) {
    onboardingGraphEntries()            // feature-owned bundles of provideXEntry() calls
    authGraphEntries()
    mainGraphEntries()
}
```

### Gradle setup

`:core:navigation` (owns the graph):

```kotlin
plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.ksp)
}
dependencies {
    androidMainApi(libs.navigation.gezgin.core)
    kspAndroid(libs.navigation.gezgin.processor)
}
```

Every `feature:{name}:presentation` that declares screens:

```kotlin
plugins { alias(libs.plugins.ksp) }
dependencies { kspAndroid(libs.navigation.gezgin.processor) }
ksp { arg("gezgin.wrapperPackages", "com.domatapp.core.presentation.screen") }
```

### Things that will bite you

- **`gezgin.wrapperPackages` is mandatory in every feature module.** KSP cannot enumerate annotated
  declarations on the classpath, so a module that does not name the wrapper's package simply does
  not see `DomatScreenRoot` — and the entries are generated **unwrapped**, with a KSP *warning*, not
  an error. A screen that silently loses its ViewModel and state collection looks like a crash, not
  a build failure. Same class of failure for `[SW6]`: a `@Screen` whose signature does not match the
  content slot (`ColumnScope.(S, (I) -> Unit)`) falls back to unwrapped. **Grep build output for
  `SW6` when a screen misbehaves.**
- **The graph is `androidMain`-only.** `gezgin-core` publishes `android` and `jvm`; there is no iOS
  klib. Nothing in `commonMain` may reference a route, or the iOS targets stop compiling.
- **The graph module must not apply the Compose compiler plugin.** Gezgin's own codegen avoids
  emitting a `@Composable` there for exactly this reason; a `@Composable` compiled without lowering
  fails at runtime with `NoSuchMethodError`.
- **Routes carry no `@Serializable`.** Gezgin generates and registers their serializers
  (`gezginSerializersModule`, `gezginJson`). Parameter types of routes still need it.
- **`@ReplaceTo`'s `clearUpTo` must be on the stack**, otherwise `NavEvent.ReplaceTargetMissing` is
  emitted and **nothing happens** — a silent no-op. The graph anchors on the start destination,
  which is always the stack bottom, for that reason.
- **The slot unifier is shallow — it decomposes function types and nothing else.** A slot parameter
  of a *generic project type* (`DomatEffectScope<I>`) is compared as an opaque whole against the
  provider's `DomatEffectScope<LoginIntent>` and fails with `[SW8]`. Anything that has to carry a
  type parameter must be a function-typed slot parameter of its own — which is why the intent sink
  is a separate `(I) -> Unit` rather than a field on `DomatEffectScope`.
- **A slot's return type is not unified, so no slot may be left unfilled unless every type parameter
  is bound elsewhere.** `viewModel: @Composable () -> BaseViewModel<S, I, E>` binds *nothing*: only
  the parameter positions participate. `E` is bound solely by `onEffect`, which is why that slot has
  no Kotlin default and every route — including one with no effects — declares an `@Effects`
  provider. Omitting it fails with `[SW7]`, not with a sensible message about effects.
- **Navigator parameters must be written by their exact simple name** (`LoginNavigator`), not
  fully-qualified and not aliased: the type does not exist yet during the KSP round that reads it,
  so the processor matches it by written name (`[SW11]`).
- **`koinViewModel()`, not `koinInject()`**, in a `@ViewModelOf` provider: only the former resolves
  against `LocalViewModelStoreOwner`, which under `GezginDisplay` is the per-entry ViewModelStore.
- **Effects must not be carried on a `replay = 0` SharedFlow.** A covered Navigation 3 entry leaves
  composition entirely, so an effect emitted then is dropped. `BaseViewModel` is `Channel`-backed,
  which holds them.
- **Not in Gezgin (deliberate V2 items):** multiple/independent back stacks (per-tab history) and
  deep-link/URL route dispatch. Do not design around them being available.
- **`@ExperimentalGezginMigrationApi`** gates `BottomSheetDragHandleMode` only, is documented as
  migration-only and may be removed — do not opt into it for new bottom-sheet UX.

## Backend Strategy (Concrete Clients + Source Pattern)

### Concrete Client Architecture

The project provides concrete client classes across two modules:

**core:remote** (Network):

- **`HttpClient`** (Ktor): the single, fully configured Ktor client (headers, ContentNegotiation,
  logging, `RemoteError` mapping) — see `provideHttpClient`
- **`Ktorfitx`**: built by `provideKtorfitx`, which also owns the `HttpClient` — used to create REST
  Source implementations

There is no concrete Firestore client class today — see *WebSocket and Firestore Sources* above.

**core:config** (Configuration):

- **`preferencesContext()`**: the platform handle KspPreferences needs to place the DataStore file.
  There is no hand-written DataStore factory any more and no RemoteConfig client.

### Source Pattern (Feature Layer)

Each feature defines its own Source interfaces:

- **`AuthRemoteSource`** → KtorfitX `@Api` interface (KSP-generated `ktorfitx.authRemoteSource`)
- **`AuthLocalSource`** → Room `@Dao` (Room-generated impl)
- **`AuthConfigSource`** → `@Preferences` (KspPreferences-generated impl)

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

KtorfitX, KMapper, KspPreferences and Gezgin all run under KSP; there is no in-repo processor
module any more (`:core:processor` was deleted with the navigation migration). Only modules that
actually register one of those processors apply `alias(libs.plugins.ksp)`.

## Key Technologies

- **KMP**: Kotlin 2.4.10 (capped by SKIE 0.10.14), Compose Multiplatform 1.12.0
- **Android**: minSdk 30, targetSdk 37, compileSdk 37, AGP 9.4.0, Gradle 9.7.1
- **Codegen**: KSP 2.3.11 (third-party processors only — no in-repo processor, and DI does not
  use KSP)
- **UI**: Jetpack Compose (Android), SwiftUI (iOS)
- **Navigation**: Gezgin 0.3.0 (`io.github.sahsenvar`) over AndroidX Navigation 3, Android-only
- **Architecture**: Coroutines + Flow (Arrow-kt is in the catalog but unused)
- **DI**: Koin 4.2.2 with Annotations 4.2.2 (Kotlin compiler plugin, `io.insert-koin.compiler.plugin` 1.2.1)
- **Networking**: Ktor Client 3.4.2 (pinned by KtorfitX), KtorfitX 3.4.2-3.3.3 (REST + WebSocket codegen via KSP)
- **Database**: Room 2.8.5 (KMP)
- **Storage**: DataStore 1.2.1 (Preferences) via KspPreferences 2.0.0 (`io.github.semenciuccosmin`)
- **Backend**: Firebase Auth (GitLive 2.7.0), Firebase Firestore
- **Serialization**: kotlinx.serialization
- **Error Handling**: Exception-based with core:resulting module
- **Object Mapping**: KMapper 2.2.2 (`io.github.sahsenvar`) - external KSP compile-time mapper
