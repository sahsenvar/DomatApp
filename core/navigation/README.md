# Module: :core:navigation

## 🎯 Purpose (Amaç)

Uygulamanın **navigasyon grafiğini** barındırır: `@NavGraph` ile işaretlenmiş sealed interface
ağacı ve her route'un `@GoTo` / `@ReplaceTo` / `@BackTo` kenarları. Gezgin'in KSP processor'ı bu
grafikten `gezginTopology`, `gezginJson`, route serializer'ları ve her route için tipli bir
`<X>Navigator` üretir.

## 🏗️ Architecture (Mimari)

- **Layer:** Infrastructure Layer
- **Library:** [Gezgin](https://github.com/sahsenvar/Gezgin) (AndroidX Navigation 3 üzerinde)
- **Patterns:** Declared-edge graph, typed per-route navigator

Kenar bildirilmemiş bir hedefe gitmek **derlenmez**: bir ekran yalnızca kendi route'unun bildirdiği
kenarlar kadar metoda sahiptir.

## 🔗 Dependencies (Bağımlılıklar)

```text
[:core:navigation]  (androidMain only)
     |
     +--> [io.github.sahsenvar:gezgin-core]  (api)
     |         |
     |         +--> [androidx.navigation3]
     v
[Kotlin Stdlib]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)

- Grafik **`src/androidMain`** içindedir. `gezgin-core` yalnızca `android` + `jvm` yayınlar, iOS
  klib'i **yoktur**. iOS tarafı kendi SwiftUI `NavigationStack`'ini
  `iosApp/.../NavigationRouter.swift` içindeki Swift `AppRoute` enum'u ile sürer ve buradaki
  Kotlin tiplerini tüketmez.
- Route'lara `@Serializable` **yazılmaz** — Gezgin serializer'ları kendisi üretir. Yalnızca route
  parametresi olarak kullanılan proje tipleri `@Serializable` ister.
- Bu modül Compose compiler plugin'i **uygulamaz** ve uygulamamalıdır; grafik modülünde bir
  `@Composable` üretilirse lowering'siz bytecode oluşur.
- Yeni bir ekran eklerken: route'u buraya, `@Screen`/`@ViewModelOf`/`@Effects` sağlayıcılarını
  ilgili feature presentation modülüne yaz. Ayrıntılar için `CLAUDE.md` → *Navigation (Gezgin)*.
