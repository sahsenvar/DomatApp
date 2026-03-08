# Module: :composeApp

## 🎯 Purpose (Amaç)
Uygulamanın Android tarafındaki ana giriş noktasıdır. Jetpack Compose kullanılarak oluşturulan UI katmanını ve Android'e özgü yapılandırmaları içerir.

## 🏗️ Architecture (Mimari)
- **Layer:** UI Layer (Android)
- **Patterns:** Jetpack Compose, Android Activity/Application

## 🔗 Dependencies (Bağımlılıklar)
```text
[:composeApp]
      |
      +--> [:shared]
      +--> [:core:navigation]
      +--> [:feature:*:presentation]
      v
[Android SDK / Jetpack Compose]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Sadece Android'e özgü UI kodları (Compose) burada bulunmalıdır.
- İş mantığı kesinlikle barındırmaz, `:shared` veya ilgili `:presentation` modüllerini kullanır.
