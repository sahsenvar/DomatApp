# KMP Architect Memory

## iOS App Structure

- iOS app located at `iosApp/iosApp/`
- Uses SwiftUI with iOS 17+ patterns
- Xcode project at `iosApp/iosApp.xcodeproj/`
- SKIE 0.10.10 enabled for Swift interop (sealed class -> enum, Flow -> AsyncSequence)
- Shared.framework is static, exports core:navigation, moko.resources, moko.graphics

## KMP ViewModel Integration (iOS)

- BaseViewModel extends `androidx.lifecycle.ViewModel` (KMP Lifecycle)
- StateFlow collected via SKIE AsyncSequence (`for await in viewModel.state`)
- Effects via Channel-backed Flow, also SKIE AsyncSequence
- KoinHelper class in `shared/src/iosMain/` provides typed accessors for Swift
- Swift cannot call Koin's reified inline functions - must use KoinHelper pattern

## Design System

- Font: Nunito Sans (must be bundled in Xcode project)
- Colors from Moko Resources `colors.xml` - exact hex values in DomatColors.swift
- Material 3 semantic scheme (light/dark) via DomatColorScheme struct + Environment
- Spacing: xxs(2) xs(4) sm(8) md(16) lg(24) xl(32) xxl(48)
- Shapes: small(8) medium(12) large(16) extraLarge(24)

## Navigation

- NavigationRouter manages root flow (auth/onboarding/main) and NavigationPath
- Main flow uses TabView with 4 tabs: Home, Wallet, Notifications, Profile
- Auth flow uses NavigationStack with push navigation
- AppRoute enum maps to KMP Route sealed interface

## Feature ViewModels Available

- AuthViewModel (only one currently implemented)
- Uses MVI pattern: AuthUiState, AuthIntent, AuthEffect

## KSP Mapper Bug: Nested Non-Null Objects

- Mapper processor generates nullable safe calls (`?.`) for nested object fields even when non-null
- Workaround: Use `@MapTo` only for flat models. Manual mappers for nested non-null complex objects.
- See: `feature/auth/data/mapper/AuthSessionMapper.kt`

## DI: Koin compiler plugin, not KSP (see koin-compiler-plugin.md)

- Koin Annotations 4.2.x is processed by `io.insert-koin.compiler.plugin`; `koin-ksp-compiler` is
  gone. `DiConventionPlugin` applies it — modules add nothing but `alias(libs.plugins.domatapp.kmp.di)`.
- Two traps: `module` is a generated **function** and is **compilation-local** — write
  `MyModule().module()`, delete `import org.koin.ksp.generated.module`, and give every Gradle
  module its own `fun xxxModule(): KoinModule` accessor because the aggregator cannot call it.
  Also import `@KoinViewModel` from `org.koin.core.annotation`, not `org.koin.android.annotation`.
- `koinCompiler { logSeverity / versionCheckSeverity }` must be `"info"` here because of
  `allWarningsAsErrors=true`; `compileSafety` off per module, on in `:shared`.

## REST DataSources: Ktorfit (replaced custom @RemoteDataSource KSP)

- The custom `@RemoteDataSource`/`@GET`/`@POST` codegen in `core:remote/annotations` +
  `core/processor/.../remote/` was removed. REST DataSources now use Ktorfit
  (`de.jensklingenberg.ktorfit.http.*`) — plain interfaces, no marker annotation.
- `core:remote` exposes `provideKtorfit(httpClient)` (`@Single`) wrapping the existing `HttpClient`,
  so headers / ContentNegotiation / RemoteError mapping are unchanged.
- DI binding is now uniform: `@Factory fun provide...(ktorfit: Ktorfit) = ktorfit.createXxx()`.
  No more hand-matching a generated constructor signature.
- Ktorfit must track the project's Ktor major.minor: 2.7.3 -> Ktor 3.4.1, 2.7.4+ -> Ktor 3.5.x.
  Check this before bumping either one.
- The Ktorfit **Gradle plugin IS applied** (`alias(libs.plugins.ktorfit)`), but with
  `ktorfit { compilerPluginVersion.set("-") }`, which disables its Kotlin compiler plugin.
  Rationale: the compiler plugin exists only to rewrite reified `ktorfit.create<T>()`, and that
  function is `@Deprecated` in 2.7.5 — unusable here under `allWarningsAsErrors=true`. Disabling it
  removes the Kotlin-version coupling entirely.
- Consequence: only the generated `ktorfit.createXxx()` extension works;
  reified `ktorfit.create<Xxx>()` is unavailable.
- The plugin registers `<buildDir>/generated/ksp/metadata/commonMain/kotlin` as a commonMain
  srcDir. `DiConventionPlugin` must register that **exact** path, not the ancestor
  `build/generated/ksp/metadata` — Gradle only collapses srcDirs resolving to the same `File`, so an
  ancestor path makes every generated file reachable through two roots.
- The 2.7.5 Gradle plugin hardcodes `KTORFIT_KSP_PLUGIN_VERSION = "2.7.3"`, so the module also
  declares `add("kspCommonMainMetadata", libs.network.ktorfit.ksp)` to pull the processor back up to the
  runtime's version via newest-wins resolution.
- Ktorfit baseUrl must end with `/`; interface paths must NOT start with `/`.
- Use `ktorfit-lib-light` (core only) — the project supplies its own engines (OkHttp / Darwin).

## No codegen for WebSocket / Firestore DataSources

- The old system had `@Subscribe`/`@Send`/`@GetDocument`/`@Observe*` annotations with zero usage.
  They were deleted, not replaced. Write such DataSources by hand until a pattern is agreed.

## Room Database Requires At Least One Entity

- `@Database(entities = [])` causes compile error
- When all entities removed, use PlaceholderEntity pattern (see
  `shared/.../database/AppDatabase.kt`)

## HttpClient Header Strategy (Supabase)

- `apikey: {publicKey}` always sent
- `Authorization: Bearer {accessToken}` only when accessToken is non-null
- NEVER use publicKey as Bearer token

## Dependency upgrade ceilings (see dependency-upgrades.md)

Three catalog entries cannot simply be bumped to "latest". Details and sources in
`dependency-upgrades.md`; the short version:

- **Kotlin is capped by SKIE.** `:shared` applies SKIE, which hard-pins to exact Kotlin versions.
  Read the supported list out of `co.touchlab.skie:gradle-plugin:<v>` before bumping Kotlin.
- **KSP 2.3.12+ is a migration, not a bump** (backing-field symbols change
  `getSymbolsWithAnnotation` results; `:core:processor` must opt in).
- **Koin Annotations 4.2.x drops `koin-ksp-compiler`** for a Kotlin compiler plugin. Whole-DI
  migration.

Also: KSP dropped `<kotlin>-<ksp>` version naming at 2.3.0 — `ksp = "2.3.x"` is a standalone KSP
version, not a Kotlin pairing. Google Maven is unreachable from agent sandboxes, so androidx / AGP /
firebase-bom versions must come from release notes, not from resolving coordinates.

## Version catalog conventions (owner-mandated, PR #9 review)

- **Alias syntax: `category-libraryIdentifier-artifact`** → `libs.category.libraryIdentifier.artifact`.
  Origin prefixes (`androidx-`, `kotlinx-`, `ktor-`) are folded away, except `kotlinx` surviving as
  `kx` inside the identifier (`kxDateTime`, `kxSerializationJson`). Multi-word identifiers are
  camelCase inside their segment. A library's principal artifact is normalised to `-core`.
- **Categories in use**: ui, core, concurrency, serialization, collections, datetime, functional,
  di, network, backend, auth, codegen, resource, navigation, **persistence** (key-value: DataStore,
  multiplatform-settings), localdb (Room/SQLite), mapping, buildlogic. The owner explicitly
  preferred `persistence` over `storage` - do not reintroduce `storage-`.
  (`network-supabase-storage` is unrelated: that is Supabase's Storage product.)
- **Coordinates: always `group` + `name` + `version.ref`**, never the packed
  `module = "group:artifact"` form.
- `[versions]` and `[plugins]` keys are NOT covered by this convention - leave them alone unless
  the owner asks.
- Renaming an alias must also update `libs.findLibrary("...")` string lookups (used in
  `CmpLibraryConventionPlugin`) - an accessor-only rewrite silently misses them.
