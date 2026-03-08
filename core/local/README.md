# Module: :core:local

## 🎯 Purpose (Amaç)
Cihaz üzerindeki yerel depolama işlemlerini (SQLite/SQLDelight, DataStore, Multiplatform Settings) yönetir. Verilerin kalıcı olarak saklanmasından sorumludur.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** Local DataSource, Database Drivers

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:local]
     |
     +--> [:core:serialization]
     +--> [:core:resulting]
     +--> [SQLDelight]
     +--> [DataStore]
     v
[Multiplatform Settings]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- SQLDelight şemaları ve DataStore tanımları burada yapılır.
- Platforma özgü Driver konfigürasyonları `androidMain` ve `iosMain` içindedir.
