# Gemini CLI Developer Persona & Project Guidelines

This file contains the core architectural decisions, developer preferences, and established conventions for the `DomatApp` project. As a Gemini CLI agent, you MUST read and strictly adhere to these guidelines during all sessions.

## 🧑‍💻 Developer Persona & Communication Style

*   **Mindset:** The user is a senior-level developer building a highly scalable, enterprise-grade application. They think in terms of Clean Architecture, strict modularization, and long-term maintainability.
*   **Proactive Feedback (Crucial):** If the user proposes a technical decision or architecture that you believe is a "bad practice," an anti-pattern, or could cause issues later on, **DO NOT silently execute it.** You MUST stop, warn the user, explain *why* it might be problematic, and propose a better alternative. Wait for their confirmation or discussion before proceeding.
*   **Tone:** Professional, direct, and collaborative. Avoid unnecessary filler words. Focus on technical reasoning and architectural impact.
*   **Efficiency:** The user prefers DRY (Don't Repeat Yourself) principles, evident in their request for `build-logic` convention plugins to manage repetitive Gradle scripts.

## 🏗️ Project Architecture & Tech Stack

The project is a **Kotlin Multiplatform (KMP)** application designed with a strict **Feature-Based Modularization** and **Clean Architecture** approach.

### 1. UI Layer Strategy (The Most Important Rule)
*   **Android:** Jetpack Compose (located in the `composeApp` module).
*   **iOS:** **100% Native SwiftUI.**
*   **Framework Implication:** Do NOT generate iOS frameworks (`binaries.framework`) for individual KMP modules. Only the `shared` module is compiled into an "Umbrella Framework" (`Shared.framework`) that the Xcode project consumes. The KMP `composeApp` module has NO iOS targets; it is strictly an Android application module.

### 2. Modularization Strategy (Tree Structure)
The project is divided into specialized layers:

```text
DomatApp/
├── composeApp/            (Android application entry point & Compose UI)
├── shared/                (Umbrella Framework exported to iOS)
├── core/                  (Infrastructure Layer)
│   ├── common/            (Shared utilities, formatters, extensions)
│   ├── data/              (Global data mappers, base repository implementations)
│   ├── domain/            (Global domain entities and shared base UseCases)
│   ├── local/             (Local storage: Room, SQLDelight, DataStore)
│   ├── localization/      (Assets and strings/translations)
│   ├── navigation/        (Route definitions and navigation interfaces)
│   ├── remote/            (Network layer: Ktor, Interceptors)
│   └── resource/          (Assets, fonts, icons)
├── feature/               (Business Logic Layer)
│   ├── auth/              (Authentication feature)
│   ├── home/              (Home screen/dashboard feature)
│   ├── notification/      (Push notifications feature)
│   ├── onboarding/        (User onboarding feature)
│   ├── product/           (Product catalog/details feature)
│   ├── profile/           (User profile feature)
│   └── wallet/            (User wallet/payment feature)
│       ├── data/          (Implements domain interfaces, handles API/DB)
│       ├── domain/        (100% pure Kotlin. UseCases, Models, Repository Interfaces)
│       └── presentation/  (UI Logic: ViewModels, StateFlow, MVI intents)
└── build-logic/           (Composite build for convention plugins)
```

**Responsibilities & Dependencies:**
*   **`:composeApp`**: The Android application entry point and Android-specific Compose UI.
*   **`:shared`**: The Umbrella Framework exported to iOS. It depends on core and feature modules.
*   **`:core:*` (Infrastructure)**: Provides building blocks. E.g., `local` handles DB, `remote` handles API, `common` handles utils.
*   **`:feature:*` (Business Logic)**:
    Features are split into strict Clean Architecture layers:
    *   **`domain`**: 100% pure Kotlin. Contains UseCases, Models, Interfaces. Depends ONLY on `:core:domain`.
    *   **`data`**: Implements domain interfaces. Handles API calls/DB queries. Depends on `domain`, `:core:remote`, `:core:local`, `:core:data`.
    *   **`presentation`**: Contains UI Logic (ViewModels, StateFlow). Used by both Android Compose and iOS SwiftUI. Depends ONLY on `domain` and `:core:common`/`:core:navigation`.

### 3. Build & CI/CD Strategy
*   **Version Catalog:** The project uses a single, centralized `gradle/libs.versions.toml` file. Avoid splitting catalogs (e.g., separate files for kmp, android, ios) as it causes dependency synchronization issues. Use prefixes (e.g., `androidx-`, `kotlin-`) to organize within the single file.
*   **Convention Plugins (`:build-logic`):** The project uses a Composite Build (`build-logic` folder) to enforce consistent Gradle configurations.
    *   All new KMP library modules MUST use the custom convention plugin: `plugins { alias(libs.plugins.domatapp.kmp.library) }` instead of manually applying standard KMP and Android Library plugins.
    *   The `build-logic` contains a `buildSrc` directory for managing precompiled script plugins, dependency definitions, or common build functions.
*   **Testing:** Test source sets (`commonTest`, `androidInstrumentedTest`, etc.) are currently disabled/removed across the core and feature modules. Do not automatically generate test files or test dependencies unless explicitly requested.

### 4. Data Layer Strategy (DataSource Pattern)
To ensure long-term maintainability and easy backend transitions:
*   **Repository Orchestration:** Repositories in the `data` layer MUST NOT contain implementation details of specific SDKs (like Firebase or Ktor). They should only orchestrate between `RemoteDataSource` and `LocalDataSource`.
*   **DataSource Abstraction:** Always define `RemoteDataSource` and `LocalDataSource` interfaces.
*   **Firebase Integration:** For the initial phase, **Firebase Authentication (GitLive KMP SDK)** is used as the backend. Implementation details MUST be encapsulated within `FirebaseAuthRemoteDataSource`.
*   **KMP Purity:** DataSources and Repositories in the KMP `data` modules MUST remain platform-independent. Avoid enjecting `Context` or any Android/iOS specific classes.

## 🛠️ Operating Instructions for Gemini CLI

When interacting with this workspace:
1.  **Respect the Boundaries:** If modifying a feature, ensure dependencies do not leak. (e.g., a `presentation` module must never see a `data` module directly).
2.  **SwiftUI Awareness:** When writing ViewModels in the `presentation` layer, ensure the exported flows (StateFlow/SharedFlow) are easily consumable by Swift/Combine. Avoid relying on Compose-specific libraries (like `androidx.compose.runtime.State`) in the KMP `presentation` or `shared` modules.
3.  **Use the Convention Plugin:** If asked to create a new module, always use the `domatapp.kmp.library` plugin. Do **NOT** manually add the `androidLibrary` block or define `namespace`, `compileSdk`, and `minSdk`. The convention plugin dynamically generates the namespace based on the module's path (e.g., `:feature:auth:data` becomes `com.domatapp.feature.auth.data`) and sets SDK versions automatically.
4.  **Continuous Learning (Self-Update):** If you (the Gemini CLI) learn new architectural constraints, tech stack changes, or specific user preferences during future sessions that are valuable for long-term project success, you MUST autonomously append those learnings to this `GEMINI.md` file. Keep this document as the living single source of truth.

core:remote retorfit vs.
data: repository implementasyonlar + datasource
domain -> Usecase repository interfaceleri
feature:chat -> UI + VM + UiState