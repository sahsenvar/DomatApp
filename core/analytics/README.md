# Module: :core:analytics

## 🎯 Purpose (Amaç)
Uygulama genelinde kullanıcı davranışı ve olay takibi (event tracking) için tek, sağlayıcıdan
bağımsız bir katman. Şu an için **kasıtlı olarak boş** - henüz hiçbir analytics sağlayıcısı
(Firebase Analytics, PostHog, vb.) seçilmedi ve hiçbir Kotlin kaynağı yok. Modül, gerçek iş
başladığında bir yer bulsun diye önden açıldı.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** Provider-agnostic facade (beklenen) - feature modülleri belirli bir SDK'ya değil,
  bu modülün tanımlayacağı bir arayüze (örn. `AnalyticsTracker`) bağımlı olacak.

## 🔗 Dependencies (Bağımlılıklar)
Henüz yok. `:core:resulting` dışında bir bağımlılığı olması beklenmiyor; belirli bir sağlayıcı SDK'sı
seçildiğinde buraya eklenecek.

## 🤖 AI Context (Yapay Zeka İçin Notlar)
- Bu modül **boş bırakılmıştır** - kod eklemeden önce kullanıcıyla hangi sağlayıcının
  kullanılacağını netleştir, tahmin ederek bir SDK'ya bağımlılık ekleme.
- `core:common` veya `core:data`'ya event tracking kodu eklenmemeli; bunun için ayrı bir modül
  olması bilinçli bir tercih (feature modüllerinin analytics'i opsiyonel bir bağımlılık olarak
  alabilmesi için).
