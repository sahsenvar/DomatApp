# Module: :core:presentation

## 🎯 Purpose (Amaç)
UI mantığı için temel yapı taşlarını (BaseViewModel, UI State yapıları) ve paylaşılan Compose
bileşenlerini içerir. Uygulamanın tek `@ScreenWrapper`'ı olan `DomatScreenRoot` da burada:
ViewModel'i çözen, state'i toplayan ve yan-etki politikasını belirleyen yer.

Artık tamamı `commonMain` — bileşenler de, `DomatScreenRoot` da Android ve iOS'ta aynı kod.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** MVI, Base ViewModel (Moko MVVM)

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:presentation]
     |
     +--> [:core:domain]
     +--> [:core:common]
     +--> [:core:navigation]
     v
[Moko MVVM Core/Flow]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- ViewModels bu modüldeki `BaseViewModel` yapısını miras almalıdır.
- `PlatformContext` (expect/abstract class) OS-modal API'lerin ihtiyaç duyduğu host handle'ı: Android'de
  `android.content.Context` (yani Activity), iOS'ta boş bir marker. `DomatEffectScope`'un
  `commonMain`'e taşınabilmesini sağlayan şey bu.
- Gezgin'in JetBrains Navigation 3 / lifecycle bağımlılıkları tüketicinin `commonMain`'ine
  ulaşmaz, bu yüzden `lifecycle-viewmodel-compose` ve `lifecycle-runtime-compose` burada
  açıkça bildirilir.
