# Module: :feature:auth:presentation

## 🎯 Purpose (Amaç)
Giriş yap, kayıt ol gibi ekranların UI mantığını (ViewModels) ve ortak UI bileşenlerini barındırır.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Presentation
- **Patterns:** MVI (Model-View-Intent), ViewModels

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:auth:presentation]
               |
               +--> [:feature:auth:domain]
               +--> [:core:presentation]
               +--> [:core:common]
               +--> [:core:resulting]
               v
         [:core:navigation]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- ViewModels, Moko MVVM kullanarak `BaseViewModel`'dan türetilir.
- UI State'ler StateFlow olarak dışarı açılır (Android/iOS uyumlu).
