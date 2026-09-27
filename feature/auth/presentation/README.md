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
- ViewModels `BaseViewModel`'dan türetilir; UI State'ler StateFlow olarak dışarı açılır.
- Ekranlar (`LoginScreen`, `LocationSelectionScreen`) ve binding'leri `commonMain`'de — Android ve
  iOS aynı Compose kodunu çalıştırır.
- Platforma özgü tek yüzey Google hesap seçici: `GoogleSignIn.kt` içindeki
  `expect suspend fun requestGoogleIdToken`. `androidMain` actual'ı Credential Manager'ı çağırır;
  `iosMain` actual'ı ise Swift'in `GoogleSignInBridge`'e kaydettiği `GoogleSignInPresenter`'ı
  bekler — GoogleSignIn-iOS bir Swift paketi olduğu için yön tersine dönüyor.
