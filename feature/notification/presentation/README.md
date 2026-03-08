# Module: :feature:notification:presentation

## 🎯 Purpose (Amaç)
Bildirim ekranlarının UI mantığını barındırır.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Presentation
- **Patterns:** MVI

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:notification:presentation]
                  |
                  +--> [:feature:notification:domain]
                  +--> [:core:common]
                  v
            [:core:navigation]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Bildirimlerin UI üzerindeki etkileşimlerini yönetir.
