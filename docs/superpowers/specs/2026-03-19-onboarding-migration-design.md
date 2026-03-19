# Onboarding Migration Design
**Date:** 2026-03-19
**Status:** Approved
**Source:** CMPKMP project
**Target:** DomatApp — `feature:onboarding:presentation`

---

## Overview

Migrate 7 onboarding screens from the CMPKMP project into DomatApp's `feature:onboarding` module following DomatApp's strict KMP/Clean Architecture conventions. All screens, ViewModels, and reusable components are fully adapted to DomatApp's design system (DomatColors, colorResource), MVI pattern (BaseViewModel), and KSP-based navigation code generation.

---

## 1. Route Architecture

### Changes to `core:navigation/Route.kt`

Remove the existing `data object Onboarding : Route` and replace with a nested sealed interface:

```kotlin
sealed interface OnboardingRoute : Route {
    data object Welcome           : OnboardingRoute
    data object Effortless        : OnboardingRoute
    data object Pricing           : OnboardingRoute
    data object Community         : OnboardingRoute
    data object Trust             : OnboardingRoute
    data object Login             : OnboardingRoute
    data object LocationSelection : OnboardingRoute
}
```

Navigation flow: Welcome → Effortless → Pricing → Community → Trust → Login → LocationSelection

---

## 2. Module Dependencies

### `feature:onboarding:presentation/build.gradle.kts`

Add to existing dependencies:
```kotlin
androidMain {
    dependencies {
        implementation(projects.core.design)
        implementation(projects.core.presentation)
        implementation(libs.compose.material3)
        implementation(libs.compose.ui)
        implementation(libs.compose.foundation)
        implementation(libs.compose.uiToolingPreview)
    }
}
dependencies {
    add("kspAndroid", projects.core.processor)
}
```

---

## 3. MVI Contract Structure

Each screen has its own contract files in `commonMain`:

```
feature/onboarding/presentation/src/commonMain/.../model/
├── welcome/
│   ├── OnboardingWelcomeUiState.kt
│   ├── OnboardingWelcomeIntent.kt
│   └── OnboardingWelcomeEffect.kt
├── effortless/   (same structure)
├── pricing/      (same structure)
├── community/    (same structure)
├── trust/        (same structure)
├── login/        (same structure)
└── location/
    ├── LocationSelectionUiState.kt
    ├── LocationSelectionIntent.kt
    └── LocationSelectionEffect.kt
```

### Contract Pattern

```kotlin
// UiState — minimal, no business data needed for simple screens
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

### LocationSelection Contract (more complex)

```kotlin
data class LocationSelectionUiState(
    val selectedBlock: String? = null,
    val selectedApartment: String? = null,
    val isConfirmEnabled: Boolean = false,
)

sealed interface LocationSelectionIntent {
    data class SelectBlock(val block: String) : LocationSelectionIntent
    data class SelectApartment(val apartment: String) : LocationSelectionIntent
    data object Confirm : LocationSelectionIntent
    data object GoBack : LocationSelectionIntent
}

sealed interface LocationSelectionEffect {
    data object NavigateToHome : LocationSelectionEffect
    data object NavigateBack : LocationSelectionEffect
}
```

---

## 4. ViewModel Architecture

**Location:** `feature/onboarding/presentation/src/androidMain/.../viewmodel/`

### Pattern

```kotlin
@NavigationViewModel(OnboardingRoute.Welcome::class)
class OnboardingWelcomeViewModel : BaseViewModel<
    OnboardingWelcomeUiState,
    OnboardingWelcomeIntent,
    OnboardingWelcomeEffect
>(OnboardingWelcomeUiState()) {

    override fun handleIntent(intent: OnboardingWelcomeIntent) {
        when (intent) {
            GoNext -> emitEffect(NavigateToEffortless)
        }
    }
}
```

7 ViewModels total, each with `@NavigationViewModel` annotation for KSP code generation.

### Effect Handlers

Each screen has a `@NavigationEffectHandler` composable for navigation side effects:

```kotlin
@NavigationEffectHandler(OnboardingRoute.Welcome::class)
@Composable
fun OnboardingWelcomeEffectHandler(
    effectFlow: Flow<OnboardingWelcomeEffect>,
    navigator: Navigator,
) {
    LaunchedEffect(Unit) {
        effectFlow.collect { effect ->
            when (effect) {
                NavigateToEffortless -> navigator.navigate(OnboardingRoute.Effortless)
            }
        }
    }
}
```

---

## 5. Screen Architecture

**Location:** `feature/onboarding/presentation/src/androidMain/.../screen/`

### Pattern

```kotlin
@NavigationScreen(OnboardingRoute.Welcome::class)
@Composable
fun OnboardingWelcomeScreen(
    uiState: OnboardingWelcomeUiState,
    onIntent: (OnboardingWelcomeIntent) -> Unit,
) { ... }
```

### Screens List

| Screen | Route | Key Content |
|--------|-------|-------------|
| `OnboardingWelcomeScreen` | `Welcome` | Hero image, progress dots, title, button |
| `OnboardingEffortlessScreen` | `Effortless` | Illustration, glow effects, title, button |
| `OnboardingPricingScreen` | `Pricing` | Supply chain diagram (onboarding-specific) |
| `OnboardingCommunityScreen` | `Community` | Community hero card (onboarding-specific) |
| `OnboardingTrustScreen` | `Trust` | Shield icon, feature items |
| `OnboardingLoginScreen` | `Login` | Hero image, Google Sign-In |
| `LocationSelectionScreen` | `LocationSelection` | Location cards, dropdowns |

---

## 6. Design System Adaptation

### Color Mapping

| CMPKMP | DomatApp |
|--------|----------|
| `AppColors.primaryAlpha10` | `colorResource(DomatColors.primary10)` |
| `AppColors.primaryAlpha20` | `colorResource(DomatColors.primary20)` |
| `AppColors.textPrimary` | `colorResource(DomatColors.textPrimary)` |
| `AppColors.textBody` | `colorResource(DomatColors.textPrimary)` |
| `AppColors.textSecondary` | `colorResource(DomatColors.textSecondary)` |
| `AppColors.textMuted` | `colorResource(DomatColors.textMuted)` |
| `AppColors.textTertiary` | `colorResource(DomatColors.textTertiary)` |
| `AppColors.borderDefault` | `colorResource(DomatColors.borderDefault)` |
| `AppColors.borderLight` | `colorResource(DomatColors.borderLight)` |
| `AppColors.surfaceSubtle` | `colorResource(DomatColors.surfaceSubtle)` |
| `MaterialTheme.colorScheme.primary` | `colorResource(DomatColors.primary)` |
| `MaterialTheme.colorScheme.background` | `colorResource(DomatColors.surfaceDefault)` |

### Spacing

CMPKMP `AppSpacing.*` values are converted to direct `dp` constants or `DomatSpacing.*` equivalents.

---

## 7. Component Migration to `core:presentation`

### Migrated Components

| Component | Source | Target File |
|-----------|--------|-------------|
| `ProgressSteps` | ProgressIndicator.kt | `component/indicator/DomatProgressSteps.kt` |
| `ProgressDots` | ProgressIndicator.kt | `component/indicator/DomatProgressDots.kt` |
| `FeatureListItem` | FeatureListItem.kt | `component/list/DomatFeatureListItem.kt` |
| `HeroBadge` | HeroBadge.kt | `component/badge/DomatHeroBadge.kt` |
| `BottomActionBar` | BottomActionBar.kt | `component/bar/DomatBottomActionBar.kt` |
| `ScreenHeader` | ScreenHeader.kt | `component/header/DomatScreenHeader.kt` |
| `LocationCard` + `LocationCardConnector` | LocationCard.kt | `component/card/DomatLocationCard.kt` |
| `InputDropdown` | InputDropdown.kt | `component/input/DomatInputDropdown.kt` |
| `GoogleSignInButton` | GoogleSignInButton.kt | `component/button/DomatGoogleSignInButton.kt` |

All components: renamed with `Domat` prefix, colors adapted to DomatColors + colorResource.

### Stays in `feature:onboarding:presentation`

| Component | Reason |
|-----------|--------|
| `SupplyChainRow` | Only used in OnboardingPricingScreen |
| `CommunityGoalCard` | Only used in OnboardingCommunityScreen |
| `DeliveryInfoCard` | Only used in OnboardingCommunityScreen |

---

## 8. Navigation Registration

KSP generates `OnboardingPresentationEntries.kt` with all 7 route composables. This extension function is added to the `NavDisplay` entry provider in `composeApp`'s navigation setup.

---

## 9. File Structure Summary

```
feature/onboarding/presentation/src/
├── commonMain/kotlin/com/domatapp/feature/onboarding/presentation/
│   └── model/
│       ├── welcome/   {UiState, Intent, Effect}
│       ├── effortless/{UiState, Intent, Effect}
│       ├── pricing/   {UiState, Intent, Effect}
│       ├── community/ {UiState, Intent, Effect}
│       ├── trust/     {UiState, Intent, Effect}
│       ├── login/     {UiState, Intent, Effect}
│       └── location/  {UiState, Intent, Effect}
└── androidMain/kotlin/com/domatapp/feature/onboarding/presentation/
    ├── viewmodel/
    │   ├── OnboardingWelcomeViewModel.kt
    │   ├── OnboardingEffortlessViewModel.kt
    │   ├── OnboardingPricingViewModel.kt
    │   ├── OnboardingCommunityViewModel.kt
    │   ├── OnboardingTrustViewModel.kt
    │   ├── OnboardingLoginViewModel.kt
    │   └── LocationSelectionViewModel.kt
    └── screen/
        ├── OnboardingWelcomeScreen.kt
        ├── OnboardingEffortlessScreen.kt
        ├── OnboardingPricingScreen.kt
        ├── OnboardingCommunityScreen.kt
        ├── OnboardingTrustScreen.kt
        ├── OnboardingLoginScreen.kt
        └── LocationSelectionScreen.kt

core/presentation/src/androidMain/.../component/
├── indicator/
│   ├── DomatProgressSteps.kt
│   └── DomatProgressDots.kt
├── list/
│   └── DomatFeatureListItem.kt
├── bar/
│   └── DomatBottomActionBar.kt
├── header/
│   └── DomatScreenHeader.kt
├── input/
│   └── DomatInputDropdown.kt   (+ existing DomatTextField.kt)
├── card/
│   └── DomatLocationCard.kt    (+ existing DomatCard.kt)
├── badge/
│   └── DomatHeroBadge.kt       (+ existing DomatBadge.kt)
└── button/
    └── DomatGoogleSignInButton.kt (+ existing DomatButton.kt)
```
