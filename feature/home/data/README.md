# Module: :feature:home:data

## 🎯 Purpose (Amaç)
Ana ekran için veri sağlayan servisleri (Remote API, Local DB) yönetir ve home domain repository'lerini gerçekleştirir.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Data
- **Patterns:** Repository Implementation, DataSources

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:home:data]
          |
          +--> [:feature:home:domain]
          +--> [:core:remote]
          +--> [:core:local]
          v
    [:core:data]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- API çağrıları `core:remote` üzerinden yapılır.
- Veri dönüşümleri için Mapper'lar kullanılır.
