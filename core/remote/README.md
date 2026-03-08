# Module: :core:remote

## 🎯 Purpose (Amaç)
Ağ katmanı işlemlerini (API çağrıları, Firebase Firestore/Remote Config) yönetir. Verilerin uzak sunuculardan getirilmesinden sorumludur.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** Remote DataSource, HTTP Client (Ktor)

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:remote]
     |
     +--> [:core:resulting]
     +--> [:core:serialization]
     +--> [Ktor Client]
     +--> [Firebase Firestore/Config]
     v
[Koin Core/Annotations]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- API endpoint tanımları ve Ktor konfigürasyonları buradadır.
- Firebase implementasyonları `RemoteDataSource` arayüzlerini gerçekleştirir.
