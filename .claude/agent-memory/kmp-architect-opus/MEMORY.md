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
  `core:processor` is still required in `feature/auth/data` for `@ConfigDataSource` codegen.

## KSP Generated DataSource Impl Constructors

- RemoteDataSource KSP generates impl with ONLY clients actually used (HttpClient, etc.)
- DI Module factory methods must match generated constructor exactly
- Always check generated impl before writing DI bindings

## Room Database Requires At Least One Entity

- `@Database(entities = [])` causes compile error
- When all entities removed, use PlaceholderEntity pattern (see
  `shared/.../database/AppDatabase.kt`)

## HttpClient Header Strategy (Supabase)

- `apikey: {publicKey}` always sent
- `Authorization: Bearer {accessToken}` only when accessToken is non-null
- NEVER use publicKey as Bearer token
