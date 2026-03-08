# Module: :feature:product:data

## 🎯 Purpose (Amaç)
Ürün verilerini servislerden ve yerel depolamadan getirir.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Data
- **Patterns:** Repository Implementation

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:product:data]
             |
             +--> [:feature:product:domain]
             +--> [:core:remote]
             +--> [:core:local]
             v
       [:core:data]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- KMP uyumlu servis entegrasyonu sağlar.
