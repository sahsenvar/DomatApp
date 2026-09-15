# Module: :core:resource

## 🎯 Purpose (Amaç)
Uygulamanın paylaşılan kaynaklarını (string, plural, vektör/raster görsel, font) tek yerden yönetir.
**Compose Multiplatform Resources** kullanır — Moko Resources projeden tamamen kaldırıldı.

## 🏗️ Architecture (Mimari)
- **Layer:** Infrastructure Layer
- **Patterns:** Shared Resource Management

## 🔗 Dependencies (Bağımlılıklar)
```text
[:core:resource]
     |
     v
[org.jetbrains.compose.resources]
```

## 📁 Kaynak Yerleşimi

```
src/commonMain/composeResources/
├── values/strings.xml      → Res.string.* ve Res.plurals.*
├── drawable/*.xml, *.png   → Res.drawable.*
└── font/*.ttf              → Res.font.*
src/androidMain/res/values/colors.xml → R.color.*
```

Üretilen sınıf: `com.domatapp.core.resource.generated.resources.Res`, `publicResClass = true`
olduğu için tüm modüllerden okunabilir.

## 🤖 AI Context (Yapay Zeka İçin Notlar)

- Erişim `Res` üzerinden: `stringResource(Res.string.x)`, `painterResource(Res.drawable.x)`,
  `Font(Res.font.x)`. Her kaynağın kendi import'u gerekir
  (`import com.domatapp.core.resource.generated.resources.x`) — bunlar üretilmiş top-level
  extension property'lerdir.
- **`painterResource` / `stringResource` `org.jetbrains.compose.resources` paketinden import
  edilmeli**, `androidx.compose.ui.res` değil. İki paketteki fonksiyonlar aynı isimde; androidx
  olanı `Int` resource id bekler ve `Res.drawable.x` ile derlenmez.
- **SVG eklenmez.** Compose Resources SVG'yi Android dışındaki platformlarda destekler; bu
  uygulamanın Compose UI'ı Android'de çalıştığı için her ikon Android vector drawable XML olarak
  eklenir.
- **Renkler burada değil.** Compose Resources'ın renk kaynağı tipi yoktur; renkler
  `src/androidMain/res/values/colors.xml` içinde kalır ve `R.color.*` ile okunur.
- Composition dışında (ViewModel, mapper) string okumak için `StringResourceApi` kullanılır —
  Koin'den `@Single` olarak gelir. Fonksiyonları `suspend`'dir.
