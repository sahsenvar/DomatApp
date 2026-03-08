# Module: :core:domain

## 🎯 Purpose (Amaç)
Proje genelinde paylaşılan temel domain modellerini, ortak UseCase arayüzlerini ve global iş kurallarını içerir. Tüm özellik (feature) modüllerinin domain katmanları buradaki temel sınıfları kullanır.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer (Core Domain)
- **Patterns:** UseCase Interfaces, Domain Entities

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:domain]
     |
     +--> [Arrow Core/Fx]
     +--> [Coroutines]
     +--> [Koin Core]
     v
[Kotlin Stdlib]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Burası 100% saf Kotlin'dir. Hiçbir platforma (Android/iOS) bağımlı olmamalıdır.
- Ortak Error modelleri ve Result yapıları burada tanımlanır.
