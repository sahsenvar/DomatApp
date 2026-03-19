# Onboarding Migration Design
**Date:** 2026-03-19
**Status:** Approved
**Source:** CMPKMP project
**Target:** DomatApp — `feature:onboarding:presentation`

---

## Overview

Migrate 7 onboarding screens from a separate KMP project into DomatApp's `feature:onboarding` module following DomatApp's strict Clean Architecture, KSP-based navigation code generation, and MVI conventions. All naming, colors, and patterns are fully adapted to DomatApp conventions — no external project references remain.

**Navigation flow:** Welcome → Effortless → Pricing → Community → Trust → Login → LocationSelection → `Route.Main.Home`

---

## 1. Route Architecture

### Changes to `core:navigation/Route.kt`

Remove `data object Onboarding : Route` and replace with a nested sealed interface. Each entry requires `@Serializable` (consistent with `AuthRoute` pattern):

```kotlin
@Serializable
sealed interface OnboardingRoute : Route {
    @Serializable data object Welcome           : OnboardingRoute
    @Serializable data object Effortless        : OnboardingRoute
    @Serializable data object Pricing           : OnboardingRoute
    @Serializable data object Community         : OnboardingRoute
    @Serializable data object Trust             : OnboardingRoute
    @Serializable data object Login             : OnboardingRoute
    @Serializable data object LocationSelection : OnboardingRoute
}
```

Any existing reference to `Route.Onboarding` in `composeApp` navigation setup must be updated to `Route.OnboardingRoute.Welcome`.

---

## 2. Module Dependencies

### `feature:onboarding:presentation/build.gradle.kts`

Mirror the auth presentation module exactly:

```kotlin
plugins {
    alias(libs.plugins.domatapp.kmp.library)
    alias(libs.plugins.domatapp.kmp.di)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(projects.core.presentation)
                implementation(projects.core.common)
                implementation(projects.core.navigation)
                implementation(projects.core.resource)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.koin.core.viewmodel)
                implementation(libs.compose.components.resources)
            }
        }
        androidMain {
            dependencies {
                implementation(projects.core.design)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.uiTooling)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.koin.compose)
                implementation(libs.navigation3.runtime)
            }
        }
        iosMain {
            dependencies {}
        }
    }
}

// @NavigationViewModel import style (use fully qualified form):
// import com.domatapp.core.navigation.Route
// @NavigationViewModel(Route.OnboardingRoute.Welcome::class)

dependencies {
    add("kspAndroid", projects.core.processor)
}

// Ensure kspAndroidMain runs after kspCommonMainKotlinMetadata (Koin KSP)
tasks.matching { it.name == "kspAndroidMain" }.configureEach {
    dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}
```

---

## 3. Koin DI Module

**Location:** `feature/onboarding/presentation/src/commonMain/.../di/OnboardingPresentationModule.kt`

```kotlin
@Module
@ComponentScan("com.domatapp.feature.onboarding.presentation")
class OnboardingPresentationModule
```

No domain module dependency needed (onboarding screens are navigation-only, no use cases required initially).

---

## 4. MVI Contract Structure

**Location:** `feature/onboarding/presentation/src/commonMain/.../model/`

Each screen has its own contract files. Pattern for simple screens (Welcome, Effortless, Pricing, Community, Trust):

```kotlin
// UiState
data class OnboardingWelcomeUiState(val isLoading: Boolean = false)

// Intent
sealed interface OnboardingWelcomeIntent {
    data object GoNext : OnboardingWelcomeIntent
}

// Effect
sealed interface OnboardingWelcomeEffect {
    data object NavigateToEffortless : OnboardingWelcomeEffect
}
```

### Login Contract

```kotlin
data class OnboardingLoginUiState(val isLoading: Boolean = false)

sealed interface OnboardingLoginIntent {
    data object OnGoogleSignInClicked : OnboardingLoginIntent
}

sealed interface OnboardingLoginEffect {
    data object NavigateToLocationSelection : OnboardingLoginEffect
}
```

> **Note:** `OnboardingLoginScreen` coexists with the existing `AuthPage` in `feature:auth:presentation`. They serve different flows — onboarding login leads to location selection, while `AuthPage` leads directly to home. Both are kept independently.

### LocationSelection Contract

```kotlin
data class LocationSelectionUiState(
    val selectedBlock: String? = null,
    val selectedApartment: String? = null,
    val isConfirmEnabled: Boolean = false,
)

sealed interface LocationSelectionIntent {
    data class SelectBlock(val block: String)     : LocationSelectionIntent
    data class SelectApartment(val apt: String)   : LocationSelectionIntent
    data object Confirm                           : LocationSelectionIntent
    data object GoBack                            : LocationSelectionIntent
}

sealed interface LocationSelectionEffect {
    data object NavigateToHome : LocationSelectionEffect   // → Route.Main.Home
    data object NavigateBack   : LocationSelectionEffect
}
```

---

## 5. ViewModel Architecture

**Location:** `feature/onboarding/presentation/src/commonMain/.../viewmodel/` (shared with iOS)

### Pattern (consistent with `AuthViewModel`)

- `@NavigationViewModel(Route.OnboardingRoute.Welcome::class)` — fully qualified route
- `@KoinViewModel` — required for Koin factory generation
- Override `onIntent()` (not `handleIntent`) — matches `BaseViewModel.onIntent()`

```kotlin
@NavigationViewModel(Route.OnboardingRoute.Welcome::class)
@KoinViewModel
class OnboardingWelcomeViewModel : BaseViewModel<
    OnboardingWelcomeUiState,
    OnboardingWelcomeIntent,
    OnboardingWelcomeEffect
>(OnboardingWelcomeUiState()) {

    override fun onIntent(intent: OnboardingWelcomeIntent) {
        when (intent) {
            OnboardingWelcomeIntent.GoNext -> emitEffect(OnboardingWelcomeEffect.NavigateToEffortless)
        }
    }
}
```

7 ViewModels total: Welcome, Effortless, Pricing, Community, Trust, Login, LocationSelection.

---

## 6. Effect Handler Architecture

**Location:** `feature/onboarding/presentation/src/androidMain/.../screen/`

Pattern mirrors `AuthEffectHandler` exactly — no navigator parameter, uses `LocalNavigator.current` + `collectLatest`:

```kotlin
@NavigationEffectHandler(Route.OnboardingRoute.Welcome::class)
@Composable
fun OnboardingWelcomeEffectHandler(
    effectFlow: Flow<OnboardingWelcomeEffect>,
) {
    val navigator = LocalNavigator.current

    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                OnboardingWelcomeEffect.NavigateToEffortless ->
                    navigator.navigate(Route.OnboardingRoute.Effortless)
            }
        }
    }
}
```

LocationSelection effect handler navigates to `Route.Main.Home` via `navigator.replaceAll(Route.Main.Home)`.

---

## 7. Screen Architecture

**Location:** `feature/onboarding/presentation/src/androidMain/.../screen/`

Pattern mirrors `AuthPage`:

```kotlin
@NavigationScreen(Route.OnboardingRoute.Welcome::class)
@Composable
fun OnboardingWelcomeScreen(
    uiState: OnboardingWelcomeUiState,
    onIntent: (OnboardingWelcomeIntent) -> Unit,
) { ... }
```

### Screens

| Screen | Route | Key Content |
|--------|-------|-------------|
| `OnboardingWelcomeScreen` | `Welcome` | Hero image, progress dots (step 1/5), title, next button |
| `OnboardingEffortlessScreen` | `Effortless` | Illustration, glow effects, progress dots (step 2/5), next button |
| `OnboardingPricingScreen` | `Pricing` | Supply chain diagram, progress dots (step 3/5), next button |
| `OnboardingCommunityScreen` | `Community` | Community hero card, progress dots (step 4/5), next button |
| `OnboardingTrustScreen` | `Trust` | Shield icon, feature items, progress dots (step 5/5), next button |
| `OnboardingLoginScreen` | `Login` | Hero image, Google Sign-In button, terms text |
| `LocationSelectionScreen` | `LocationSelection` | Location cards hierarchy, block/apartment dropdowns, confirm button |

---

## 8. Design System Adaptation

### Color Mapping

| Source | DomatApp |
|--------|----------|
| `primary alpha 10%` | `colorResource(DomatColors.primary10)` |
| `primary alpha 20%` | `colorResource(DomatColors.primary20)` |
| `text primary` | `colorResource(DomatColors.textPrimary)` |
| `text body` | `colorResource(DomatColors.textPrimary)` |
| `text secondary` | `colorResource(DomatColors.textSecondary)` |
| `text muted` | `colorResource(DomatColors.textMuted)` |
| `text tertiary` | `colorResource(DomatColors.textTertiary)` |
| `border default` | `colorResource(DomatColors.borderDefault)` |
| `border light` | `colorResource(DomatColors.borderLight)` |
| `surface subtle` | `colorResource(DomatColors.surfaceSubtle)` |
| `colorScheme.primary` | `colorResource(DomatColors.primary)` |
| `colorScheme.background` | `colorResource(DomatColors.surfaceDefault)` |

### Spacing

All spacing values converted to direct `dp` constants or `DomatSpacing.*` equivalents.

---

## 9. Component Migration to `core:presentation`

All components renamed with `Domat` prefix, colors adapted to `DomatColors + colorResource`.

### Migrated

| New Name | Target File |
|----------|-------------|
| `DomatProgressSteps` | `component/indicator/DomatProgressSteps.kt` |
| `DomatProgressDots` | `component/indicator/DomatProgressDots.kt` |
| `DomatFeatureListItem` | `component/list/DomatFeatureListItem.kt` |
| `DomatHeroBadge` | `component/badge/DomatHeroBadge.kt` |
| `DomatBottomActionBar` | `component/bar/DomatBottomActionBar.kt` |
| `DomatScreenHeader` | `component/header/DomatScreenHeader.kt` |
| `DomatLocationCard` + `DomatLocationCardConnector` | `component/card/DomatLocationCard.kt` |
| `DomatInputDropdown` | `component/input/DomatInputDropdown.kt` |
| `DomatGoogleSignInButton` | `component/button/DomatGoogleSignInButton.kt` |

### Stays in `feature:onboarding:presentation` (onboarding-specific)

| Component | Used By |
|-----------|---------|
| `SupplyChainRow` | `OnboardingPricingScreen` only |
| `CommunityGoalCard` | `OnboardingCommunityScreen` only |
| `DeliveryInfoCard` | `OnboardingCommunityScreen` only |

---

## 10. Navigation Registration

KSP generates `OnboardingPresentationEntries.kt`. The generated `onboardingPresentationEntries()` extension is added to `composeApp`'s `NavDisplay` entry provider alongside `authPresentationEntries()`.

---

## 11. File Structure Summary

```
feature/onboarding/presentation/src/
├── commonMain/kotlin/com/domatapp/feature/onboarding/presentation/
│   ├── di/
│   │   └── OnboardingPresentationModule.kt
│   ├── viewmodel/
│   │   ├── OnboardingWelcomeViewModel.kt
│   │   ├── OnboardingEffortlessViewModel.kt
│   │   ├── OnboardingPricingViewModel.kt
│   │   ├── OnboardingCommunityViewModel.kt
│   │   ├── OnboardingTrustViewModel.kt
│   │   ├── OnboardingLoginViewModel.kt
│   │   └── LocationSelectionViewModel.kt
│   └── model/
│       ├── welcome/   {UiState, Intent, Effect}
│       ├── effortless/{UiState, Intent, Effect}
│       ├── pricing/   {UiState, Intent, Effect}
│       ├── community/ {UiState, Intent, Effect}
│       ├── trust/     {UiState, Intent, Effect}
│       ├── login/     {UiState, Intent, Effect}
│       └── location/  {UiState, Intent, Effect}
└── androidMain/kotlin/com/domatapp/feature/onboarding/presentation/
    └── screen/
        ├── OnboardingWelcomeScreen.kt        (+ OnboardingWelcomeEffectHandler)
        ├── OnboardingEffortlessScreen.kt     (+ OnboardingEffortlessEffectHandler)
        ├── OnboardingPricingScreen.kt        (+ OnboardingPricingEffectHandler)
        ├── OnboardingCommunityScreen.kt      (+ OnboardingCommunityEffectHandler)
        ├── OnboardingTrustScreen.kt          (+ OnboardingTrustEffectHandler)
        ├── OnboardingLoginScreen.kt          (+ OnboardingLoginEffectHandler)
        └── LocationSelectionScreen.kt        (+ LocationSelectionEffectHandler)

core/presentation/src/androidMain/.../component/
├── indicator/  DomatProgressSteps.kt, DomatProgressDots.kt
├── list/       DomatFeatureListItem.kt
├── bar/        DomatBottomActionBar.kt
├── header/     DomatScreenHeader.kt
├── input/      DomatInputDropdown.kt
├── card/       DomatLocationCard.kt
├── badge/      DomatHeroBadge.kt
└── button/     DomatGoogleSignInButton.kt
```
