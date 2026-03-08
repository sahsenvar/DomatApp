# Module: :core:processor

## 🎯 Purpose (Amaç)
Özel KSP (Kotlin Symbol Processing) annotasyon işlemcilerini içerir. Derleme zamanında kod üretimi (DI konfigürasyonları, boilerplate azaltma) işlemlerini yapar.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer (Compilation Tools)
- **Patterns:** Annotation Processing, Code Generation

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:processor]
     |
     +--> [KSP API]
     +--> [KotlinPoet]
     v
[Kotlin Stdlib]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Bu modül sadece derleme zamanında çalışır.
- Yeni annotasyon işlemcileri eklemek için KSP ve KotlinPoet kullanılır.
