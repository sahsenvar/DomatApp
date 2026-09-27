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
[Shared.xcframework]  -->  [iosApp/Packages/Shared/Package.swift]  -->  [iosApp.xcodeproj]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- iOS tarafına export edilecek sınıflar ve interface'ler burada konfigüre edilir.
- Koin modüllerinin ana başlatma noktası burasıdır (`initKoin` / iOS için `doInitKoin`).
- Paylaşılan Compose kökü burada: `DomatApp` (`commonMain`) ve onu `ComposeUIViewController` ile
  saran `MainViewController` (`iosMain`). Android'de `MainActivity`, iOS'ta `ContentView` bunu
  host eder.
- Bu modül `domatapp.cmp.library`'yi uygular; Compose Gradle plugin'i olmadan Compose Resources'ın
  iOS resource-sync task'ı `Shared.framework` için hiç koşmuyordu.
- Xcode'u açmadan önce framework'ü üret:
  `./gradlew :shared:syncDebugSharedXCFramework`. Ayrıntı: `CLAUDE.md` → *iOS app and Swift
  Package Manager*. Apple hedefleri yalnız macOS'ta link'lenir.
