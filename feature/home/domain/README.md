# Module: :feature:home:domain

## 🎯 Purpose (Amaç)
Ana ekran (Home/Dashboard) için gerekli iş mantığını içerir. Dashboard verileri, özet bilgiler vb. UseCase'leri barındırır.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Domain
- **Patterns:** Clean Architecture, UseCases, Repository Interfaces

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:home:domain]
          |
          v
    [:core:domain]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Saf Kotlin modülüdür.
- Sadece `core:domain` modülüne bağımlıdır.
