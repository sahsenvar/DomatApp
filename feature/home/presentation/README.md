# Module: :feature:home:presentation

## 🎯 Purpose (Amaç)
Ana ekranın UI mantığını ve ortak UI bileşenlerini barındırır.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Presentation
- **Patterns:** MVI, ViewModels

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:home:presentation]
               |
               +--> [:feature:home:domain]
               +--> [:core:common]
               v
         [:core:navigation]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- ViewModels, StateFlow ve Intent yapısını kullanır.
- UI katmanı platformdan bağımsız (Compose/SwiftUI) veri modelleri ile beslenir.
