# Module: :core:data

## 🎯 Purpose (Amaç)
Global veri eşleyicileri (mappers), temel repository implementasyonları ve veri katmanı için ortak altyapı sağlar. Özellik modüllerindeki veri katmanları buradaki yapıları miras alabilir.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer (Core Data)
- **Patterns:** Repository Pattern, Data Mappers

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:data]
     |
     +--> [:core:domain]
     v
[Koin Core]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Veri modelleri ile domain modelleri arasındaki dönüşümler için ortak Mapper arayüzleri burada bulunur.
- DataSource soyutlamaları için temel sınıflar sağlar.
