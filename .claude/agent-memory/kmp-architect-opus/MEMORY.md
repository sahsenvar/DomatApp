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
