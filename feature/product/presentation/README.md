# Module: :feature:product:presentation

## 🎯 Purpose (Amaç)
Ürün listesi ve detay ekranlarının UI mantığını barındırır.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Presentation
- **Patterns:** MVI

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:product:presentation]
                |
                +--> [:feature:product:domain]
                +--> [:core:common]
                v
          [:core:navigation]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Filtreleme ve arama durumlarını yönetir.
