# Module: :feature:auth:data

## 🎯 Purpose (Amaç)
Kimlik doğrulama işlemleri için veri kaynaklarını (Firebase Auth, Remote API) yönetir ve domain katmanındaki repository arayüzlerini gerçekleştirir.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Data
- **Patterns:** Repository Implementation, Remote/Local DataSource

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:auth:data]
          |
          +--> [:feature:auth:domain]
          +--> [:core:remote]
          +--> [:core:local]
          v
    [:core:data]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Firebase Auth entegrasyonu `FirebaseAuthRemoteDataSource` içinde kapsüllenmiştir.
- Domain katmanına veri sağlarken Mapper'lar üzerinden dönüşüm yapılmalıdır.
