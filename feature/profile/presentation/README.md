# Module: :feature:profile:presentation

## 🎯 Purpose (Amaç)
Profil ekranının UI mantığını barındırır.

## 🏗️ Architecture (Mimari)
- **Layer:** Business Logic Layer - Presentation
- **Patterns:** MVI

## 🔗 Dependencies (Bağımlılıklar)
```text
[:feature:profile:presentation]
                |
                +--> [:feature:profile:domain]
                +--> [:core:common]
                v
          [:core:navigation]
```

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Profil bilgileri ve ayarların görüntülenmesini sağlar.
