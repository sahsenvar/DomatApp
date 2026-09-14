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
  `core:processor` is still required in `feature/auth/data` for `@ConfigSource` codegen.

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

## Naming: `*Source`, never `*DataSource`

The three data-layer source types are `{Name}RemoteSource`, `{Name}LocalSource`,
`{Name}ConfigSource` — and the marker annotation is `@ConfigSource`. The directory stays
`datasource/`; only the type suffix changed.

Two things bite when renaming these:
- `ConfigSourceProcessor` matches on the annotation's **simple name**
  (`it.shortName.asString() == "ConfigSource"`) as well as its FQN string. Renaming the annotation
  without updating both makes the codegen silently stop emitting the `*Impl`, and the failure shows
  up as an unresolved `AuthConfigSourceImpl`, not as a processor error.
- The processor is registered by fully-qualified name in
  `core/processor/src/main/resources/META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`.
  A class rename must update that file — a repo-wide `grep --include=*.kt` will not see it.
- Ktorfit derives its extension from the interface name (`create${'$'}{classData.name}` in
  `poetspec/FileSpec.kt`), so renaming `AuthRemoteDataSource` changes the call site to
  `ktorfit.createAuthRemoteSource()`.

## There is no `core:serialization` module

Removed. It never contained the `SerializationApi`/`KotlinxSerializationApi` abstraction CLAUDE.md
described — only a `Json` provider and a `toSerializationError()` mapper with **zero call sites**.
The `Json` `@Single` now lives in `CoreRemoteModule`. Use `kotlinx.serialization` directly; do not
reintroduce a wrapper.

`SerializationError` stays in `core:resulting` as part of the DomainError hierarchy, but **nothing
throws it today** — a `kotlinx.serialization.SerializationException` from a malformed response body
currently escapes unmapped past `HttpResponseValidator` (which only handles response exceptions).
That gap predates the removal.
