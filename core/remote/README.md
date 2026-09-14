# Module: :core:remote

## 🎯 Purpose (Amaç)
Ağ katmanı işlemlerini (Ktor/Ktorfit üzerinden REST API çağrıları) yönetir. Verilerin uzak sunuculardan getirilmesinden sorumludur.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** Remote DataSource, HTTP Client (Ktor)

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:remote]
     |
     +--> [:core:resulting]
     +--> [Ktor Client]
     +--> [Ktorfit]
     v
[Koin Core/Annotations]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- API endpoint tanımları ve Ktor konfigürasyonları buradadır.
- REST `*RemoteSource` arayüzlerinin implementasyonları Ktorfit KSP tarafından üretilir.
