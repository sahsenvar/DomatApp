# Module: :feature:onboarding:presentation

## 🎯 Purpose (Amaç)
Onboarding ekranlarının UI mantığını barındırır.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Presentation
- **Patterns:** MVI

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:onboarding:presentation]
                   |
                   +--> [:feature:onboarding:domain]
                   +--> [:core:common]
                   v
             [:core:navigation]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Adım bazlı geçişleri yönetir.
