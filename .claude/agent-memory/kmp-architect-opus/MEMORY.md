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

## REST DataSources: Ktorfit (replaced custom @RemoteDataSource KSP)

- The custom `@RemoteDataSource`/`@GET`/`@POST` codegen in `core:remote/annotations` +
  `core/processor/.../remote/` was removed. REST DataSources now use Ktorfit
  (`de.jensklingenberg.ktorfit.http.*`) — plain interfaces, no marker annotation.
- `core:remote` exposes `provideKtorfit(httpClient)` (`@Single`) wrapping the existing `HttpClient`,
  so headers / ContentNegotiation / RemoteError mapping are unchanged.
- DI binding is now uniform: `@Factory fun provide...(ktorfit: Ktorfit) = ktorfit.createXxx()`.
  No more hand-matching a generated constructor signature.
- Ktorfit version is pinned to the release built against the project's Ktor version
  (2.7.3 -> Ktor 3.4.1; 2.7.4+ jumped to Ktor 3.5.0). Check this before bumping.
- The Ktorfit **Gradle plugin is intentionally not applied**: it registers
  `build/generated/ksp/metadata/commonMain/kotlin` as a srcDir, which duplicates the broader
  `build/generated/ksp/metadata` srcDir that `DiConventionPlugin` already adds (duplicate-source
  compile errors), and it applies a Kotlin compiler plugin versioned independently of the project's
  Kotlin. `add("kspCommonMainMetadata", libs.ktorfit.ksp)` is used directly instead.
- Consequence: only the generated `ktorfit.createXxx()` extension works;
  reified `ktorfit.create<Xxx>()` needs the compiler plugin and is unavailable.
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
