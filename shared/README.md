# Module: :shared

## 🎯 Purpose (Amaç)
Tüm KMP modüllerini birleştiren ve iOS tarafına "Umbrella Framework" olarak sunan ana modüldür. Özellik modüllerini ve core modülleri bir araya getirerek uygulamanın ortak paydasını oluşturur.

## 🏗️ Architecture (Mimari)
- **Layer:** Integration Layer
- **Patterns:** Umbrella Framework, Dependency Injection (Koin) Integration

## 🔗 Dependencies (Bağımlılıklar)
```text
[:shared]
    |
    +--> [:core:*] (Tüm Core Modülleri)
    +--> [:feature:*:domain]
    +--> [:feature:*:data]
    +--> [:feature:*:presentation]
    v
[iOS Shared.framework]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- iOS tarafına export edilecek sınıflar ve interface'ler burada konfigüre edilir.
- Koin modüllerinin ana başlatma noktası burasıdır.
