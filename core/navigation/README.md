# Module: :core:navigation

## 🎯 Purpose (Amaç)
Uygulama genelindeki rota tanımlarını, ekran geçişlerini ve navigasyon arayüzlerini içerir. Farklı platformların (Android/iOS) aynı navigasyon yapısını kullanabilmesini sağlar.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** Route Mapping, Typed Navigation

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:navigation]
     |
     +--> [Kotlin Serialization]
     v
[Kotlin Stdlib]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Ekran rotaları burada `sealed class` veya `enum` olarak tanımlanır.
- Tip güvenli navigasyon için veri modelleri serialization kullanılarak taşınır.
