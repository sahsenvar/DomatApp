# Module: :feature:wallet:presentation

## 🎯 Purpose (Amaç)
Cüzdan ekranının UI mantığını barındırır.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Presentation
- **Patterns:** MVI

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:wallet:presentation]
               |
               +--> [:feature:wallet:domain]
               +--> [:core:common]
               v
         [:core:navigation]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Bakiye ve işlem geçmişinin gösterimini yönetir.
