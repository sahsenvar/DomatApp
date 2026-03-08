# Module: :core:presentation

## 🎯 Purpose (Amaç)
UI mantığı için temel yapı taşlarını (BaseViewModel, UI State yapıları, Flow yardımcıları) içerir. Hem Compose hem de SwiftUI'ın tüketebileceği ortak sunum katmanı araçlarını barındırır.

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
- iOS için Flow'ları kolay tüketilmesini sağlayan araçlar burada bulunur.
