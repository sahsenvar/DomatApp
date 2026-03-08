# Module: :core:resource

## 🎯 Purpose (Amaç)
Uygulamanın görsel ve işitsel kaynaklarını (resimler, fontlar, ikonlar) yönetir. Moko Resources kullanarak çoklu platform desteği sağlar.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** Shared Resource Management

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:resource]
     |
     v
[Moko Resources]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Yeni bir resim veya font eklendiğinde `commonMain/resources` klasörü kullanılır.
- Kod içinden `MR` nesnesi üzerinden kaynaklara erişilir.
