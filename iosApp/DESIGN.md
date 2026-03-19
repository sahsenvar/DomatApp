# iOS Design System — DomatApp

Bu döküman iOS SwiftUI tarafında keşfedilen kritik tasarım kararlarını, font/renk sorunlarını ve layout kurallarını kapsar. Android/Compose tarafını reference alarak iOS'a doğru aktarım yaparken karşılaşılan problemler ve çözümleri burada belgelenmiştir.

---

## 1. Font Sistemi

### 1.1 Mevcut Font Dosyaları

Projede yalnızca **iki** Nunito Sans variant'ı vardır:

| Dosya | PostScript Adı | Gerçek Ağırlık |
|-------|---------------|----------------|
| `nunito_sans_regular.ttf` | `NunitoSans-12ptExtraLight` | ExtraLight (200) — Regular **değil** |
| `nunito_sans_italic.ttf` | `NunitoSans-12ptExtraLightItalic` | ExtraLight Italic |

Dosyalar iki yerde kopyalı bulunur:
- `iosApp/iosApp/Resources/Fonts/` — iOS bundle'a dahil edilen kopya
- `core/resource/src/commonMain/moko-resources/fonts/` — MOKO Resources kaynak

**Bold, ExtraBold, Regular, SemiBold varyantları projede mevcut değildir.**

### 1.2 Kritik Sorun: PostScript Adı Uyuşmazlığı

iOS'da `UIFont(name:)` ve `.custom(_:size:)` çağrıları **tam PostScript adı** gerektirir. Family adı veya dosya adı kabul edilmez.

```swift
// ❌ YANLIŞ — UIFont(name:) nil döner, sistem fallback'e düşer
UIFont(name: "NunitoSans", size: 36)
UIFont(name: "NunitoSans-Regular", size: 36)
UIFont(name: "nunito_sans_regular", size: 36)

// ✅ DOĞRU — gerçek PostScript adı
UIFont(name: "NunitoSans-12ptExtraLight", size: 36)
```

PostScript adını bulmak için:
```python
# TTF name table'dan okuma (platform ID=3, nameID=6)
with open("nunito_sans_regular.ttf", "rb") as f:
    data = f.read()
# → "NunitoSans-12ptExtraLight"
```

### 1.3 Font Fallback Zincirleri: iOS vs Android Farkı

**iOS fallback davranışı:**
- `UIFont(name: "NunitoSans-Regular")` → nil döner
- `Font.custom("NunitoSans-Regular", size: 36)` → otomatik sistem fontuna düşer
- Sistem fontu SF Pro Display, 36pt display boyutunda **büyük sol sidebearing** içerir
- Sonuç: Metin görsel olarak solda boşluklu görünür, padding vermişsiniz gibi davranır

**Android fallback davranışı:**
- `fontFamilyResource(MR.fonts.nunito_sans_regular)` → Android font sistemine Nunito Sans ailesi olarak kayıt yapar
- `FontWeight.ExtraBold` verildiğinde Android, Nunito Sans geometrisi üzerinden ağırlığı **sentezler**
- Glyph metric'leri değişmez → sol boşluk sorunu oluşmaz

**Sonuç:** iOS'da font adı yanlışsa tamamen farklı glyph metric'lere sahip başka bir font ailesi devreye girer. Android'de ise aynı aile içinde ağırlık sentezi yapılır, metrik korunur.

### 1.4 DomatTypography — Doğru Uygulama

`iosApp/iosApp/Core/Design/Theme/DomatTypography.swift`:

```swift
private static let fontRegular = "NunitoSans-12ptExtraLight"

private static func font(size: CGFloat, weight: Font.Weight) -> Font {
    if let _ = UIFont(name: fontRegular, size: size) {
        return .custom(fontRegular, size: size).weight(weight)
    }
    return .system(size: size, weight: weight, design: .default)
}
```

> **Önemli:** `UIFont(name: fontRegular, size:)` kontrol adımı başarılı olur (doğru PostScript adı verildiği için). Başarısız olursa `.system()` fallback'i devreye girer — bu kabul edilebilir bir güvenli alan.

### 1.5 ExtraBold/Bold Varyantları

Projede ExtraBold ve Bold font dosyaları **yoktur**. Şu an `displaySmallExtraBold` gibi "heavy" weight token'lar, aslında ExtraLight dosyası üzerinden iOS'un font weight synthesizer'ı ile oluşturulur. Görsel fark azdır; doğru davranış için font dosyalarının eklenmesi gerekir.

**Eksik font dosyaları eklenirse yapılacaklar:**
1. `iosApp/iosApp/Resources/Fonts/` altına kopyala
2. `Info.plist → UIAppFonts` dizisine PostScript adını ekle
3. `DomatTypography.swift`'te `fontBold`, `fontExtraBold` sabitlerini tanımla
4. `font()` metodunu ağırlığa göre doğru PostScript adını seçecek şekilde güncelle

---

## 2. Renk Tokenleri

### 2.1 Primary Renk Hatası (Düzeltildi)

`DomatColorScheme.light.primary` yanlış renk değeriyle başlatılmıştı:

```swift
// ❌ ESKİ — yanlış
primary: DomatPalette.green700  // #1B5E20 (koyu orman yeşili)

// ✅ DOĞRU
primary: Color(hex: 0x13EC49)   // parlak limon yeşili
```

Doğru değer `core/resource/src/commonMain/moko-resources/base/colors.xml`'den alınır:
```xml
<color name="primary_default">#13EC49</color>
```

### 2.2 Figma → iOS Renk Mapping

| Figma Token | Android (Kotlin) | iOS (Swift) |
|-------------|-----------------|------------|
| `primary` | `MaterialTheme.colorScheme.primary` | `colors.primary` (env) |
| `onPrimary` | `colorScheme.onPrimary` | `colors.onPrimary` |
| `surface` | `colorScheme.surface` | `colors.surface` |
| `onSurface` | `colorScheme.onSurface` | `colors.onSurface` |
| `onSurfaceVariant` | `colorScheme.onSurfaceVariant` | `colors.onSurfaceVariant` |
| `outline` | `colorScheme.outline` | `colors.outline` |
| `primaryContainer` | `colorScheme.primaryContainer` | `colors.primaryContainer` |

### 2.3 `onPrimary` Değeri

| Tema | Değer | Hex |
|------|-------|-----|
| Light | `DomatPalette.green900` | `#002204` |
| Dark | `DomatPalette.green800` | `#00390A` |

`#13EC49` gibi parlak primer üzerine yazı için çok koyu yeşil kullanılır (WCAG kontrast oranı yüksek).

### 2.4 Colors Environment Key

Renklere erişim `@Environment(\.domatColors)` ile yapılır:

```swift
@Environment(\.domatColors) private var colors

// Kullanım
Text("merhaba").foregroundStyle(colors.onSurface)
Rectangle().fill(colors.primary)
```

Dark mode'u test etmek için `domatTheme(colorScheme: .dark)` modifier'ı kullanılır.

---

## 3. SwiftUI Layout Kuralları

### 3.1 ScrollView İçinde VStack Tam Genişlik

SwiftUI'da `VStack` içinde `ScrollView` kullanıldığında VStack otomatik olarak tam genişliğe uzanmaz:

```swift
// ❌ Sorunlu — VStack içerik kadar daralır, alanı doldurmaz
ScrollView {
    VStack { ... }
}

// ✅ Doğru
ScrollView {
    VStack { ... }
        .frame(maxWidth: .infinity)
}
```

Bu kural özellikle şunları etkiler:
- `Text` ile center hizalama (`.multilineTextAlignment(.center)` yeterli olmaz)
- Background fill
- Padding'in tüm genişliğe uygulanması

### 3.2 ZStack İçinde Child Frame Kontrolü

`ZStack(alignment: .bottomLeading)` kullanımında her child kendi frame'ini yönetmek zorundadır:

```swift
ZStack(alignment: .bottomLeading) {
    // Her child'ın maxWidth'i açıkça belirtilmeli
    Image(uiImage: img)
        .resizable()
        .scaledToFill()
        .frame(maxWidth: .infinity, minHeight: 442, maxHeight: 442)  // ✅
        .clipped()

    LinearGradient(...)
        .frame(maxWidth: .infinity, minHeight: 442, maxHeight: 442)  // ✅

    VStack { ... }
        .padding(...)
    // Bu VStack sabit boyutlu olduğu için frame gerekmez
}
.frame(maxWidth: .infinity, minHeight: 442, maxHeight: 442)  // ✅ ZStack'in kendisi
```

### 3.3 Compose'dan SwiftUI'ya Padding Çevirisi

| Compose | SwiftUI |
|---------|---------|
| `Column(modifier = Modifier.padding(horizontal = 24.dp))` | `.padding(.horizontal, 24)` VStack üzerinde |
| `Column(modifier = Modifier.padding(top = 32.dp, bottom = 16.dp))` | `.padding(.top, 32).padding(.bottom, 16)` |
| `Spacer(modifier = Modifier.height(124.dp))` | `Color.clear.frame(height: 124)` |
| `fillMaxWidth()` | `.frame(maxWidth: .infinity)` |
| `fillMaxHeight()` | `.frame(maxHeight: .infinity)` |
| `wrapContentWidth()` | Varsayılan — frame gerekmez |

### 3.4 Hero Bölümü Layout Paterni

Hero section'da görsel (full-bleed), gradient overlay ve içerik birleşimi için önerilen yapı:

```swift
ZStack(alignment: .bottomLeading) {
    // 1. Arka plan görseli (veya renk fallback)
    Group {
        if let img = loadImage("img_hero_login") {
            Image(uiImage: img).resizable().scaledToFill()
        } else {
            colors.primaryContainer
        }
    }
    .frame(maxWidth: .infinity, minHeight: 442, maxHeight: 442)
    .clipped()

    // 2. Gradient overlay (ayrı katman)
    LinearGradient(
        stops: [
            .init(color: .clear, location: 0.0),
            .init(color: .black.opacity(0.2), location: 0.5),
            .init(color: .black.opacity(0.7), location: 1.0),
        ],
        startPoint: .top,
        endPoint: .bottom
    )
    .frame(maxWidth: .infinity, minHeight: 442, maxHeight: 442)

    // 3. İçerik (alt-sol hizalı)
    VStack(alignment: .leading, spacing: 16) {
        // badge, başlık vb.
    }
    .padding(.horizontal, 24)
    .padding(.bottom, 32)
}
.frame(maxWidth: .infinity, minHeight: 442, maxHeight: 442)
```

> **Figma notu:** LoginScreen hero section'ın alt kenarı düzdür — `clipShape` veya corner radius uygulanmaz.

---

## 4. Tipografi Ölçeği

`DomatTypography` Compose'daki `DomatTypographyScale`'i tam olarak yansıtır:

| Token | Boyut | Ağırlık | Kullanım |
|-------|-------|---------|----------|
| `displayLarge` | 57 | regular | — |
| `displayMedium` | 45 | regular | — |
| `displaySmall` | 36 | regular | — |
| `displaySmallExtraBold` | 36 | heavy | Hero başlık |
| `headlineLarge` | 32 | regular | — |
| `headlineMedium` | 28 | regular | — |
| `headlineSmall` | 24 | regular | — |
| `titleLarge` | 22 | medium | — |
| `titleMedium` | 16 | medium | Buton metni |
| `titleSmall` | 14 | medium | — |
| `bodyLarge` | 16 | regular | — |
| `bodyMedium` | 14 | regular | — |
| `bodySmall` | 12 | regular | — |
| `labelLarge` | 14 | medium | Badge, card label |
| `labelMedium` | 12 | medium | ToS metni |
| `labelSmall` | 11 | medium | — |

**Dikkat:** Figma'da belirlenen bazı boyutlar token ölçeğine uymayabilir. Örneğin LoginScreen subtitle'ı Figma'da 18pt olarak belirtilmiştir — bu `bodyLarge` (16pt) ile eşleşmez. Bu durumda `DomatTypography` token'ı değil `.system(size: 18)` kullanılır ve neden böyle yapıldığı bir yorum satırıyla belirtilir.

### 4.1 Line Height

`DomatLineHeight` enum'u Compose'daki değerlerle eşleşir. SwiftUI'da line height'ı uygulamak için:

```swift
// Yaklaşım 1 — lineSpacing (satırlar arası boşluk, tam line height değil)
Text("...").lineSpacing(DomatLineHeight.bodyLarge - DomatTypography.bodyLargeSize)

// Yaklaşım 2 — custom ViewModifier (daha kesin)
// Henüz implement edilmedi
```

---

## 5. Komponent Spesifikasyonları (Figma Referansı)

### 5.1 DomatHeroBadge

Figma node ölçümleri (Login ekranı hero badge):

| Özellik | Değer |
|---------|-------|
| Şekil | Pill (cornerRadius = 9999) |
| Arka plan | `colors.primary` @ %20 opaklık |
| Border | 1pt `colors.primary` @ %30 opaklık |
| Padding yatay | 12pt |
| Padding dikey | 4pt |
| İkon boyutu | 13×13pt |
| İkon-metin aralığı | 8pt |
| Metin | `.uppercased()`, `labelLarge` |
| Metin rengi | `colors.primary` |

SwiftUI uygulaması `iosApp/iosApp/Core/Design/Components/DomatHeroBadge.swift`.

### 5.2 DomatGoogleSignInButton

Figma node ölçümleri:

| Özellik | Değer |
|---------|-------|
| Boyut | fillWidth × 56pt |
| Köşe yarıçapı | 12pt (`DomatShape.medium`) |
| Arka plan | `colors.surface` (beyaz) |
| Border | 1pt `colors.outline` |
| Gölge | `DomatElevation.xs` |
| İkon | sol kenara 20pt padding, 24×24pt |
| Metin | merkez hizalı, `titleMedium`, `colors.onSurface` |

> **İkon konumlandırması:** Metin merkeze hizalanırken ikon sol kenarda sabit kalır. Bu, ZStack ile overlay yapısı gerektiriyor: dış HStack'te ikon + Spacer, üstüne ZStack Text katmanı.

### 5.3 LoginView Layout Özeti

Figma node `19:2` ölçümlerine dayalı tam layout:

```
┌─────────────────────────────────┐ ← ignoresSafeArea(.top)
│                                 │
│     Hero Görseli                │ 442pt yükseklik
│     (gradient overlay)          │ flat alt kenar, clip yok
│                                 │
│  [badge] Taze & Yerel           │ ← padding bottom=32, left=24
│  DomatApp (36pt, heavy)         │
└─────────────────────────────────┘
  padding top=32, horizontal=24
  "Haftalık olarak..." (18pt)
  padding bottom=32
  [Spacer 124pt]
  [Google Sign In Button]
  padding bottom=16
─────────────────────────────────
  padding top=32, horizontal=16
  [ToS metni] (labelMedium)
  padding bottom=24
```

---

## 6. MOKO Resources Entegrasyonu

### 6.1 Görsel Yükleme

MOKO Resources görselleri Xcode bundle'ına ayrı bir bundle olarak kopyalanır. `Bundle.main` üzerinden değil, MOKO'nun kendi bundle'ı üzerinden erişilmesi gerekir:

```swift
private func mokoImage(_ name: String) -> UIImage? {
    if let url = Bundle.main.url(
        forResource: "DomatApp.core:resource",
        withExtension: "bundle"
    ),
    let resourceBundle = Bundle(url: url),
    let img = UIImage(named: name, in: resourceBundle, with: nil) {
        return img
    }
    return nil
}
```

Bu yöntem başarısız olursa (`nil` dönerse) UI'da fallback renk gösterilmeli:

```swift
if let img = mokoImage("img_hero_login") {
    Image(uiImage: img).resizable().scaledToFill()
} else {
    colors.primaryContainer  // fallback renk
}
```

### 6.2 MOKO Bundle Adlandırması

Bundle adı (`DomatApp.core:resource`) Gradle modül yolundan türer: `:core:resource` modülü. Bundle adını değiştirmeniz gerekirse `core/resource/build.gradle.kts`'teki `moko-resources` konfigürasyonuna bakın.

### 6.3 Font Dosyalarının Kayıt Edilmesi

Font dosyaları `Info.plist`'e kayıtlı olmalıdır:

```xml
<key>UIAppFonts</key>
<array>
    <string>nunito_sans_regular.ttf</string>
    <string>nunito_sans_italic.ttf</string>
</array>
```

Font bundle konumu: `iosApp/iosApp/Resources/Fonts/`
Xcode proje ayarı: Target → Build Phases → Copy Bundle Resources'da bu dosyalar görünmeli.

---

## 7. Theme Entegrasyonu

### 7.1 DomatTheme Modifier

Tüm view'lar `domatTheme()` modifier'ı ile sarılır. Bu modifier:
- `DomatColorScheme` environment value'yu inject eder
- Sistem color scheme'e göre light/dark seçimi yapar

```swift
// Preview'larda
#Preview {
    LoginView()
        .environmentObject(NavigationRouter())
        .domatTheme()
}

// Karanlık mod testi
#Preview("Dark") {
    LoginView()
        .environmentObject(NavigationRouter())
        .domatTheme(colorScheme: .dark)
}
```

### 7.2 Environment Values Erişimi

```swift
// Renklere erişim
@Environment(\.domatColors) private var colors

// Kullanım
colors.primary          // Ana renk
colors.surface          // Yüzey rengi
colors.onSurface        // Yüzey üstü metin rengi
colors.onSurfaceVariant // İkincil metin rengi
colors.outline          // Border rengi
colors.primaryContainer // Primer container rengi
```

---

## 8. Shape ve Elevation

### 8.1 DomatShape

| Token | Değer |
|-------|-------|
| `extraSmall` | 4pt |
| `small` | 8pt |
| `medium` | 12pt |
| `large` | 16pt |
| `extraLarge` | 24pt |
| `full` | 9999pt (pill) |

### 8.2 DomatElevation ve domatShadow

Gölge uygulamak için:

```swift
view.domatShadow(DomatElevation.xs)
```

Elevation seviyeleri Compose'daki `shadowElevation` değerleriyle eşleştirilir.

---

## 9. Bilinen Sorunlar ve Geçici Çözümler

### 9.1 SourceKit False Positive: `UIFont` not in scope

`DomatTypography.swift`'te `UIFont` kullanımı SourceKit analizörünün false positive uyarısı vermesine yol açabilir ("UIFont is not available in this context"). Derleme sırasında hata **oluşmaz** — yalnızca IDE'de görsel uyarıdır. Bunun nedeni bazı SourceKit analyzer sürümlerinin UIKit türlerini Swift dosyalarında yanlış scope kontrolü yapmasıdır.

### 9.2 ExtraBold Ağırlığı Görsel Olarak Yeterince Kalın Değil

Projede ExtraBold font dosyası olmadığı için `.heavy` weight, iOS'un font synthesizer'ı ile üretilir. Mevcut Nunito Sans dosyası ExtraLight (200) ağırlığında olduğundan sentezlenen kalın versiyon gerçek bir ExtraBold'a benzemeyebilir. Gerçek ExtraBold için font dosyası eklenmesi gerekir.

### 9.3 TODO: NavigateToLocationSelection

`LoginView.swift`'te Google Sign-In butonu tıklandığında ViewModel `LoginEffectNavigateToLocationSelection` effect'i emit eder. iOS tarafındaki navigasyon henüz implement edilmemiştir:

```swift
.onEffect(from: vm) { effect in
    if effect is LoginEffectNavigateToLocationSelection {
        // TODO: router.navigate(to: .locationSelection)
    }
}
```

`LocationSelectionView` oluşturulduğunda bu TODO tamamlanmalıdır.

---

## 10. iOS Dosya Yapısı

```
iosApp/iosApp/
├── App/
│   ├── DomatApp.swift              # @main entry
│   └── NavigationRouter.swift      # ObservableObject router
├── Core/
│   └── Design/
│       ├── Theme/
│       │   ├── DomatColors.swift       # DomatPalette, DomatColorScheme, env key
│       │   ├── DomatTypography.swift   # Font ölçeği + DomatLineHeight
│       │   ├── DomatShape.swift        # Köşe yarıçapları
│       │   └── DomatTheme.swift        # domatTheme() view modifier
│       └── Components/
│           ├── DomatHeroBadge.swift
│           ├── DomatGoogleSignInButton.swift
│           └── ...
├── Features/
│   └── Auth/
│       └── LoginView.swift
└── Resources/
    └── Fonts/
        ├── nunito_sans_regular.ttf   # PostScript: NunitoSans-12ptExtraLight
        └── nunito_sans_italic.ttf    # PostScript: NunitoSans-12ptExtraLightItalic
```

---

## 11. Figma → SwiftUI Çeviri Özeti

| Figma Özellik | SwiftUI Karşılığı |
|--------------|------------------|
| `cornerRadius: 9999` | `Capsule()` veya `clipShape(Capsule())` |
| `cornerRadius: 12` | `RoundedRectangle(cornerRadius: 12)` |
| `padding: top=32, bottom=16, left=24, right=24` | `.padding(.top, 32).padding(.bottom, 16).padding(.horizontal, 24)` |
| `mainAxisAlignment: CENTER` | `.frame(maxWidth: .infinity)` + `.multilineTextAlignment(.center)` |
| `crossAxisAlignment: START` | `VStack(alignment: .leading)` |
| `gap: 16` | `VStack(spacing: 16)` veya `HStack(spacing: 16)` |
| `fill: linear-gradient(...)` | `LinearGradient(stops: [...], startPoint: .top, endPoint: .bottom)` |
| `opacity: 20%` | `.opacity(0.2)` |
| `stroke: color 30%, width: 1` | `.overlay { Capsule().strokeBorder(color.opacity(0.3), lineWidth: 1) }` |
| `text: uppercase` | `.uppercased()` |
| `image: fill container` | `.resizable().scaledToFill().clipped()` |
| `ignoresSafeArea: top` | `.ignoresSafeArea(edges: .top)` |
