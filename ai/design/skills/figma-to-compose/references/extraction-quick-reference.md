# Figma'dan Tasarım Çıkarma ve Implement Etme Rehberi

Bu rehber, Figma tasarımından Compose Multiplatform koduna geçiş sürecinin tüm adımlarını anlatır.
Her design implementasyonunda bu rehberi takip et.

**Skill:** `ai/design/skills/figma-to-compose/SKILL.md` (Claude bu skill'i otomatik kullanır)

---

## Tam Workflow

```
Figma URL →
  [A0] HAZIRLIK    → URL parse + referans screenshot kaydet
  [A1] DETECT      → Element sınıflandır (image/vector/shape)
  [A2] EXTRACT     → Asset'leri indir (PNG + SVG)
  [A3] CONVERT     → SVG → XML Vector Drawable + doğrula
  [A4] TOKEN       → Design token'ları çıkar + eşleştir
  [A5] IMPLEMENT   → Compose ekranları yaz
  [A6] VALIDATE    → Otomatik doğrulama + screenshot karşılaştırma
  [A7] FIX         → Farkları düzelt → A6'ya dön
```

---

## Aşama 0: Hazırlık

### URL'den Bilgi Çıkarma
```
https://figma.com/design/:fileKey/:fileName?node-id=1-2
  → fileKey: /design/ sonrası segment
  → nodeId: node-id değeri (URL'de 1-2, MCP'de 1:2)
```

### Figma Bağlamı Alma (Paralel)
```
get_metadata(nodeId="0:1")           → Tüm frame/node ağacı
get_design_context(nodeId, dir)      → Kod + asset URL'leri
get_screenshot(nodeId)               → Görsel referans
get_variable_defs(nodeId)            → Design token'lar (varsa)
```

### Referans Screenshot Kaydetme
Figma MCP JSON-RPC ile screenshot'ı diske kaydet:
```bash
# screenshots/figma/ klasörüne PNG olarak kaydeder
# Detaylı script: ai/design/skills/figma-to-compose/SKILL.md → AŞAMA 0
```

---

## Aşama 1: DETECT — Element Sınıflandırma

Figma'da "image layer" yoktur. Her fotoğraf bir RECTANGLE'ın `fills[].type = "IMAGE"` özelliğidir.

| MCP Çıktısı | Element Tipi | Aksiyon |
|-------------|-------------|---------|
| `localhost/assets/abc.png` | Fotoğraf (raster) | PNG olarak indir |
| `localhost/assets/def.svg` veya `import { img }` | Vektör (ikon/logo) | SVG → XML Vector Drawable |
| CSS renk/gradient kodu | Shape | Compose kodu yaz, export etme |
| Büyük fotoğrafik alan | Raster IMAGE | PNG olarak indir |
| Küçük sembolik grafik ≤64px | Vektör SVG | SVG → XML Vector Drawable |

### Kritik Kural
**Fotoğrafı SVG olarak export etme!** SVG içine base64 `<image>` olarak gömülür — hâlâ raster, 3x daha büyük.

---

## Aşama 2: EXTRACT — Asset Çıkarma

### MCP ile Otomatik
```
get_design_context(nodeId, dirForAssetWrites="figma-assets/")
```

### REST API ile
```bash
python3 scripts/extract_assets.py \
  --token FIGMA_TOKEN \
  --file-key FILE_KEY \
  --node-id NODE_ID \
  --output ./figma-assets
```

### MCP Asset Kuralları (ZORUNLU)
1. MCP localhost URL döndürüyorsa → **doğrudan kullan**
2. Yeni ikon paketi **ekleme** — asset'ler Figma payload'ından gelir
3. Localhost source varsa **placeholder oluşturma**

---

## Aşama 3: CONVERT — Dönüştürme

### SVG → XML Vector Drawable
Python script ile batch dönüşüm. Dikkat:
- Regex: `\s+d="([^"]+)"` kullan (`id=` yakalamasın)
- `var(--fill-0, #color)` → sadece `#color`
- Path komutları valid mi: `M,L,H,V,C,S,Q,T,A,Z`

### İsimlendirme
```
hash → anlamlı isim
e89ceb2e... → ic_google
38aaf9eb... → img_hero_login
```

### Hedefe Kopyala
```bash
cp ic_xxx.xml composeApp/src/commonMain/composeResources/drawable/
cp img_xxx.png composeApp/src/commonMain/composeResources/drawable/
```

### Doğrula
```bash
python3 scripts/validate_svg_paths.py
```

---

## Aşama 4: TOKEN — Design Token Çıkarma

### MCP Çıktısından Okuma
```
bg-[#13EC49]        → AppColors: primary
text-[#0F172A]      → AppColors: textPrimary
font-[18px] Bold    → AppTypography: headlineSmall
```

### Eşleştirme Dosyaları
| Token Tipi | Dosya |
|-----------|-------|
| Renkler | `theme/AppColors.kt` |
| Typography | `theme/AppTypography.kt` |
| Spacing | `theme/AppSpacing.kt` |
| Shapes | `theme/AppShapes.kt` |
| Shadows | `theme/AppShadows.kt` |

### Doğrula
```bash
python3 scripts/validate_design_tokens.py
```

---

## Aşama 5: IMPLEMENT — Compose Kodu Yazma

### Layout Kuralları (ZORUNLU)
- **Max 20dp** doğrudan padding/spacing
- 20dp üstü → `Spacer(Modifier.weight(1f))` veya `Alignment`
- `Scaffold { innerPadding -> }` → innerPadding **mutlaka** uygula
- Hardcoded `Color(0xFF...)` **yasak** → `AppColors` / `MaterialTheme`

### Onboarding Ekran Kalıbı
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface)
        .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Spacer(modifier = Modifier.weight(1f))    // üst esnek
    // İllüstrasyon
    Spacer(modifier = Modifier.height(20.dp)) // max 20dp
    // Başlık + açıklama
    Spacer(modifier = Modifier.weight(1f))    // alt esnek
    // ProgressDots + PrimaryButton
}
```

### Asset Kullanımı
```kotlin
// Vektör ikon — MUTLAKA painterResource ile kullan, Canvas ile ÇIZME
Icon(
    painter = painterResource(Res.drawable.ic_xxx),
    contentDescription = null,
    tint = Color.Unspecified, // Renkli SVG ise Unspecified kullan
)

// Fotoğraf
Image(painter = painterResource(Res.drawable.img_xxx), contentScale = ContentScale.Crop, ...)

// Shape → Kod olarak yaz
Box(modifier = Modifier.clip(CircleShape).background(AppColors.primaryAlpha10))
```

### İkon — Canvas YASAK
**İkonları asla `Canvas {}` ile çizme.** Figma'dan SVG olarak export et, XML Vector Drawable'a çevir, `painterResource` ile kullan.

❌ **Yanlış:**
```kotlin
Canvas(modifier = Modifier.size(24.dp)) {
    drawCircle(color = Color.Red, radius = 12.dp.toPx())
    drawPath(...)
}
```

✅ **Doğru:**
```kotlin
// 1. Figma'dan SVG export et
// 2. XML Vector Drawable'a çevir → res/drawable/ic_xxx.xml
// 3. painterResource ile kullan
Icon(
    painter = painterResource(R.drawable.ic_xxx),
    contentDescription = null,
    tint = Color.Unspecified,
)
```

### Component'lerde Dual Icon Support
```kotlin
@Composable
fun MyComponent(
    icon: ImageVector? = null,        // Material icon
    iconPainter: Painter? = null,     // composeResources asset
)
```

---

## Aşama 6: VALIDATE — Doğrulama

### Tüm Katmanları Çalıştır
```bash
make validate-all
```

### Screenshot Karşılaştırma

**Compose screenshot yakalama (instrumented test):**
```bash
./gradlew :composeApp:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=org.example.project.screenshots.ScreenCaptureTest

adb pull /sdcard/Pictures/SCREEN_compose.png screenshots/compose/
```

**Pixel diff:**
```bash
python3 scripts/pixel_diff.py \
  screenshots/figma/SCREEN.png \
  screenshots/compose/SCREEN_compose.png \
  -o screenshots/diff/SCREEN_diff.png -t 5
```

**Claude görsel review:**
İki PNG'yi oku, fark tablosu oluştur:

| Özellik | Figma | Compose | Durum |
|---------|-------|---------|-------|
| Layout | ... | ... | ✅/❌ |
| Typography | ... | ... | ✅/❌ |
| Renkler | ... | ... | ✅/❌ |
| İkonlar | ... | ... | ✅/❌ |

---

## Aşama 7: FIX — Düzelt ve Tekrarla

Pixel diff > %5 veya Claude review'da ❌ varsa:
1. Farkları listele
2. Root cause: Asset? Token? Layout? Metin?
3. İlgili aşamaya dön ve düzelt
4. A6'yı tekrar çalıştır
5. Diff ≤ %5 olana kadar tekrarla

---

## Araçlar Özeti

| Araç | Komut |
|------|-------|
| SVG Validator | `python3 scripts/validate_svg_paths.py` |
| Token Validator | `python3 scripts/validate_design_tokens.py` |
| Pixel Diff | `python3 scripts/pixel_diff.py img1 img2 -o diff.png` |
| Asset Extractor | `python3 scripts/extract_assets.py` |
| Screenshot Capture | `./scripts/capture_screen.sh` |
| Full Validation | `make validate-all` |

---

## 12 Altın Kural

1. **Sınıfla, sonra kodla** — Önce image/vector/shape ayrımı
2. **Fotoğraflar raster kalır** — SVG'ye çevirme
3. **Vektörler SVG olur** — İkon/logo → XML Vector Drawable
4. **Shape'ler kod olur** — Düz renk/gradient → Compose Brush
5. **MCP URL'leri kutsaldır** — Localhost URL döndüyse direkt kullan
6. **Max 20dp padding** — Fazlası → `weight(1f)` / `Alignment`
7. **Hardcoded Color yasak** — `AppColors` / `MaterialTheme` kullan
8. **SVG'de `d=` vs `id=` dikkat** — Regex'te `\s+d=` kullan
9. **Scaffold innerPadding uygulanmalı** — Yoksa nav bar altında kalır
10. **Önce screenshot, sonra kod** — Referans PNG olmadan başlama
11. **%5 pixel diff threshold** — Üstü → düzelt, altı → geç
12. **Her image'ın fallback'i olmalı** — Placeholder + error state
13. **İkonları Canvas ile çizme** — Figma'dan SVG export et → XML Vector Drawable → `painterResource`
