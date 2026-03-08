# Module: :core:locale

## 🎯 Purpose (Amaç)
Proje genelinde çoklu dil desteği (localization) yönetimini sağlar. Sabit metinler, çeviriler ve dile bağlı işlemler burada yapılır.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** Translation Utilities

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:locale]
     |
     v
[Kotlin Stdlib]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Yeni bir dil veya çeviri anahtarı eklendiğinde bu modül referans alınmalıdır.
- Platform bağımsız metin formatlama yardımcılarını içerir.
