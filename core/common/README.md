# Module: :core:common

## 🎯 Purpose (Amaç)
Bu modül, tüm proje genelinde kullanılan ortak yardımcı sınıfları, formatlayıcıları, extension fonksiyonlarını ve genel yardımcı araçları içerir. İş mantığı barındırmaz, sadece altyapısal kolaylıklar sağlar.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** Extension Functions, Utility Classes

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:common]
     |
     v
[Kotlin Stdlib]
[Coroutines]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Yeni bir yardımcı fonksiyon eklemeden önce bu modülü kontrol et.
- Sadece saf Kotlin kodları içermelidir (Platform bağımsız).
- UI veya Business logic barındırmamalıdır.
