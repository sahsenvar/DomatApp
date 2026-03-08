# Module: :feature:onboarding:data

## 🎯 Purpose (Amaç)
Onboarding verilerini ve kullanıcının onboarding durumunu (tamamlandı/tamamlanmadı) yönetir.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Data
- **Patterns:** Repository Implementation

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:onboarding:data]
               |
               +--> [:feature:onboarding:domain]
               +--> [:core:remote]
               +--> [:core:local]
               v
         [:core:data]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Kullanıcının onboarding durumunu `core:local` (DataStore/Settings) üzerinde saklar.
