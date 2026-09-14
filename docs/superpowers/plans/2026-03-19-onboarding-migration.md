# Onboarding Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate 7 onboarding screens from CMPKMP into DomatApp's `feature:onboarding:presentation`, fully adapted to DomatApp conventions (DomatColors, KSP annotations, MVI, Koin).

**Architecture:** KSP-generated navigation via `@NavigationScreen`/`@NavigationViewModel`/`@NavigationEffectHandler`. Each screen has its own ViewModel, MVI contracts, and EffectHandler. Reusable components go to `core:presentation`, onboarding-specific ones stay in the feature module.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, Koin Annotations, Navigation3, Moko Resources, KSP

---

## Color Mapping Reference

| CMPKMP AppColors | DomatApp |
|---|---|
| `AppColors.primaryAlpha5` | `colorResource(DomatColors.primary5)` |
| `AppColors.primaryAlpha10` | `colorResource(DomatColors.primary10)` |
| `AppColors.primaryAlpha20` | `colorResource(DomatColors.primary20)` |
| `AppColors.primaryAlpha30` | `colorResource(DomatColors.primary30)` *(new)* |
| `AppColors.textPrimary` | `colorResource(DomatColors.textPrimary)` |
| `AppColors.textBody` | `colorResource(DomatColors.textPrimary)` |
| `AppColors.textSecondary` | `colorResource(DomatColors.textSecondary)` |
| `AppColors.textMuted` | `colorResource(DomatColors.textMuted)` |
| `AppColors.textTertiary` | `colorResource(DomatColors.textTertiary)` |
| `AppColors.textFeature` | `colorResource(DomatColors.textPrimary)` |
| `AppColors.borderDefault` | `colorResource(DomatColors.borderDefault)` |
| `AppColors.borderLight` | `colorResource(DomatColors.borderLight)` |
| `AppColors.borderActive` | `colorResource(DomatColors.primary)` |
| `AppColors.surfaceSubtle` | `colorResource(DomatColors.surfaceSubtle)` |
| `MaterialTheme.colorScheme.primary` | `colorResource(DomatColors.primary)` |
| `MaterialTheme.colorScheme.background` | `colorResource(DomatColors.surfaceDefault)` |
| `MaterialTheme.colorScheme.surface` | `colorResource(DomatColors.surfaceDefault)` |

## Resource Reference

All drawables copied from CMPKMP → `feature/onboarding/presentation/src/commonMain/composeResources/drawable/`

Access pattern: `domatapp.feature.onboarding.presentation.generated.resources.Res`

---

## Task 1: Update Route.kt

**Files:**
- Modify: `core/navigation/src/commonMain/kotlin/com/domatapp/core/navigation/Route.kt`

- [ ] Replace `data object Onboarding : Route` with `sealed interface OnboardingRoute`:

```kotlin
package com.domatapp.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    sealed interface AuthRoute : Route {
        @Serializable
        data object AuthScreen : AuthRoute

        @Serializable
        data class AddressValidationScreen(
            val address: String
        ) : AuthRoute
    }

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

    @Serializable
    sealed interface Main : Route {
        @Serializable data object Home : Main
        @Serializable data object Wallet : Main
        @Serializable data object Notifications : Main
        @Serializable data object Profile : Main
    }

    @Serializable
    sealed interface Product : Route {
        @Serializable data object List : Product
        @Serializable data class Detail(val productId: String) : Product
    }
}
```

- [ ] Commit: `feat(navigation): add OnboardingRoute sealed interface`

---

## Task 2: Update feature:onboarding:presentation/build.gradle.kts

**Files:**
- Modify: `feature/onboarding/presentation/build.gradle.kts`

- [ ] Replace entire file:

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

dependencies {
    add("kspAndroid", projects.core.processor)
}

tasks.matching { it.name == "kspAndroidMain" }.configureEach {
    dependsOn(tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}
```

- [ ] Commit: `build(onboarding): configure presentation module dependencies`

---

## Task 3: Add Missing Colors & Update DomatButton

**Files:**
- Modify: `core/resource/src/commonMain/moko-resources/base/colors.xml`
- Modify: `core/design/src/androidMain/kotlin/com/domatapp/core/design/theme/DomatColors.kt`
- Modify: `core/presentation/src/androidMain/kotlin/com/domatapp/core/presentation/component/button/DomatButton.kt`

- [ ] Add `primary_30` to `colors.xml` (after `primary_20`):

```xml
<color name="primary_30">#4D13EC49</color>
```

- [ ] Add `primary30` to `DomatColors.kt` (after `primary20`):

```kotlin
val primary30: ColorResource = MR.colors.primary_30
```

- [ ] Add `trailingContent` slot to `DomatPrimaryButton` (for arrow icons in onboarding):

In `DomatButton.kt`, update `DomatPrimaryButton`:

```kotlin
@Composable
fun DomatPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(DomatColors.primary),
            contentColor = colorResource(DomatColors.textPrimary),
            disabledContainerColor = colorResource(DomatColors.surfaceMuted),
            disabledContentColor = colorResource(DomatColors.textDisabled),
        ),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.titleLarge)
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}
```

- [ ] Commit: `feat(design): add primary30 color and trailingContent to DomatPrimaryButton`

---

## Task 4: Create OnboardingPresentationModule

**Files:**
- Create: `feature/onboarding/presentation/src/commonMain/kotlin/com/domatapp/feature/onboarding/presentation/di/OnboardingPresentationModule.kt`

- [ ] Create file:

```kotlin
package com.domatapp.feature.onboarding.presentation.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("com.domatapp.feature.onboarding.presentation")
class OnboardingPresentationModule
```

- [ ] Commit: `feat(onboarding): add Koin DI module`

---

## Task 5: Create MVI Contracts (commonMain)

**Files:** (21 files total — 3 per screen × 7 screens)
- Create: `feature/onboarding/presentation/src/commonMain/kotlin/com/domatapp/feature/onboarding/presentation/model/welcome/`
- Create: `feature/onboarding/presentation/src/commonMain/kotlin/com/domatapp/feature/onboarding/presentation/model/effortless/`
- Create: `feature/onboarding/presentation/src/commonMain/kotlin/com/domatapp/feature/onboarding/presentation/model/pricing/`
- Create: `feature/onboarding/presentation/src/commonMain/kotlin/com/domatapp/feature/onboarding/presentation/model/community/`
- Create: `feature/onboarding/presentation/src/commonMain/kotlin/com/domatapp/feature/onboarding/presentation/model/trust/`
- Create: `feature/onboarding/presentation/src/commonMain/kotlin/com/domatapp/feature/onboarding/presentation/model/login/`
- Create: `feature/onboarding/presentation/src/commonMain/kotlin/com/domatapp/feature/onboarding/presentation/model/location/`

Base path: `feature/onboarding/presentation/src/commonMain/kotlin/com/domatapp/feature/onboarding/presentation/model/`

### Welcome

- [ ] Create `welcome/OnboardingWelcomeUiState.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.welcome
data class OnboardingWelcomeUiState(val isLoading: Boolean = false)
```

- [ ] Create `welcome/OnboardingWelcomeIntent.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.welcome
sealed interface OnboardingWelcomeIntent {
    data object GoNext : OnboardingWelcomeIntent
}
```

- [ ] Create `welcome/OnboardingWelcomeEffect.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.welcome
sealed interface OnboardingWelcomeEffect {
    data object NavigateToEffortless : OnboardingWelcomeEffect
}
```

### Effortless

- [ ] Create `effortless/OnboardingEffortlessUiState.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.effortless
data class OnboardingEffortlessUiState(val isLoading: Boolean = false)
```

- [ ] Create `effortless/OnboardingEffortlessIntent.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.effortless
sealed interface OnboardingEffortlessIntent {
    data object GoNext : OnboardingEffortlessIntent
    data object GoBack : OnboardingEffortlessIntent
}
```

- [ ] Create `effortless/OnboardingEffortlessEffect.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.effortless
sealed interface OnboardingEffortlessEffect {
    data object NavigateToPricing : OnboardingEffortlessEffect
    data object NavigateBack : OnboardingEffortlessEffect
}
```

### Pricing

- [ ] Create `pricing/OnboardingPricingUiState.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.pricing
data class OnboardingPricingUiState(val isLoading: Boolean = false)
```

- [ ] Create `pricing/OnboardingPricingIntent.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.pricing
sealed interface OnboardingPricingIntent {
    data object GoNext : OnboardingPricingIntent
    data object GoBack : OnboardingPricingIntent
}
```

- [ ] Create `pricing/OnboardingPricingEffect.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.pricing
sealed interface OnboardingPricingEffect {
    data object NavigateToCommunity : OnboardingPricingEffect
    data object NavigateBack : OnboardingPricingEffect
}
```

### Community

- [ ] Create `community/OnboardingCommunityUiState.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.community
data class OnboardingCommunityUiState(val isLoading: Boolean = false)
```

- [ ] Create `community/OnboardingCommunityIntent.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.community
sealed interface OnboardingCommunityIntent {
    data object GoNext : OnboardingCommunityIntent
    data object GoBack : OnboardingCommunityIntent
}
```

- [ ] Create `community/OnboardingCommunityEffect.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.community
sealed interface OnboardingCommunityEffect {
    data object NavigateToTrust : OnboardingCommunityEffect
    data object NavigateBack : OnboardingCommunityEffect
}
```

### Trust

- [ ] Create `trust/OnboardingTrustUiState.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.trust
data class OnboardingTrustUiState(val isLoading: Boolean = false)
```

- [ ] Create `trust/OnboardingTrustIntent.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.trust
sealed interface OnboardingTrustIntent {
    data object GoNext : OnboardingTrustIntent
    data object GoBack : OnboardingTrustIntent
}
```

- [ ] Create `trust/OnboardingTrustEffect.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.trust
sealed interface OnboardingTrustEffect {
    data object NavigateToLogin : OnboardingTrustEffect
    data object NavigateBack : OnboardingTrustEffect
}
```

### Login

- [ ] Create `login/OnboardingLoginUiState.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.login
data class OnboardingLoginUiState(val isLoading: Boolean = false)
```

- [ ] Create `login/OnboardingLoginIntent.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.login
sealed interface OnboardingLoginIntent {
    data object OnGoogleSignInClicked : OnboardingLoginIntent
}
```

- [ ] Create `login/OnboardingLoginEffect.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.login
sealed interface OnboardingLoginEffect {
    data object NavigateToLocationSelection : OnboardingLoginEffect
}
```

### LocationSelection

- [ ] Create `location/LocationSelectionUiState.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.location
data class LocationSelectionUiState(
    val selectedBlock: String? = null,
    val selectedApartment: String? = null,
    val isConfirmEnabled: Boolean = false,
)
```

- [ ] Create `location/LocationSelectionIntent.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.location
sealed interface LocationSelectionIntent {
    data class SelectBlock(val block: String) : LocationSelectionIntent
    data class SelectApartment(val apartment: String) : LocationSelectionIntent
    data object Confirm : LocationSelectionIntent
    data object GoBack : LocationSelectionIntent
}
```

- [ ] Create `location/LocationSelectionEffect.kt`:
```kotlin
package com.domatapp.feature.onboarding.presentation.model.location
sealed interface LocationSelectionEffect {
    data object NavigateToHome : LocationSelectionEffect
    data object NavigateBack : LocationSelectionEffect
}
```

- [ ] Commit: `feat(onboarding): add MVI contracts for all 7 screens`

---

## Task 6: Create ViewModels (commonMain)

Base path: `feature/onboarding/presentation/src/commonMain/kotlin/com/domatapp/feature/onboarding/presentation/viewmodel/`

- [ ] Create `OnboardingWelcomeViewModel.kt`:

```kotlin
package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeEffect
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeIntent
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeUiState
import org.koin.android.annotation.KoinViewModel

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

- [ ] Create `OnboardingEffortlessViewModel.kt`:

```kotlin
package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.effortless.OnboardingEffortlessEffect
import com.domatapp.feature.onboarding.presentation.model.effortless.OnboardingEffortlessIntent
import com.domatapp.feature.onboarding.presentation.model.effortless.OnboardingEffortlessUiState
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.OnboardingRoute.Effortless::class)
@KoinViewModel
class OnboardingEffortlessViewModel : BaseViewModel<
    OnboardingEffortlessUiState,
    OnboardingEffortlessIntent,
    OnboardingEffortlessEffect
>(OnboardingEffortlessUiState()) {
    override fun onIntent(intent: OnboardingEffortlessIntent) {
        when (intent) {
            OnboardingEffortlessIntent.GoNext -> emitEffect(OnboardingEffortlessEffect.NavigateToPricing)
            OnboardingEffortlessIntent.GoBack -> emitEffect(OnboardingEffortlessEffect.NavigateBack)
        }
    }
}
```

- [ ] Create `OnboardingPricingViewModel.kt`:

```kotlin
package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.pricing.OnboardingPricingEffect
import com.domatapp.feature.onboarding.presentation.model.pricing.OnboardingPricingIntent
import com.domatapp.feature.onboarding.presentation.model.pricing.OnboardingPricingUiState
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.OnboardingRoute.Pricing::class)
@KoinViewModel
class OnboardingPricingViewModel : BaseViewModel<
    OnboardingPricingUiState,
    OnboardingPricingIntent,
    OnboardingPricingEffect
>(OnboardingPricingUiState()) {
    override fun onIntent(intent: OnboardingPricingIntent) {
        when (intent) {
            OnboardingPricingIntent.GoNext -> emitEffect(OnboardingPricingEffect.NavigateToCommunity)
            OnboardingPricingIntent.GoBack -> emitEffect(OnboardingPricingEffect.NavigateBack)
        }
    }
}
```

- [ ] Create `OnboardingCommunityViewModel.kt`:

```kotlin
package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.community.OnboardingCommunityEffect
import com.domatapp.feature.onboarding.presentation.model.community.OnboardingCommunityIntent
import com.domatapp.feature.onboarding.presentation.model.community.OnboardingCommunityUiState
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.OnboardingRoute.Community::class)
@KoinViewModel
class OnboardingCommunityViewModel : BaseViewModel<
    OnboardingCommunityUiState,
    OnboardingCommunityIntent,
    OnboardingCommunityEffect
>(OnboardingCommunityUiState()) {
    override fun onIntent(intent: OnboardingCommunityIntent) {
        when (intent) {
            OnboardingCommunityIntent.GoNext -> emitEffect(OnboardingCommunityEffect.NavigateToTrust)
            OnboardingCommunityIntent.GoBack -> emitEffect(OnboardingCommunityEffect.NavigateBack)
        }
    }
}
```

- [ ] Create `OnboardingTrustViewModel.kt`:

```kotlin
package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.trust.OnboardingTrustEffect
import com.domatapp.feature.onboarding.presentation.model.trust.OnboardingTrustIntent
import com.domatapp.feature.onboarding.presentation.model.trust.OnboardingTrustUiState
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.OnboardingRoute.Trust::class)
@KoinViewModel
class OnboardingTrustViewModel : BaseViewModel<
    OnboardingTrustUiState,
    OnboardingTrustIntent,
    OnboardingTrustEffect
>(OnboardingTrustUiState()) {
    override fun onIntent(intent: OnboardingTrustIntent) {
        when (intent) {
            OnboardingTrustIntent.GoNext -> emitEffect(OnboardingTrustEffect.NavigateToLogin)
            OnboardingTrustIntent.GoBack -> emitEffect(OnboardingTrustEffect.NavigateBack)
        }
    }
}
```

- [ ] Create `OnboardingLoginViewModel.kt`:

```kotlin
package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.login.OnboardingLoginEffect
import com.domatapp.feature.onboarding.presentation.model.login.OnboardingLoginIntent
import com.domatapp.feature.onboarding.presentation.model.login.OnboardingLoginUiState
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.OnboardingRoute.Login::class)
@KoinViewModel
class OnboardingLoginViewModel : BaseViewModel<
    OnboardingLoginUiState,
    OnboardingLoginIntent,
    OnboardingLoginEffect
>(OnboardingLoginUiState()) {
    override fun onIntent(intent: OnboardingLoginIntent) {
        when (intent) {
            OnboardingLoginIntent.OnGoogleSignInClicked ->
                emitEffect(OnboardingLoginEffect.NavigateToLocationSelection)
        }
    }
}
```

- [ ] Create `LocationSelectionViewModel.kt`:

```kotlin
package com.domatapp.feature.onboarding.presentation.viewmodel

import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationViewModel
import com.domatapp.core.presentation.base.BaseViewModel
import com.domatapp.feature.onboarding.presentation.model.location.LocationSelectionEffect
import com.domatapp.feature.onboarding.presentation.model.location.LocationSelectionIntent
import com.domatapp.feature.onboarding.presentation.model.location.LocationSelectionUiState
import org.koin.android.annotation.KoinViewModel

@NavigationViewModel(Route.OnboardingRoute.LocationSelection::class)
@KoinViewModel
class LocationSelectionViewModel : BaseViewModel<
    LocationSelectionUiState,
    LocationSelectionIntent,
    LocationSelectionEffect
>(LocationSelectionUiState()) {
    override fun onIntent(intent: LocationSelectionIntent) {
        when (intent) {
            is LocationSelectionIntent.SelectBlock ->
                updateState { it.copy(selectedBlock = intent.block, isConfirmEnabled = it.selectedApartment != null) }
            is LocationSelectionIntent.SelectApartment ->
                updateState { it.copy(selectedApartment = intent.apartment, isConfirmEnabled = it.selectedBlock != null) }
            LocationSelectionIntent.Confirm -> emitEffect(LocationSelectionEffect.NavigateToHome)
            LocationSelectionIntent.GoBack -> emitEffect(LocationSelectionEffect.NavigateBack)
        }
    }
}
```

- [ ] Commit: `feat(onboarding): add 7 ViewModels`

---

## Task 7: Copy Drawable Assets

**Source:** `/Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/`
**Destination:** `feature/onboarding/presentation/src/commonMain/composeResources/drawable/`

- [ ] Create destination directory and copy all needed assets:

```bash
mkdir -p feature/onboarding/presentation/src/commonMain/composeResources/drawable

cp /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_arrow_back.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_arrow_forward.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_arrow_forward_white.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_building.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_checkmark.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_chevron_down.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_delivery_truck_green.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_door.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_feature_location.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_feature_origin.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_feature_producer.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_google.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_leaf_badge.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_lock.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_person_community.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_person_community_white.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_pricing_consumer.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_pricing_producer.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_pricing_retail.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_pricing_wholesaler.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_shield_large.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_trending_down.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/ic_trust_wallet_badge.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/img_effortless_illustration.xml \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/img_hero_login.png \
   /Users/yusufokur/AndroidStudioProjects/CMPKMP/composeApp/src/commonMain/composeResources/drawable/img_welcome_neighborhood.png \
   feature/onboarding/presentation/src/commonMain/composeResources/drawable/
```

- [ ] Commit: `feat(onboarding): add drawable assets`

---

## Task 8: Migrate Components to core:presentation

Base path: `core/presentation/src/androidMain/kotlin/com/domatapp/core/presentation/component/`

### DomatProgressDots & DomatProgressSteps

- [ ] Create `indicator/DomatProgressIndicator.kt`:

```kotlin
package com.domatapp.core.presentation.component.indicator

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatProgressDots(
    totalDots: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalDots) { index ->
            val pillShape = RoundedCornerShape(9999.dp)
            if (index == activeIndex) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(8.dp)
                        .clip(pillShape)
                        .background(colorResource(DomatColors.primary))
                        .animateContentSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colorResource(DomatColors.borderDefault)),
                )
            }
        }
    }
}

@Composable
fun DomatProgressSteps(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val pillShape = RoundedCornerShape(9999.dp)
            when {
                index < currentStep -> Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colorResource(DomatColors.primary)),
                )
                index == currentStep -> Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(6.dp)
                        .clip(pillShape)
                        .background(colorResource(DomatColors.primary))
                        .animateContentSize(),
                )
                else -> Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colorResource(DomatColors.borderDefault)),
                )
            }
        }
    }
}
```

### DomatFeatureListItem

- [ ] Create `list/DomatFeatureListItem.kt`:

```kotlin
package com.domatapp.core.presentation.component.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatFeatureListItem(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colorResource(DomatColors.surfaceSubtle),
        border = BorderStroke(1.dp, colorResource(DomatColors.borderLight)),
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = colorResource(DomatColors.textPrimary),
            )
        }
    }
}
```

### DomatHeroBadge

- [ ] Create `badge/DomatHeroBadge.kt`:

```kotlin
package com.domatapp.core.presentation.component.badge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatHeroBadge(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(9999.dp),
        color = colorResource(DomatColors.primary20),
        border = BorderStroke(1.dp, colorResource(DomatColors.primary30)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = colorResource(DomatColors.primary),
            )
        }
    }
}
```

### DomatBottomActionBar

- [ ] Create `bar/DomatBottomActionBar.kt`:

```kotlin
package com.domatapp.core.presentation.component.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatBottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colorResource(DomatColors.borderLight))
            .background(colorResource(DomatColors.surfaceDefault))
            .padding(start = 16.dp, end = 16.dp, top = 17.dp, bottom = 32.dp),
    ) {
        content()
    }
}
```

### DomatScreenHeader

- [ ] Create `header/DomatScreenHeader.kt`:

```kotlin
package com.domatapp.core.presentation.component.header

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource
import domatapp.feature.onboarding.presentation.generated.resources.Res
import domatapp.feature.onboarding.presentation.generated.resources.ic_arrow_back
import org.jetbrains.compose.resources.painterResource

@Composable
fun DomatScreenHeader(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    bottomContent: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(DomatColors.surfaceDefault))
            .border(width = 1.dp, color = colorResource(DomatColors.borderLight)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = "Geri",
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = colorResource(DomatColors.textPrimary),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 48.dp),
            )
        }
        bottomContent?.invoke()
    }
}
```

### DomatLocationCard & DomatLocationCardConnector

- [ ] Create `card/DomatLocationCard.kt` (add to existing file alongside `DomatProductCard`):

> **Note:** This is a NEW file, not the existing `DomatCard.kt`

```kotlin
package com.domatapp.core.presentation.component.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource
import domatapp.feature.onboarding.presentation.generated.resources.Res
import domatapp.feature.onboarding.presentation.generated.resources.ic_checkmark
import domatapp.feature.onboarding.presentation.generated.resources.ic_lock
import org.jetbrains.compose.resources.painterResource

@Composable
fun DomatLocationCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isLocked: Boolean = true,
    cardAlpha: Float = 1f,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(DomatColors.surfaceSubtle))
            .padding(17.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colorResource(DomatColors.primary20)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_checkmark),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = colorResource(DomatColors.textTertiary),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = colorResource(DomatColors.textPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isLocked) {
            Image(
                painter = painterResource(Res.drawable.ic_lock),
                contentDescription = null,
                modifier = Modifier.size(width = 13.dp, height = 17.dp),
            )
        }
    }
}

@Composable
fun DomatLocationCardConnector(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 36.dp)
            .width(2.dp)
            .height(24.dp)
            .background(colorResource(DomatColors.borderDefault)),
    )
}
```

### DomatInputDropdown

- [ ] Create `input/DomatInputDropdown.kt`:

```kotlin
package com.domatapp.core.presentation.component.input

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource
import domatapp.feature.onboarding.presentation.generated.resources.Res
import domatapp.feature.onboarding.presentation.generated.resources.ic_checkmark
import domatapp.feature.onboarding.presentation.generated.resources.ic_chevron_down
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun DomatInputDropdown(
    label: String,
    value: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isOpen: Boolean = false,
    items: List<String> = emptyList(),
    selectedItem: String? = null,
    onItemSelected: (String) -> Unit = {},
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isOpen) 180f else 0f,
        label = "chevronRotation",
    )

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = colorResource(DomatColors.surfaceDefault),
            border = BorderStroke(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) colorResource(DomatColors.primary) else colorResource(DomatColors.borderDefault),
            ),
            shadowElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(if (isActive) 18.dp else 17.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        colorFilter = ColorFilter.tint(
                            if (isActive) colorResource(DomatColors.primary) else colorResource(DomatColors.textTertiary),
                        ),
                    )
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorResource(DomatColors.textTertiary),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorResource(DomatColors.textPrimary),
                        modifier = Modifier.weight(1f),
                    )
                    Image(
                        painter = painterResource(Res.drawable.ic_chevron_down),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp).rotate(chevronRotation),
                        colorFilter = ColorFilter.tint(
                            if (isActive) colorResource(DomatColors.primary) else colorResource(DomatColors.textMuted),
                        ),
                    )
                }
            }
        }

        if (isOpen && items.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
                    .padding(top = 88.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f),
                    ),
                shape = RoundedCornerShape(12.dp),
                color = colorResource(DomatColors.surfaceDefault),
                border = BorderStroke(2.dp, colorResource(DomatColors.primary)),
            ) {
                Column(modifier = Modifier.padding(2.dp)) {
                    items.forEachIndexed { index, item ->
                        val isSelected = item == selectedItem
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) colorResource(DomatColors.primary10)
                                    else colorResource(DomatColors.surfaceDefault),
                                )
                                .clickable { onItemSelected(item) }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorResource(DomatColors.textPrimary),
                            )
                            if (isSelected) {
                                Image(
                                    painter = painterResource(Res.drawable.ic_checkmark),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(colorResource(DomatColors.primary)),
                                )
                            }
                        }
                        if (index < items.lastIndex) {
                            HorizontalDivider(color = colorResource(DomatColors.borderLight), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}
```

### DomatGoogleSignInButton

- [ ] Create `button/DomatGoogleSignInButton.kt`:

```kotlin
package com.domatapp.core.presentation.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun DomatGoogleSignInButton(
    onClick: () -> Unit,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = colorResource(DomatColors.surfaceDefault),
        border = BorderStroke(1.dp, colorResource(DomatColors.borderDefault)),
        shadowElevation = 1.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = iconPainter,
                contentDescription = "Google",
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp).size(24.dp),
            )
            Text(
                text = "Google ile Devam Et",
                style = MaterialTheme.typography.titleMedium,
                color = colorResource(DomatColors.textPrimary),
            )
        }
    }
}
```

> **Note:** `DomatInputDropdown`, `DomatLocationCard`, `DomatScreenHeader` reference `domatapp.feature.onboarding.presentation.generated.resources.Res` for icons — these components live in `core:presentation` but use resources from the feature module. This creates a dependency. Alternative: pass icon painters as parameters. **Preferred fix**: pass icon `Painter` as parameter for `ic_arrow_back`, `ic_checkmark`, `ic_lock`, `ic_chevron_down` so `core:presentation` stays independent. The callers (screens) provide the painters.
>
> **Updated signature for DomatScreenHeader**: Add `backIconPainter: Painter` parameter.
> **Updated signature for DomatLocationCard**: Add `checkmarkPainter: Painter` and `lockPainter: Painter?` parameters.
> **Updated signature for DomatInputDropdown**: Add `chevronPainter: Painter` and `checkmarkPainter: Painter` parameters.

- [ ] Commit: `feat(presentation): add onboarding reusable components`

---

## Task 9: Create Screens — Welcome, Effortless, Pricing (androidMain)

Base path: `feature/onboarding/presentation/src/androidMain/kotlin/com/domatapp/feature/onboarding/presentation/screen/`

Res import: `domatapp.feature.onboarding.presentation.generated.resources.Res`

- [ ] Create `OnboardingWelcomeScreen.kt`:

```kotlin
package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatColors
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.core.presentation.component.button.DomatPrimaryButton
import com.domatapp.core.presentation.component.indicator.DomatProgressDots
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeIntent
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeUiState
import dev.icerock.moko.resources.compose.colorResource
import domatapp.feature.onboarding.presentation.generated.resources.Res
import domatapp.feature.onboarding.presentation.generated.resources.ic_arrow_forward
import domatapp.feature.onboarding.presentation.generated.resources.img_welcome_neighborhood
import org.jetbrains.compose.resources.painterResource

@NavigationScreen(Route.OnboardingRoute.Welcome::class)
@Composable
fun OnboardingWelcomeScreen(
    uiState: OnboardingWelcomeUiState,
    onIntent: (OnboardingWelcomeIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(DomatColors.surfaceDefault))
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(DomatColors.primary10)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.img_welcome_neighborhood),
                contentDescription = "Mahalle görseli",
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = buildAnnotatedString {
                append("Hoş Geldiniz 👋\n")
                withStyle(SpanStyle(color = colorResource(DomatColors.primary))) {
                    append("Taze sebze ve meyveler")
                }
                append("\nmahallenize geliyor")
            },
            style = MaterialTheme.typography.displayMedium,
            color = colorResource(DomatColors.textPrimary),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Her hafta en taze domatesleri doğrudan\nüreticiden sitenize getiriyoruz. Stres yok,\nmarket gezmek yok, sürpriz yok.",
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(DomatColors.textPrimary),
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            DomatProgressDots(totalDots = 3, activeIndex = 0)
            DomatPrimaryButton(
                text = "Devam Et",
                onClick = { onIntent(OnboardingWelcomeIntent.GoNext) },
                trailingContent = {
                    Image(
                        painter = painterResource(Res.drawable.ic_arrow_forward),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}
```

- [ ] Create `OnboardingWelcomeEffectHandler.kt`:

```kotlin
package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.feature.onboarding.presentation.model.welcome.OnboardingWelcomeEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@NavigationEffectHandler(Route.OnboardingRoute.Welcome::class)
@Composable
fun OnboardingWelcomeEffectHandler(effectFlow: Flow<OnboardingWelcomeEffect>) {
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

- [ ] Create `OnboardingEffortlessScreen.kt` (adapted from CMPKMP — replace `AppColors` with `DomatColors`, `@Screen` with `@NavigationScreen`, use `DomatProgressDots`, `DomatPrimaryButton`)

- [ ] Create `OnboardingEffortlessEffectHandler.kt` (same pattern as Welcome, effect: `NavigateToPricing` → `navigator.navigate(Route.OnboardingRoute.Pricing)`)

- [ ] Create `OnboardingPricingScreen.kt` (adapted — contains private `SupplyChainRow` composable, stays in this file)

- [ ] Create `OnboardingPricingEffectHandler.kt` (effect: `NavigateToCommunity` → `navigator.navigate(Route.OnboardingRoute.Community)`)

- [ ] Commit: `feat(onboarding): add Welcome, Effortless, Pricing screens`

---

## Task 10: Create Screens — Community, Trust, Login (androidMain)

- [ ] Create `OnboardingCommunityScreen.kt` (adapted — contains private `CommunityHeroCard`, `OverlappingAvatars`, `TruckWithPriceIndicator`, `PersonAvatarCircle` composables)

- [ ] Create `OnboardingCommunityEffectHandler.kt` (effect: `NavigateToTrust` → `navigator.navigate(Route.OnboardingRoute.Trust)`)

- [ ] Create `OnboardingTrustScreen.kt` (adapted — uses `DomatFeatureListItem` from core:presentation, `DomatProgressDots`, `DomatPrimaryButton`)

- [ ] Create `OnboardingTrustEffectHandler.kt` (effect: `NavigateToLogin` → `navigator.navigate(Route.OnboardingRoute.Login)`)

- [ ] Create `OnboardingLoginScreen.kt` (adapted — uses `DomatGoogleSignInButton`, `DomatHeroBadge`)

- [ ] Create `OnboardingLoginEffectHandler.kt` (effect: `NavigateToLocationSelection` → `navigator.navigate(Route.OnboardingRoute.LocationSelection)`)

- [ ] Commit: `feat(onboarding): add Community, Trust, Login screens`

---

## Task 11: Create LocationSelectionScreen (androidMain)

- [ ] Create `LocationSelectionScreen.kt` (adapted — uses `DomatScreenHeader`, `DomatProgressSteps`, `DomatLocationCard`, `DomatLocationCardConnector`, `DomatInputDropdown`, `DomatBottomActionBar`, `DomatPrimaryButton`; local state moved to ViewModel intent)

- [ ] Create `LocationSelectionEffectHandler.kt`:

```kotlin
@NavigationEffectHandler(Route.OnboardingRoute.LocationSelection::class)
@Composable
fun LocationSelectionEffectHandler(effectFlow: Flow<LocationSelectionEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                LocationSelectionEffect.NavigateToHome -> navigator.replaceAll(Route.Main.Home)
                LocationSelectionEffect.NavigateBack -> navigator.popBack()
            }
        }
    }
}
```

- [ ] Commit: `feat(onboarding): add LocationSelection screen`

---

## Task 12: Register Onboarding in composeApp

**Files:**
- Modify: `composeApp/src/main/kotlin/com/domatapp/app/MainActivity.kt`
- Modify: `composeApp/src/main/kotlin/com/domatapp/app/MainViewModel.kt` (or wherever Koin is initialized — add `OnboardingPresentationModule`)
- Modify: `shared/src/commonMain/kotlin/.../di/KoinInitializer.kt` (add OnboardingPresentationModule to Koin setup)

- [ ] In `MainActivity.kt`, add `onboardingPresentationEntries()` to `entryProvider`:

```kotlin
entryProvider = entryProvider {
    authPageEntry()
    onboardingPresentationEntries()
    homeEntries()
}
```

- [ ] Add import: `import com.domatapp.feature.onboarding.presentation.navigation.onboardingPresentationEntries`

- [ ] Find KoinInitializer.kt and add `OnboardingPresentationModule` to modules list

- [ ] Update starting route to `Route.OnboardingRoute.Welcome` if needed (check `MainViewModel` initial backStack)

- [ ] Add `implementation(projects.feature.onboarding.presentation)` to `composeApp/build.gradle.kts`

- [ ] Commit: `feat(app): register onboarding navigation entries`

---

## Task 13: Build & Fix

- [ ] Run build:
```bash
./gradlew :composeApp:assembleDebug 2>&1 | grep -E "^(e:|error:|FAILED|BUILD)"
```

- [ ] Fix any compilation errors (missing imports, type mismatches, unresolved references)

- [ ] Re-run build until `BUILD SUCCESSFUL`

- [ ] Final commit: `fix(onboarding): resolve build errors`
