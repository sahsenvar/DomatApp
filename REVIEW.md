# REVIEW.md — project review rules

Reviewer Guy reads this file before every review. These are DomatApp-specific rules on top of its general
Kotlin / Compose / KMP / Postgres skill sets. CI already enforces builds, tests, detekt, design diffs and
role paths, so this file covers what only a reader can catch. Each rule carries the severity to report it with.
`CLAUDE.md` is the source of the architecture; cite the section when a rule comes from it.

## Architecture (CLAUDE.md › Architecture, Dependency Rules)

- **warning** — A `feature:*:domain` module depends on anything but `:core:domain` / `:core:resulting`, or a
  `presentation` module depends on its feature's `data` module.
- **warning** — A feature module re-declares a dependency that `:core:data` / `:core:presentation` already
  exposes as `api`, or a Source is named `*DataSource` instead of `*Source`.
- **warning** — A repository uses a concrete client (`HttpClient`, `Ktorfitx`, DataStore) directly instead of
  its `*RemoteSource` / `*LocalSource` / `*ConfigSource`.
- **warning** — A KMapper `Result` is propagated past the call site instead of `.getOrThrow()` (CLAUDE.md ›
  Object Mapping), or an error is swallowed instead of being mapped `RemoteError → <Feature>Error`.
- **critical** — Anything that can break the minimum-version check (Supabase `app_config`, CLAUDE.md ›
  Config Source). An app that cannot read it cannot even show the update screen.

## Screens and design contract (design/README.md › Kod)

- **warning** — A screen file contains `Color(0x…)`, `colorResource(…)`, `RoundedCornerShape(<n>.dp)` or raw
  spacing `dp` (rule 13). Component-internal sizes are fine inside component files only.
- **warning** — A component listed in `design/components.yaml` is re-implemented inside a screen instead of
  calling its composable.
- **warning** — A user-visible string is hard-coded instead of coming from `strings.xml` (rule 14).
- **warning** — An interactive element misses its `testTag` `<ScreenId>/<elementKey>` (QA's black-box tests
  depend on it), or a touch target is smaller than 48 dp (rule 10).
- **suggestion** — A screen state from the card has no `@Preview(name = "<ID>@<state>")` (rule 15); CI's
  design job reports the pixel diff, this rule only flags a missing state.

## Navigation (CLAUDE.md › Navigation)

- **warning** — A `@Effects` provider is `@Composable`, a `@ViewModelOf` provider uses `koinInject()`
  instead of `koinViewModel()`, or effects are emitted on a `replay = 0` SharedFlow.
- **warning** — A new feature presentation module runs Gezgin's KSP without
  `gezgin.wrapperDeclarations` (screens silently lose their ViewModel).

## Tests (karar kaydı K-44, K-160)

- **warning** — Production code changes with no test change: a ViewModel `action` / `navigation` without a
  ViewModel unit test, or RLS / SQL / edge-function changes under `supabase/**` without a test. Say which
  behaviour is untested.
- **warning** — A test is skipped, disabled, `@Ignore`d or loosened to make CI green.

## Backend (supabase/**, K-52..K-56)

- **critical** — A migration or policy weakens RLS, exposes a table without RLS, or touches production data
  directly.
- **warning** — An incompatible API change without the release order (new app, store approval, minimum
  version raised, then backend) or without updating the API contract in the YouTrack KB.

## Process

- **warning** — A PR is not linked to a YouTrack task (branch `da-12`, title or commits `DA-12: …`).
- **suggestion** — The PR description does not tell QA which tests to run and what else may be affected.
