# Module: :core:resulting

## 🎯 Purpose (Amaç)
Hata yönetimi ve işlem sonuçlarını (Success/Failure) sarmalayan ortak Result ve Either yapılarını içerir. Uygulama genelinde standart bir hata raporlama yapısı kurar.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** Railway Oriented Programming (Result/Either)

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:resulting]
     |
     +--> [Arrow Core]
     v
[Coroutines]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- İşlem sonuçlarını döndürürken `Result` veya Arrow'un `Either` yapıları kullanılmalıdır.
- Hata durumları (Failure) için standart modeller buradadır.
