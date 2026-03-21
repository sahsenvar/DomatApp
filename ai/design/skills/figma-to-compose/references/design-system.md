# DomatApp Design System

Figma tasarımından çıkarılan design token'lar ve UI component'ler.

**Figma Kaynak:** TestApp - DomatApp
**Font:** Plus Jakarta Sans (Regular, Medium, SemiBold, Bold, ExtraBold)
**Platform:** Compose Multiplatform (Android + iOS)

---

## 1. Renkler (Colors)

### Primary
| Token | Hex | Compose | Kullanım |
|-------|-----|---------|----------|
| Primary | `#13EC49` | `MaterialTheme.colorScheme.primary` | Ana buton, aktif progress, link, badge |
| PrimaryAlpha50 | `rgba(19,236,73,0.5)` | `AppColors.primaryAlpha50` | Progress bar glow |
| PrimaryAlpha30 | `rgba(19,236,73,0.3)` | `AppColors.primaryAlpha30` | Badge border |
| PrimaryAlpha20 | `rgba(19,236,73,0.2)` | `AppColors.primaryAlpha20` | İkon arka plan, badge bg |
| PrimaryAlpha10 | `rgba(19,236,73,0.1)` | `AppColors.primaryAlpha10` | Onboarding circle bg |
| PrimaryAlpha5 | `rgba(19,236,73,0.05)` | `AppColors.primaryAlpha5` | Çok hafif yeşil bg |

### Yüzey (Surface)
| Token | Hex | Compose | Kullanım |
|-------|-----|---------|----------|
| Background | `#F6F8F6` | `MaterialTheme.colorScheme.background` | Sayfa arka planı |
| Surface | `#FFFFFF` | `MaterialTheme.colorScheme.surface` | Kart, input, container |
| SurfaceSubtle | `#F8FAFC` | `AppColors.surfaceSubtle` | Location card, feature list item |

### Metin (Text)
| Token | Hex | Compose | Kullanım |
|-------|-----|---------|----------|
| TextPrimary | `#0F172A` | `AppColors.textPrimary` | Başlık, buton text (dark) |
| TextSecondary | `#1E293B` | `AppColors.textSecondary` | Value proposition, alt başlık |
| TextFeature | `#334155` | `AppColors.textFeature` | Feature list item text |
| TextBody | `#475569` | `AppColors.textBody` | Açıklama paragrafları |
| TextTertiary | `#64748B` | `AppColors.textTertiary` | Label text, muted |
| TextMuted | `#94A3B8` | `AppColors.textMuted` | Placeholder, terms text |

### Kenarlık (Border)
| Token | Hex | Compose | Kullanım |
|-------|-----|---------|----------|
| BorderDefault | `#E2E8F0` | `AppColors.borderDefault` | Input/card border, inactive dots |
| BorderLight | `#F1F5F9` | `AppColors.borderLight` | Header border, feature item border |
| BorderActive | `#13EC49` | `AppColors.borderActive` | Seçili input border (2dp) |

---

## 2. Tipografi (Typography)

Font ailesi: **Plus Jakarta Sans**

| Material3 Slot | Figma Token | Size | Weight | Line Height | Tracking | Kullanım |
|----------------|-------------|------|--------|-------------|----------|----------|
| `displayLarge` | HeadingHero | 36sp | ExtraBold | 45sp | -0.9sp | "DomatApp" splash |
| `displayMedium` | HeadingPage | 30sp | Bold | 37.5sp | -0.75sp | "Hoş Geldiniz" sayfa başlığı |
| `headlineLarge` | HeadingSection | 24sp | Bold | 30sp | — | "Basit. Adil. Şeffaf." |
| `headlineSmall` | HeadingScreen | 18sp | Bold | 22.5sp | -0.27sp | "Teslimat Bölgesi" header |
| `titleLarge` | ButtonLarge / CardValue | 18sp | Bold | 28sp | — | "Devam Et", kart değerleri |
| `titleMedium` | ButtonDefault | 16sp | Bold | 24sp | 0.4sp | "Google ile Devam Et" |
| `bodyLarge` | BodyLarge | 18sp | Medium | 29.25sp | — | Açıklama paragrafları |
| `bodyMedium` | BodyDefault | 14sp | Regular | 22.75sp | — | İçerik metni |
| `bodySmall` | BodyFeature | 14sp | Medium | 20sp | — | Feature list item |
| `labelLarge` | Label / Badge | 12sp | SemiBold | 16sp | 0.6sp | "İLÇE / İL", "TAZE & YEREL" |
| `labelMedium` | Caption | 12sp | Regular | 18sp | — | Terms, alt metin |

### Kullanım Örnekleri
```kotlin
// Hero başlık
Text(
    text = "DomatApp",
    style = MaterialTheme.typography.displayLarge,
    color = Color.White,
)

// Label (uppercase)
Text(
    text = "İLÇE / İL",
    style = MaterialTheme.typography.labelLarge,
    color = AppColors.textTertiary,
)
```

---

## 3. Boşluk (Spacing)

`AppSpacing` objesi ile erişilir.

| Token | Değer | Kullanım |
|-------|-------|----------|
| `xxs` | 2dp | Minimal boşluk |
| `xs` | 4dp | İkon iç boşluk |
| `sm` | 6dp | Progress dot boyutu |
| `md` | 8dp | Gap küçük, input iç gap |
| `lg` | 12dp | Feature list gap, ikon padding |
| `xl` | 16dp | Genel padding, card gap |
| `xxl` | 20dp | Orta section padding |
| `xxxl` | 24dp | Section arası, hero bottom |
| `xxxxl` | 32dp | Büyük section padding, bottom bar |
| `xxxxxl` | 40dp | En büyük boşluk |

---

## 4. Köşe Yuvarlaklığı (Shapes)

`AppShapes` veya `MaterialTheme.shapes` ile erişilir.

| Material3 Slot | Değer | Kullanım |
|----------------|-------|----------|
| `extraSmall` | 4dp | Minimal |
| `small` | 8dp | Feature list item, ikon container |
| `medium` | 12dp | Buton, kart, input, mobile container |
| `large` | 16dp | Büyük container |
| `extraLarge` | 9999dp | Badge/pill, progress dots, circle |

### Ek Shape Değerleri
| Değer | Kullanım |
|-------|----------|
| 24dp | Hero image alt köşeleri (özel) |

---

## 5. Gölge (Shadows)

`Modifier` extension'ları ile erişilir.

| Token | Elevation | Renk | Kullanım |
|-------|-----------|------|----------|
| `shadowXs` | 1dp | Black 5% | Input fields |
| `shadowSm` | 2dp | Black 5% | Küçük kartlar |
| `shadowMd` | 4dp | Black 10% | Orta kartlar |
| `shadowLg` | 10dp | Black 10% | Büyük konteynerler |
| `shadowXl` | 20dp | Black 15% | Modal, overlay |
| `shadowPrimaryGlow` | 8dp | Green 30% | Primary buton yeşil glow |

---

## 6. UI Component'ler

### PrimaryButton
Ana aksiyon butonu. Yeşil arka plan, yeşil glow shadow.

**Dosya:** `components/PrimaryButton.kt`

| Özellik | Değer |
|---------|-------|
| Background | `primary` (#13EC49) |
| Text | `titleLarge` (18sp Bold) |
| Text Color | `onPrimaryContainer` (#0F172A) |
| Shape | 12dp rounded |
| Padding | vertical 16dp |
| Shadow | primaryGlow |
| Width | full-width |

```kotlin
PrimaryButton(
    text = "Devam Et",
    onClick = { },
)
```

---

### GoogleSignInButton
Google ile giriş butonu. Beyaz arka plan, outline border.

**Dosya:** `components/GoogleSignInButton.kt`

| Özellik | Değer |
|---------|-------|
| Background | `surface` (white) |
| Border | 1dp `borderDefault` |
| Text | `titleMedium` (16sp Bold) |
| Height | 56dp |
| Shape | 12dp rounded |
| Shadow | 1dp |

```kotlin
GoogleSignInButton(
    onClick = { },
    iconPainter = painterResource(Res.drawable.google_icon),
)
```

---

### ScreenHeader
Ekran üst başlığı. Geri butonu + başlık + opsiyonel alt içerik.

**Dosya:** `components/ScreenHeader.kt`

| Özellik | Değer |
|---------|-------|
| Background | surface 90% alpha |
| Border Bottom | 1dp `borderLight` |
| Back Button | 48x48, CircleShape |
| Title | `headlineSmall` centered |

```kotlin
ScreenHeader(
    title = "Teslimat Bölgesi",
    onBackClick = { },
    bottomContent = {
        ProgressSteps(totalSteps = 4, currentStep = 1)
    },
)
```

---

### ProgressSteps
Header'da kullanılan adım göstergesi.

**Dosya:** `components/ProgressIndicator.kt`

| Özellik | Değer |
|---------|-------|
| Dot Size | 6dp |
| Active Width | 32dp pill |
| Active Color | `primary` |
| Inactive Color | `borderDefault` |
| Gap | 12dp |

```kotlin
ProgressSteps(totalSteps = 4, currentStep = 1)
```

---

### ProgressDots
Onboarding'de kullanılan sayfa göstergesi.

**Dosya:** `components/ProgressIndicator.kt`

| Özellik | Değer |
|---------|-------|
| Dot Size | 8dp |
| Active Width | 24dp pill |
| Active Color | `primary` |
| Inactive Color | `borderDefault` |
| Gap | 8dp |

```kotlin
ProgressDots(totalDots = 3, activeIndex = 0)
```

---

### LocationCard
Konum seçim kartı. İkon + etiket + değer + chevron.

**Dosya:** `components/LocationCard.kt`

| Özellik | Değer |
|---------|-------|
| Background | `surfaceSubtle` |
| Shape | 12dp rounded |
| Padding | 17dp |
| Icon Box | 40x40, 8dp rounded, `primaryAlpha20` bg |
| Label | `labelLarge` uppercase |
| Value | `titleLarge` |
| Connector | 2dp `borderDefault`, 24dp height |

```kotlin
LocationCard(
    label = "İlçe / İl",
    value = "Tuzla/İstanbul",
    isVerified = true,
    cardAlpha = 0.6f,
)
LocationCardConnector()
```

---

### InputDropdown
Dropdown seçim inputu. Active/inactive durumlu.

**Dosya:** `components/InputDropdown.kt`

| Özellik | Değer |
|---------|-------|
| Background | `surface` (white) |
| Border Inactive | 1dp `borderDefault` |
| Border Active | 2dp `borderActive` (#13EC49) |
| Shape | 12dp rounded |
| Label | `labelLarge` uppercase |
| Value | `titleLarge` |

```kotlin
InputDropdown(
    label = "Blok No",
    value = "Seçiniz",
    icon = Res.drawable.ic_building,
    onClick = { },
    isActive = true,
)
```

---

### FeatureListItem
Özellik listesi öğesi. İkon + metin.

**Dosya:** `components/FeatureListItem.kt`

| Özellik | Değer |
|---------|-------|
| Background | `surfaceSubtle` |
| Border | 1dp `borderLight` |
| Shape | 8dp rounded |
| Padding | 13dp |
| Text | `bodySmall` (14sp Medium) |

```kotlin
FeatureListItem(
    icon = Icons.Default.Check,
    text = "Üretici adı görünür",
)
```

---

### BottomActionBar
Ekran altı aksiyon barı. İçine PrimaryButton konur.

**Dosya:** `components/BottomActionBar.kt`

| Özellik | Değer |
|---------|-------|
| Background | `surface` (white) |
| Border Top | 1dp `borderLight` |
| Padding | 16dp horizontal, 17dp top, 32dp bottom |

```kotlin
BottomActionBar {
    PrimaryButton(
        text = "Devam Et",
        onClick = { },
    )
}
```

---

### HeroBadge
Splash ekranında kullanılan badge/tag. Pill şekilli.

**Dosya:** `components/HeroBadge.kt`

| Özellik | Değer |
|---------|-------|
| Background | `primaryAlpha20` |
| Border | 1dp `primaryAlpha30` |
| Shape | pill (9999dp) |
| Text | `labelLarge` uppercase, `primary` renk |
| Icon | 13dp, `primary` renk |

```kotlin
HeroBadge(
    icon = Icons.Default.Eco,
    text = "Taze & Yerel",
)
```

---

## 7. Ekranlar

Figma'da 10 ekran tanımlı:

| # | Ekran | Node ID | Açıklama |
|---|-------|---------|----------|
| 1 | Giriş Ekranı | 51:3 | Hero image + DomatApp branding + Google Sign-In |
| 2 | Konum Seçimi (Step 2) | 51:34 | Teslimat bölgesi - Daire No |
| 3 | Konum Seçimi (Step 3) | 51:126 | Teslimat bölgesi - variant |
| 4 | Konum Seçimi (Step 4) | 51:224 | Teslimat bölgesi - variant |
| 5 | Onboarding: Trust & Safety | 51:319 | "Basit. Adil. Şeffaf." |
| 6 | Onboarding: Direct Pricing | 51:365 | Fiyat açıklaması |
| 7 | Onboarding: Community Power | 51:438 | Topluluk gücü |
| 8 | Onboarding: Welcome | 51:490 | "Hoş Geldiniz" |
| 9 | Konum Seçimi - Dropdown | 51:513 | Blok dropdown açık |
| 10 | Onboarding: Effortless Shopping | 51:614 | "Zahmetsiz alışveriş" |

---

## 8. Layout Kuralları

### Spacing Kuralı
- **Max doğrudan padding/spacing: 20dp.** 20dp'yi aşan boşluk gerekiyorsa `Spacer(Modifier.weight(1f))` kullan veya `Alignment.Bottom/Top` ile sabitle.
- Hardcoded büyük padding değerleri (`padding(bottom = 140.dp)` gibi) **yasak**.

```kotlin
// YANLIŞ - 48dp hardcoded padding
Column(modifier = Modifier.padding(top = 48.dp)) { ... }

// DOĞRU - weight ile esnek boşluk
Column {
    Spacer(modifier = Modifier.weight(1f))
    // içerik
    Spacer(modifier = Modifier.weight(1f))
}
```

### Onboarding Ekran Layout Kalıbı
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface)
        .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Spacer(modifier = Modifier.weight(1f))    // üst esnek boşluk
    // İllüstrasyon (200dp circle/box)
    Spacer(modifier = Modifier.height(20.dp)) // max 20dp
    // Başlık
    Spacer(modifier = Modifier.height(12.dp))
    // Açıklama
    Spacer(modifier = Modifier.weight(1f))    // alt esnek boşluk
    // ProgressDots + PrimaryButton (doğal olarak alta oturur)
}
```

### Scaffold innerPadding Kuralı
- `Scaffold { innerPadding -> }` kullanıldığında, `innerPadding` **mutlaka** içerik container'ına uygulanmalı.
- Aksi halde içerik sistem navigasyon barının altında kalır.

```kotlin
// DOĞRU
Scaffold { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
        // içerik
    }
}

// YANLIŞ - innerPadding kullanılmıyor
Scaffold { innerPadding ->
    NavDisplay(...)  // navigasyon barının altında kalır
}
```

---

## 9. Doğrulama Sistemi (Validation)

### 5 Katmanlı Doğrulama

| Katman | Ne Yapar | Komut |
|--------|----------|-------|
| K1: DETECT | Element sınıflandırma (image/vector/shape) | figma-image skill |
| K2: CONVERT | SVG path data doğrulama | `make validate-svg` |
| K3: TOKEN | Renk/typography eşleştirme + hardcoded scan | `make validate-tokens` |
| K4: PREVIEW | @Screen → @Preview otomatik generation | `make validate-previews` |
| K5: COMPARE | Figma vs Compose pixel diff + Claude review | `pixel_diff.py` + Claude |

### Hepsini Çalıştır
```bash
make validate-all
```

### Görsel Karşılaştırma (K5)
```bash
# 1. Figma screenshot: get_screenshot(nodeId) → figma.png
# 2. Cihazdan: ./scripts/capture_screen.sh compose.png
# 3. Diff: python3 scripts/pixel_diff.py figma.png compose.png -o diff.png -t 5
# 4. Claude: İki PNG'yi oku ve karşılaştır
```

### Doğrulama Scriptleri
| Script | Dosya | Ne Kontrol Eder |
|--------|-------|-----------------|
| SVG Validator | `scripts/validate_svg_paths.py` | Path data geçerli mi, fillColor var mı |
| Token Validator | `scripts/validate_design_tokens.py` | Renkler/typography eşleşiyor mu, hardcoded var mı |
| Pixel Diff | `scripts/pixel_diff.py` | İki screenshot arası fark yüzdesi |
| Asset Extractor | `scripts/extract_assets.py` | Figma REST API ile asset çekme |
| Screen Capture | `scripts/capture_screen.sh` | Cihazdan screenshot alma |

### Not: Paparazzi
Paparazzi (1.3.5) Gradle 8.14+ ile uyumsuz (`TestFailure` class removed). Gradle güncellendiğinde veya Paparazzi yeni versiyon çıkardığında tekrar denenebilir. Şu an screenshot'lar `adb` + `capture_screen.sh` ile alınıyor.

---

## 10. Dosya Yapısı

```
composeApp/src/commonMain/kotlin/org/example/project/
├── theme/
│   ├── AppTheme.kt          # MaterialTheme wrapper
│   ├── AppColors.kt         # Renk token'ları + LightColorScheme
│   ├── AppTypography.kt     # Typography + Plus Jakarta Sans
│   ├── AppShapes.kt         # Shape token'ları
│   ├── AppSpacing.kt        # Spacing token'ları
│   └── AppShadows.kt        # Shadow modifier extension'ları
├── components/
│   ├── PrimaryButton.kt     # Ana aksiyon butonu
│   ├── GoogleSignInButton.kt# Google giriş butonu
│   ├── ScreenHeader.kt      # Ekran üst başlığı
│   ├── ProgressIndicator.kt # ProgressSteps + ProgressDots
│   ├── LocationCard.kt      # Konum kartı + connector
│   ├── InputDropdown.kt     # Dropdown input
│   ├── FeatureListItem.kt   # Özellik listesi öğesi
│   ├── BottomActionBar.kt   # Alt aksiyon barı
│   ├── HeroBadge.kt         # Hero badge/tag
│   ├── QuantitySelector.kt  # Miktar seçici
│   ├── CommunityGoalCard.kt # Community goal kartı
│   └── DeliveryInfoCard.kt  # Teslimat bilgi kartı
```
