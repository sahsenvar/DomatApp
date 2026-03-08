# Module: :core:serialization

## 🎯 Purpose (Amaç)
Global JSON serileştirme ve deserileştirme konfigürasyonlarını içerir. Verilerin transfer formatları için ortak ayarları sağlar.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** JSON Configuration

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:serialization]
     |
     +--> [:core:resulting]
     v
[Kotlinx Serialization]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- `Json` nesnesi konfigürasyonu (ignoreUnknownKeys, vs.) buradaki yapıları kullanmalıdır.
- Ortak serileştiriciler (serializers) burada tanımlanır.
