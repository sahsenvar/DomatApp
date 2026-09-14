# Dependency upgrade notes

Verified during the Sep 2026 project-wide bump (PR #10). Re-verify before trusting; these are
version-sensitive facts, not permanent truths.

## How to check versions from an agent sandbox

- **Maven Central is reachable**: `https://repo1.maven.org/maven2/<group with / >/<artifact>/maven-metadata.xml`
  gives `<release>` / `<latest>` and the full version list. A `HEAD` on the exact `.pom` URL is a
  real resolve check. POMs are also the authoritative source for what a library was *built against*
  (e.g. a compiler plugin's `kotlin-stdlib` version).
- **Google Maven (`dl.google.com` / `maven.google.com`) is blocked**, and so are the usual mirrors.
  AGP, every `androidx.*` artifact, `com.google.firebase:firebase-bom` and `googleid` must come from
  official release notes. The AndroidX aggregate table
  (`developer.android.com/jetpack/androidx/versions`) covers many libraries in one fetch and is more
  reliable than the per-library pages, which sometimes render stale numbers.
- Sources jars (`-sources.jar`) of Gradle plugins are usually published — reading them beats
  guessing at plugin behaviour.

## Kotlin ceiling: SKIE

`:shared` applies `co.touchlab.skie`. SKIE ships compiler support for an explicit list of Kotlin
versions. To find the ceiling, download `co.touchlab.skie:gradle-plugin:<version>.jar` and grep the
embedded version strings. As of SKIE 0.10.14 (Jul 2026) that list ends at **Kotlin 2.4.10**;
touchlab/SKIE#205 tracks 2.4.20. Bumping Kotlin past the SKIE ceiling breaks the iOS framework
build, not the Android one, so it will not show up in a quick `assembleDebug`.

## KSP

- Version naming changed at **KSP 2.3.0**: no more `<kotlinVersion>-<kspVersion>`. `ksp = "2.3.11"`
  is a standalone KSP version.
- **2.3.12 is a breaking release.** Backing fields become their own KSP symbols:
  `Resolver.getSymbolsWithAnnotation` may return `KSBackingField` where it used to return
  `KSPropertyDeclaration`, and `effectiveJavaModifiers` returns less. Processors opt in via
  `SymbolProcessorEnvironment.registerProcessorForNewFeatures` plus moving `KSVisitor`
  implementations to `KSVisitorNext` / `KSTopDownVisitor(enableNewFeatures = true)`.
  `:core:processor` has not been migrated.
- Third-party processor floors worth remembering: `koin-ksp-compiler:2.3.1` was built against
  `symbol-processing-api:2.3.2`; `io.github.sahsenvar:kmapper-compiler:2.2.2` against **2.3.9**.

## Koin

Koin Annotations jumped from `2.3.1` straight to `4.2.x` (aligned with koin-core) and in doing so
**replaced KSP with a Kotlin compiler plugin**: `io.insert-koin:koin-compiler-plugin` +
`koin-compiler-gradle-plugin` (1.2.1). `koin-ksp-compiler` stops at 2.3.1 stable. Migrating means
rewriting `DiConventionPlugin`, every module's `kspCommonMainMetadata` wiring, and the DI section of
`CLAUDE.md`. Koin core and Koin Annotations must move together.

## Compose Multiplatform

`org.jetbrains.compose.material3:material3` is versioned on its own track, `X.Y.0-alphaNN`, and is
matched to the CMP plugin by `X.Y` (CMP 1.10.2 ↔ material3 1.10.0-alpha05; CMP 1.12.0 ↔
1.12.0-alpha03). The `-alpha` suffix is normal for that artifact and is not a reason to hold back.
The compose-compiler Gradle plugin follows the `kotlin` version ref, never its own.

## Other pairings

- **Ktorfit ↔ Ktor**: Ktorfit 2.7.3 → Ktor 3.4.1, 2.7.4+ → Ktor 3.5.x. Check before moving either.
- **GitLive Firebase ↔ firebase-bom**: read the BoM version straight out of
  `dev.gitlive:firebase-<x>-android:<v>`'s POM rather than picking the newest BoM independently.
- **AGP ↔ Gradle ↔ compileSdk**: AGP 9.4.0 needs Gradle ≥ 9.6.0 and supports up to API 37.
- `compileSdk`/`minSdk` are duplicated: `libs.versions.toml` *and* a hardcoded copy in
  `KmpLibraryConventionPlugin`. Bump both.

## The real risk in any Kotlin bump here

`gradle.properties` sets `kotlin.compiler.allWarningsAsErrors=true` and
`KmpLibraryConventionPlugin` sets `extraWarnings.set(true)`. New deprecation warnings from a Kotlin
feature-release bump become hard build failures. Expect to fix warnings on the first real build
after any Kotlin bump.
