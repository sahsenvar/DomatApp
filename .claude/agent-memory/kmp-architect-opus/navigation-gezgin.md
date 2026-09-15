# Navigation: Gezgin

Linked from MEMORY.md. Library: https://github.com/sahsenvar/Gezgin

`io.github.sahsenvar:gezgin-{core,processor}` — https://github.com/sahsenvar/Gezgin. Owned by the
same person as this repo (and as KMapper). Annotation + KSP navigation over AndroidX Navigation 3.
Removed in the same change: `core:navigation`'s `Route`/`Navigator`/`annotations/*`, the whole
`:core:processor` Gradle module, `MainViewModel`'s back stack, `LocalNavigator`.

### Layout chosen here

- Graph lives in **`core/navigation/src/androidMain`** as flat sibling `@NavGraph` sealed
  interfaces (`OnboardingGraph`, `AuthGraph`, `MainGraph`), each extending `dev.gezgin.core.Route`.
  androidMain because `gezgin-core` publishes only `android` + `jvm` — **no iOS klib**.
- The app's single `@ScreenWrapper` + its `@ScreenSlot` markers (`ViewModelOf`, `Effects`) live in
  `core/presentation/src/androidMain/.../screen/DomatScreenRoot.kt`, mirroring Gezgin's
  `sample/designsystem`.
- Per-feature `@Screen` / `@ViewModelOf` / `@Effects` providers + a hand-written
  `GezginEntryScope.xGraphEntries()` bundle that calls the generated `provideXEntry()`s.

### Traps (all read out of the processor source, several are silent)

- **`gezgin.wrapperPackages` KSP arg is mandatory in every feature module.** KSP cannot enumerate
  classpath declarations by annotation, so a module that omits it does not see the wrapper and
  generates **unwrapped** entries — a `[SW6]` KSP *warning*, not an error. Same silent failure if a
  `@Screen`'s signature does not unify with the content slot. Grep build output for `SW6`.
- **Navigator params must be written by exact simple name** (`LoginNavigator`) — the type does not
  exist during the round that reads it, so it is matched by written name, not resolved (`[SW11]`).
- **`@ReplaceTo`'s `clearUpTo` missing from the stack = silent no-op** (`NavEvent.ReplaceTargetMissing`).
  Anchor it on the start destination. `replaceAll(X)` ≙ `@ReplaceTo(X, clearUpTo = <start>, inclusive = true)`.
- Generated names strip a trailing `Route`, then a trailing `Screen`/`Flow`: `LoginRoute` →
  `LoginNavigator`, `provideLoginEntry()`, `goToLogin()`, `replaceToHome()`.
- Routes need **no** `@Serializable` (0.3.0 generates their serializers); their *parameter types* do.
- The graph module must **not** apply the Compose compiler plugin (Gezgin's own codegen refuses to
  emit a `@Composable` there for this reason — unlowered bytecode → `NoSuchMethodError`).
- `@Effects` providers are **plain** functions — no composition locals. Anything they need
  (`Context`, snackbar, `onIntent`) has to be passed through a slot parameter; here that is
  `DomatEffectScope<I>`.
- Use `koinViewModel()` (koin-compose-viewmodel), not `koinInject()`, in `@ViewModelOf`: only the
  former resolves against `LocalViewModelStoreOwner` = the per-NavEntry ViewModelStore.
- **The slot unifier decomposes function types and nothing else** (`SlotUnifier.kt`). A generic
  project type in a slot (`DomatEffectScope<I>`) is compared as an opaque whole → `[SW8]`. Anything
  carrying a type parameter must be its own function-typed slot parameter.
- **A slot's return type is NOT unified**, so `viewModel: @Composable () -> BaseViewModel<S,I,E>`
  binds no type parameter at all. `E` is bound only by the effect slot → that slot must have **no**
  Kotlin default and every route needs an `@Effects` provider, or `[SW7]` fires. Gezgin's own
  `sample/hello` gets away with defaults elsewhere only because its effect slot is mandatory.

### Version / toolchain reality (verified 2026-09-14, recheck before trusting)

- **Maven Central has only 0.1.0 and 0.2.0; `@ScreenWrapper` is 0.3.0-only and 0.3.0 is
  unpublished** (no `v0.3.0` git tag either). 0.2.0 is the pre-`@ScreenWrapper` design
  (`gezgin-mvi` + `@MviViewModel`/`@EffectHandler`), which 0.3.0 removed outright. Anything written
  against 0.3.0 cannot resolve until the owner publishes it.
- Gezgin builds on **Kotlin 2.3.21 / KSP 2.3.9 / AGP 8.13.2 / CMP 1.11.1 / nav3 1.0.0**; DomatApp
  is on **Kotlin 2.4.10 / KSP 2.3.11 / AGP 9.4.0 / CMP 1.12.0**. Gezgin's CHANGELOG 0.2.0 explicitly
  *defers* Kotlin 2.4.10 — that is about Gezgin's own build, not about consuming it, but it means
  no Gezgin release has ever been compiled against DomatApp's toolchain.
- `compatibility/zad-consumer` in the Gezgin repo is a ZAD-targeted compatibility fixture and is
  worth reading first for any integration question — but it pins Kotlin 2.3.21, AGP 9.2.1 and Koin
  compiler 1.0.1, i.e. **not** DomatApp's actual versions.
- **No iOS/CMP support and none on the roadmap** (`gezgin-core` is commonMain + androidMain +
  jvmMain; the README's compat table marks Compose Multiplatform as "Android + desktop;
  iOS/web compile-level"). Relevant because the owner has mentioned eventually moving DomatApp to
  shared Compose Multiplatform UI.
- Also deliberately absent (V2): multiple/independent back stacks, deep-link route dispatch.

### Non-obvious dependency finding (saves a substitution block)

`org.jetbrains.androidx.lifecycle:*-android` artifacts **depend on** their `androidx.lifecycle:*`
counterparts rather than duplicating the classes (verified from
`lifecycle-viewmodel-compose-android-2.11.0.pom`). So Gezgin's androidx-family lifecycle deps and
DomatApp's JetBrains-family ones coexist without duplicate-class errors, and the
`dependencySubstitution` block Gezgin's own `zad-consumer` fixture uses is not needed here.
