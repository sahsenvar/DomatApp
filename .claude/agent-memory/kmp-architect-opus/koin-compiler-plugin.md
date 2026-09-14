# Koin Annotations on the Kotlin compiler plugin (no KSP)

DI in this project is Koin Annotations 4.2.x processed by `io.insert-koin.compiler.plugin`
(the Koin Kotlin compiler plugin), **not** `koin-ksp-compiler`. Migrated Sep 2026.

## Where the wiring lives

`DiConventionPlugin` (`domatapp.kmp.di`) does all of it: it applies the compiler plugin, configures
`koinCompiler { }`, and adds `koin-core` / `koin-annotations` (commonMain) and `koin-android`
(androidMain). A module that needs DI applies `domatapp.kmp.di` and adds nothing else. `:shared` is
outside that convention plugin and applies `alias(libs.plugins.koinCompiler)` directly.

Because the plugin is a `KotlinCompilerPluginSupportPlugin`, a single `pluginManager.apply` covers
every compilation — commonMain metadata and each Android/iOS target. There is no generated source
directory to register and no task ordering to arrange. That is the main reason the old setup had
~25 lines of KSP boilerplate per module and this one has none.

## Sharp edges

- **`module` is a generated FUNCTION and it is compilation-local.** Koin's migration guide is wrong
  on both points (checked against 4.2.2 + plugin 1.2.1 by compiling and running a probe):
  - Write `MyModule().module()`, not `MyModule().module` — the property form fails with
    "Function invocation 'module()' expected".
  - `import org.koin.ksp.generated.module` must be deleted; that package no longer exists.
  - The accessor is generated **only in the compilation that declares the `@Module` class**, so
    `CoreRemoteModule().module()` does NOT resolve from `:shared` ("receiver type mismatch").
    Each Gradle module therefore exposes `fun coreRemoteModule(): KoinModule = CoreRemoteModule().module()`
    next to its `@Module`, and `KoinInitializer` calls those. Import
    `org.koin.core.module.Module as KoinModule` to avoid clashing with the `@Module` annotation.
  - `@Module(includes = [Other::class])` DOES work across Gradle modules — verified at runtime.
  - The documented alternatives do not work on these versions: `startKoin<MyApp>()` has no such
    overload in koin-core 4.2.2, and `@Configuration` + bare `startKoin { }` registered nothing
    (every `get()` threw NoDefinitionFoundException). Load modules explicitly.
- **`@KoinViewModel` moved package**: `org.koin.android.annotation` -> `org.koin.core.annotation`.
  The annotation is otherwise unchanged. Every other annotation (`@Module`, `@ComponentScan`,
  `@Single`, `@Factory`, `@Named`, `@Scoped`, `@InjectedParam`, `@Property`, `@Scope`) is identical.
- **`logSeverity` and `versionCheckSeverity` must be `"info"` in this project.** They default to
  `"warning"`, and `gradle.properties` sets `kotlin.compiler.allWarningsAsErrors=true`, so the
  plugin's purely informational output would fail the build. This is Koin issue #73.
- **`compileSafety` must be off for ordinary modules.** It validates that a `@Module` can satisfy
  all its own dependencies, but modules here compose via `@Module(includes = [...])` across Gradle
  modules, so per-module validation reports things it cannot see. This is the same reason the old
  KSP setup passed `KOIN_CONFIG_CHECK=false`. `:shared` sets `compileSafety = true` because it owns
  `startKoin` and sees the whole graph.
- **`:shared` never gets an up-to-date Kotlin compile.** The plugin scans sources for `startKoin` /
  `koinApplication` / `@KoinApplication` and auto-enables `strictSafety` on whatever it finds, which
  disables up-to-date checks and caching for that compile task. Deliberate: DSL lambda bodies are
  not part of any declaration's ABI, so incremental compilation would skip re-validation. Escape
  hatch for a false positive is `strictSafetyForceOff = true`, not `strictSafety = false` (which is
  ignored once detection fires).
- **Kotlin version gate.** One artifact spans Kotlin 2.3.20 - 2.4.20; verified versions are
  2.3.20 / 2.4.0 / 2.4.10 / 2.4.20. K2 only. Check this before bumping Kotlin.
- **Custom `@Qualifier` annotation classes changed meaning** (now type qualifiers via `named<T>()`
  rather than string names). This project has none; watch for it if any are added.
- The old DSL shorthand `singleOf(::Type)` becomes `single<Type>()`. This project uses no Koin DSL.

## KSP did not go away

`core:processor` (mapping / config / navigation) and `ktorfit-ksp` still run under KSP. Only modules
that register one of those apply `alias(libs.plugins.ksp)`. `DiConventionPlugin` still registers the
`build/generated/ksp/metadata/commonMain/kotlin` srcDir and the `kspCommonMainKotlinMetadata` task
ordering, but now guarded behind `withPlugin(KSP_PLUGIN_ID)` so DI-only modules are unaffected.
