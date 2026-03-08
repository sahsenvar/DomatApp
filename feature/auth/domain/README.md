# Module: :feature:auth:domain

## 🎯 Purpose (Amaç)
Kimlik doğrulama (Authentication) işlemleri için iş mantığını içerir. Login, Register, Logout gibi UseCase'leri ve kullanıcı modellerini barındırır.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Domain
- **Patterns:** Clean Architecture, UseCases, Repository Interfaces

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:auth:domain]
          |
          v
    [:core:domain]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- 100% saf Kotlin. Hiçbir framework (Firebase, Ktor vb.) bağımlılığı olmamalıdır.
- Sadece `core:domain` modülüne bağımlıdır.
