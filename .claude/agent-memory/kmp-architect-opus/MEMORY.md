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

## Navigation (iOS side)

- NavigationRouter manages root flow (auth/onboarding/main) and NavigationPath
- Main flow uses TabView with 4 tabs: Home, Wallet, Notifications, Profile
- Auth flow uses NavigationStack with push navigation
- `AppRoute` is a **pure Swift enum**; it does not consume any Kotlin type. It used to mirror a
  `Route` sealed interface in `core:navigation`, but that type is gone — the Android graph is now
  Gezgin's, in androidMain. Keeping the two in sync is a manual, by-eye job.

## Navigation: Gezgin (see navigation-gezgin.md)

`io.github.sahsenvar:gezgin-{core,processor}` — the owner's own library, annotation + KSP
navigation over AndroidX Navigation 3. Replaced `core:navigation`'s `Route`/`Navigator`/annotations,
the hand-rolled `Navigator` + `MainViewModel` back stack, `LocalNavigator`, and the **whole
`:core:processor` module** (navigation was its last processor).

The four things that will actually bite, in `navigation-gezgin.md` with the rest:

- **`ksp { arg("gezgin.wrapperPackages", "com.domatapp.core.presentation.screen") }` is mandatory in
  every feature presentation module.** Omitting it does not fail the build — entries are generated
  **unwrapped** with only an `[SW6]` KSP warning, and the screen loses its ViewModel at runtime.
- **`@ReplaceTo`'s `clearUpTo` must be on the stack or the navigation is a silent no-op.**
- **The graph is androidMain-only** (`gezgin-core` has no iOS klib) and must not apply the Compose
  compiler plugin.
- **`@ScreenWrapper` — the headline feature this migration is for — is 0.3.0-only, and 0.3.0 was
  not published to Maven Central as of 2026-09-14** (Central has 0.1.0/0.2.0; no `v0.3.0` tag).
  Gezgin also builds on Kotlin 2.3.21 / KSP 2.3.9 while this repo is on 2.4.10 / 2.3.11.

## Feature ViewModels Available

- AuthViewModel (only one currently implemented)
- Uses MVI pattern: AuthUiState, AuthIntent, AuthEffect

## Object Mapping: KMapper library (replaced in-repo core:mapping)

- The in-repo `core:mapping` module + `core/processor/.../mapping/` KSP processor were removed and
  replaced by the published library **KMapper 2.2.2** (`io.github.sahsenvar`, artifacts
  `kmapper-core` / `kmapper-annotations` / `kmapper-compiler`). Library docs:
  https://kmapper.gitbook.io/docs — don't re-document it in CLAUDE.md.
- Annotation package is **`com.sahsenvar.kmapper.annotations`** (NOT `com.domatapp.*`).
- Generated functions are `to<Target>Result()` returning **`kotlin.Result<T>`**, emitted into the
  **receiver's** package. Project convention: unwrap at the call site with `.getOrThrow()` so the
  existing exception-based error chain (`.catch { throw it.toAuthError() }`) is preserved.
  Do NOT propagate `Result<T>` into repository/domain layers.
- This fixed a real bug in the old in-repo processor: it generated nullable safe calls (`?.`) for
  nested **non-null** object fields. KMapper routes nested pairs through sub-mappers correctly
  (each level still needs its own `@MapTo`).
- 1.x -> 2.x renames to watch for: `@Ignore`->`@IgnoreMap`,
  `@UseMapTypeConverter(X)`->`@ConvertWith(use = X)`, `@MapDefaultValue` removed (use a constructor
  default), `startKMapper {}` DSL -> `@KMapperConfig` annotation.
- `MappingError` in `core:resulting` is now unreferenced (KMapper throws its own types, which land
  in `toAuthError()`'s `else -> AuthError.Unknown` branch). Left in place deliberately; removing it
  is a separate public-API decision.
- Only modules that **declare** mappings need `kmapper-compiler` on `kspCommonMainMetadata`.
  `:core:processor` no longer exists at all — its last processor was navigation codegen, removed
  with the Gezgin migration (see below).

## DI: Koin compiler plugin, not KSP (see koin-compiler-plugin.md)

- Koin Annotations 4.2.x is processed by `io.insert-koin.compiler.plugin`; `koin-ksp-compiler` is
  gone. `DiConventionPlugin` applies it — modules add nothing but `alias(libs.plugins.domatapp.kmp.di)`.
- Two traps: `module` is a generated **function** and is **compilation-local** — write
  `MyModule().module()`, delete `import org.koin.ksp.generated.module`, and give every Gradle
  module its own `fun xxxModule(): KoinModule` accessor because the aggregator cannot call it.
  Also import `@KoinViewModel` from `org.koin.core.annotation`, not `org.koin.android.annotation`.
- `koinCompiler { logSeverity / versionCheckSeverity }` must be `"info"` here because of
  `allWarningsAsErrors=true`; `compileSafety` off per module, on in `:shared`.

## REST Sources: KtorfitX (replaced Ktorfit, which replaced custom KSP)

`cn.ktorfitx` — https://github.com/annotation-engine/ktorfitx. Chosen over Ktorfit because it covers
REST **and** WebSocket in one annotation family; the smaller/younger-project risk was accepted
deliberately by the owner.

- Interfaces: `@Api` on the interface, `@GET`/`@POST`/`@Body`/`@Query`/`@Path`/`@Header` on members
  (`cn.ktorfitx.multiplatform.annotation.*`). `@WebSocket` lives in `multiplatform-websockets`.
- The processor generates `<pkg>.impls.<Name>Impl` **and an extension property** `Ktorfitx.<name>`
  (interface name, first char lowercased) in that same `impls` package. So it is
  `ktorfitx.authRemoteSource`, not a `create...()` call — and the import is
  `...datasource.impls.authRemoteSource`.
- Gradle plugin id `cn.ktorfitx.multiplatform`; it injects multiplatform-annotation/core/websockets
  and multiplatform-ksp itself, so do NOT declare those in a module that applies it. It works in
  `afterEvaluate`, so plugins-block order does not matter (unlike Ktorfit's plugin).

### Two traps, both read out of the plugin source

- **Ktor version is hard-checked.** `checkDependency("io.ktor", "ktor-client-core")` errors the
  build unless the declared version equals `KtorfitxVersions.KTOR`. KtorfitX version strings are
  `<ktor>-<ktorfitx>`, so `3.4.2-3.3.3` means **Ktor must be exactly 3.4.2**. Bumping Ktor requires
  a matching KtorfitX release; there is no override.
- The checked deps must be **declared in the module applying the plugin**, not inherited — the check
  scans that project's own configurations. Enabling `websockets` adds the same check for
  `ktor-client-websockets`.

### KtorfitX builds its own HttpClient

`KtorfitxConfig` cannot accept a pre-built `HttpClient` (Ktorfit could). Its only configurable
overload `httpClient(engineFactory) { }` also forces naming the engine, so `core:remote` needs an
`expect/actual` `KtorfitxConfig.platformHttpClient` (OkHttp / Darwin). The app-wide `HttpClient`
single is therefore derived: `ktorfitx.config.httpClient`.

It also registers `build/generated/ksp/metadata/commonMain/kotlin` as a commonMain srcDir — the
exact same path `DiConventionPlugin` uses, so they dedupe. Keep DiConventionPlugin on that precise
path. Its task wiring uses `KspAATask`, i.e. it assumes KSP2.

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
  `getSymbolsWithAnnotation` results). Every processor is third-party now, so the bump waits on
  KtorfitX / KMapper / KspPreferences / Gezgin rather than on this repo.
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

## Naming: `*Source`, never `*DataSource`

The three data-layer source types are `{Name}RemoteSource`, `{Name}LocalSource`,
`{Name}ConfigSource` — and the marker annotation is `@ConfigSource`. The directory stays
`datasource/`; only the type suffix changed.

`@ConfigSource` itself no longer exists — `{Name}ConfigSource` interfaces are now annotated with
KspPreferences' `@Preferences` (see below). The `*Source` type-name convention is unchanged.

Two things bite when renaming these:
- A KSP processor that matches on an annotation's **simple name** as well as its FQN needs both
  updated, or codegen silently stops emitting the `*Impl` and the failure surfaces as an unresolved
  `*Impl` reference rather than a processor error.
- KtorfitX derives its generated extension property from the interface name, so renaming an `@Api`
  interface changes the `ktorfitx.<name>` call site and the `...impls.<name>` import with it.

## There is no `core:serialization` module

Removed. It never contained the `SerializationApi`/`KotlinxSerializationApi` abstraction CLAUDE.md
described — only a `Json` provider and a `toSerializationError()` mapper with **zero call sites**.
The `Json` `@Single` now lives in `CoreRemoteModule`. Use `kotlinx.serialization` directly; do not
reintroduce a wrapper.

`SerializationError` stays in `core:resulting` as part of the DomainError hierarchy, but **nothing
throws it today** — a `kotlinx.serialization.SerializationException` from a malformed response body
currently escapes unmapped past `HttpResponseValidator` (which only handles response exceptions).
That gap predates the removal.


## Local preferences: KspPreferences (replaced the in-repo `@ConfigSource` system)

`io.github.semenciuccosmin:preferences-{annotations,compiler}:2.0.0` —
https://github.com/SemenciucCosmin/KspPreferences. Replaced `core:config`'s hand-written
`@ConfigSource`/`@SaveLocalConfig`/`@RetrieveLocalConfig`/`@ObserveLocalConfig`/`@ClearLocalConfig`/
`@ClearAllLocalConfig` annotations and `ConfigSourceProcessor`. Very small/young project (solo
maintainer); adoption risk accepted deliberately by the owner.

**RemoteConfig support was removed outright** — `FirebaseRemoteConfigClient`,
`@RetrieveRemoteConfig`/`@ObserveRemoteConfig` and `dev.gitlive:firebase-config` all had zero usage.
This is the one deliberate exception to the "keep every Firebase library" carve-out, because here
the code itself was dead, not merely the dependency.

`core:config` is now almost empty: an `expect fun preferencesContext(): Any?` bridge
(`PreferencesContextHolder.context` on Android, `null` on iOS) plus `api` on the KspPreferences and
DataStore artifacts. **It declares no Koin module** — each `@Preferences` Source is provided by its
own feature module.

### Five traps, all read out of the library's source

- **Register the compiler per target** (`kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64` — no
  `kspIosX64`, the `iosX64` target was dropped, see below), **never on `kspCommonMainMetadata`.** It
  emits an `actual object` for the
  user's `expect object XConstructor : PreferencesConstructor<X>`, and an `actual` cannot be
  generated into the common metadata compilation. The library's own sample does the same.
- **Every `@Set` parameter must be named `value`.** `GenerateSetFunctionUseCase` hard-codes
  `override suspend fun x(value: T)`. A renamed override parameter is a Kotlin warning, and this
  repo runs `allWarningsAsErrors=true` — so a different name fails the build.
- **Primitive preferences are non-nullable.** `@Get` generates `?: defaultValue` and `@GetFlow`
  `Flow<T>`. Declaring `String?` still compiles (the override narrows, and `Flow` is covariant) but
  the value is never actually null. Model absence with an explicit default constant.
- **`@Clear` wipes the whole store** (`dataStore.edit { it.clear() }`); it takes no parameters,
  must return `Unit`, and has no single-key variant.
- **`PreferencesFactory.create<T>()` (reflection) throws on iOS** by design. Always use
  `@ConstructedBy` + `create(constructor, context)`. Bind it `@Single`: two instances open two
  DataStores over one file.

Also: KspPreferences writes to `<filesDir>/datastore/<name>.preferences_pb`, whereas the old
in-repo `DataStoreFactory` used `<filesDir>/<name>.preferences_pb`. Migrating a store means the old
file is orphaned, not read.

## `iosX64` target was dropped (resolved)

`KmpLibraryConventionPlugin` used to declare `iosX64()` alongside `iosArm64()`/
`iosSimulatorArm64()`, but CLAUDE.md documented only the latter two, and several dependencies
(`kmapper-core`, `preferences-annotations`) publish no `iosX64` variant, so `compileKotlinIosX64`
was unbuildable repo-wide since KMapper was hoisted into `core:data`. Confirmed pre-existing via a
real standalone Gradle run, not caused by any one feature PR.

Fixed by removing `iosX64()` from `KmpLibraryConventionPlugin` and from the target list in
`shared/build.gradle.kts` — the owner's call, made explicitly (it's the obsolete Intel-simulator
target; the docs already only assumed `iosArm64`/`iosSimulatorArm64`). If a future PR reintroduces
an `iosX64()` declaration anywhere, check dependency variant coverage first — this is exactly the
class of failure that bit `core:data` twice (KMapper, then KspPreferences).

## CI does not verify anything

`.github/workflows/ci.yml`'s `build` job runs `echo "build ok"`. Green checks on a PR are
meaningless — never cite them as validation. No Android SDK and `dl.google.com` is blocked in the
agent sandbox, so `./gradlew` cannot get past AGP plugin resolution for this project. Maven Central
*is* reachable: a throwaway KMP project using only `kotlin("multiplatform")` will run, which is the
way to verify that a dependency's variants actually resolve.
