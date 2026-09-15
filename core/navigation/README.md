# Module: :core:navigation

## 🎯 Purpose (Amaç)

Uygulamanın **navigasyon grafiğini** barındırır: `@NavGraph` ile işaretlenmiş sealed interface
ağacı ve her route'un `@GoTo` / `@ReplaceTo` / `@BackTo` kenarları. Gezgin'in KSP processor'ı bu
grafikten `gezginTopology`, `gezginJson`, route serializer'ları ve her route için tipli bir
`<X>Navigator` üretir.

## 🏗️ Architecture (Mimari)

- **Layer:** Infrastructure Layer
- **Library:** [Gezgin](https://github.com/sahsenvar/Gezgin) (Android'de AndroidX Navigation 3,
  iOS'ta JetBrains Navigation 3 üzerinde)
- **Patterns:** Declared-edge graph, typed per-route navigator

Kenar bildirilmemiş bir hedefe gitmek **derlenmez**: bir ekran yalnızca kendi route'unun bildirdiği
kenarlar kadar metoda sahiptir.

## 🔗 Dependencies (Bağımlılıklar)

```text
[:core:navigation]  (commonMain)
     |
     +--> [io.github.sahsenvar:gezgin-core]  (api)
     |         |
     |         +--> [androidx.navigation3]            (androidMain)
     |         +--> [org.jetbrains.androidx.navigation3]  (iosMain)
     v
[Kotlin Stdlib]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)

- Grafik **`src/commonMain`** içindedir. `gezgin-core` artık `iosArm64` ve `iosSimulatorArm64`
  klib'leri de yayınlıyor, bu yüzden grafik, üretilen navigator'lar ve onları render eden ekranlar
  tek bir yerde derleniyor. **`iosX64` yok** — JetBrains `navigation3-ui` onu yayınlamıyor, yani
  Intel Mac simülatörü desteklenmiyor.
- Eski SwiftUI `NavigationRouter.swift` / `AppRoute` enum'u **silindi**; iOS artık bu grafiği
  `ComposeUIViewController` içinden sürüyor.
- Gezgin processor'ı `kspAndroid` ile değil **`kspCommonMainMetadata`** ile kayıtlı: ürettiği kod
  platformdan bağımsız, bir kez koşuyor ve çıktısı `commonMain`'e ekleniyor.
- Route'lara `@Serializable` **yazılmaz** — Gezgin serializer'ları kendisi üretir. Yalnızca route
  parametresi olarak kullanılan proje tipleri `@Serializable` ister.
- Bu modül Compose compiler plugin'i **uygulamaz** ve uygulamamalıdır; grafik modülünde bir
  `@Composable` üretilirse lowering'siz bytecode oluşur.
- Yeni bir ekran eklerken: route'u buraya, `@Screen`/`@ViewModelOf`/`@Effects` sağlayıcılarını
  ilgili feature presentation modülüne yaz. Ayrıntılar için `CLAUDE.md` → *Navigation (Gezgin)*.
