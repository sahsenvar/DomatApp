---
name: figma-to-compose
description: >
  Complete Figma-to-Compose Multiplatform design implementation pipeline.
  Covers the ENTIRE flow: Figma URL parsing, asset classification (image/vector/shape),
  asset extraction & SVG conversion, design token extraction, Compose screen implementation,
  @Preview generation, screenshot capture, pixel diff comparison, and Claude visual review.
  TRIGGER: whenever user provides a Figma URL, mentions "implement design", "Figma to code",
  "design implementation", "tasarımı implement et", or asks to build UI matching Figma specs.
  This skill REPLACES implement-design, figma-image, and figma-compose-validation — it is the
  unified pipeline that combines all three.
---

# Figma → Compose Pipeline

Figma tasarımından Compose Multiplatform koduna kadar her adımı kapsayan 7 aşamalı pipeline.

**Bu skill'i her design implementasyonunda kullan. Adım atlama.**

---

## AŞAMA 0: HAZIRLIK

### Figma URL'den Bilgi Çıkar
```
URL: https://figma.com/design/:fileKey/:fileName?node-id=1-2
  → fileKey: /design/ sonrası segment
  → nodeId: node-id parametresi (URL'de 1-2, MCP'de 1:2)
```

### İlk Screenshot'ı Al ve Kaydet (Referans)
```python
# MCP JSON-RPC ile Figma screenshot'ını diske kaydet
python3 -c "
import json, base64, http.client
conn = http.client.HTTPConnection('127.0.0.1', 3845)
headers = {'Content-Type': 'application/json', 'Accept': 'application/json, text/event-stream'}

# Initialize
conn.request('POST', '/mcp', json.dumps({
    'jsonrpc': '2.0', 'id': 1, 'method': 'initialize',
    'params': {'protocolVersion': '2024-11-05', 'capabilities': {},
               'clientInfo': {'name': 'pipeline', 'version': '1.0'}}
}), headers)
resp = conn.getresponse()
session = resp.getheader('mcp-session-id')
resp.read()

# Initialized notification
conn2 = http.client.HTTPConnection('127.0.0.1', 3845)
h2 = dict(headers); h2['mcp-session-id'] = session
conn2.request('POST', '/mcp', json.dumps({'jsonrpc':'2.0','method':'notifications/initialized'}), h2)
conn2.getresponse().read()

# Screenshot
conn3 = http.client.HTTPConnection('127.0.0.1', 3845, timeout=30)
h3 = dict(headers); h3['mcp-session-id'] = session
conn3.request('POST', '/mcp', json.dumps({
    'jsonrpc': '2.0', 'id': 2, 'method': 'tools/call',
    'params': {'name': 'get_screenshot', 'arguments': {'nodeId': 'NODE_ID'}}
}), h3)
raw = conn3.getresponse().read().decode()
for line in raw.split('\n'):
    if line.startswith('data: '):
        data = json.loads(line[6:])
        for item in data.get('result', {}).get('content', []):
            if item.get('type') == 'image':
                with open('screenshots/figma/SCREEN_NAME.png', 'wb') as f:
                    f.write(base64.b64decode(item['data']))
                print('Saved!')
"
```

Veya MCP tool ile al (conversation'da görürsün ama dosyaya kaydetmez):
```
get_screenshot(nodeId="NODE_ID")
```

---

## AŞAMA 1: DETECT — Element Sınıflandırma

### MCP ile Sınıflandırma
```
get_metadata(nodeId) → Node ağacı (yapı analizi)
get_design_context(nodeId, dirForAssetWrites="figma-assets/") → Kod + asset URL'leri
```

### Sınıflandırma Kuralları

| MCP Çıktısı | Element Tipi | Format | Aksiyon |
|-------------|-------------|--------|---------|
| `localhost:3845/assets/abc.png` | Fotoğraf | PNG | `composeResources/drawable/` |
| `localhost:3845/assets/def.svg` | Vektör ikon | SVG→XML | `composeResources/drawable/` |
| `import { img } from "./svg-xxx"` | Vektör asset | SVG→XML | `composeResources/drawable/` |
| CSS renk/gradient kodu | Shape | Kod | `Brush`/`Canvas` composable |
| Büyük alan, fotoğrafik | Raster IMAGE | PNG | `painterResource` |
| Küçük sembolik grafik ≤64px | Vektör SVG | SVG→XML | `painterResource` |

### KRİTİK KURALLAR

**1. Fotoğrafı SVG olarak export etme!** SVG içine base64 `<image>` olarak gömülür — hâlâ raster, ama 3x daha büyük.

**2. SVG DOSYASI ANDROID'DE DESTEKLENMIYOR!**
`composeResources/drawable/` içine `.svg` dosyası koyarsan Android'de crash olur:
```
IllegalStateException: Android platform doesn't support SVG format.
```
**Her zaman XML Vector Drawable (`.xml`) kullan.** XML VD hem Android hem iOS'ta çalışır.

**3. IMAGE vs CODE Karar Ağacı:**
```
Element etkileşimli mi? (tıklanabilir parçalar var mı?)
  ├─ EVET → Compose kodu ile yaz
  └─ HAYIR → Aynı bölgede birden fazla dekoratif element mi?
      ├─ EVET → TEK XML VD olarak al (karmaşık illüstrasyonlar)
      └─ HAYIR → Basit shape mi?
          ├─ EVET → Compose kodu (Box + background/clip)
          └─ HAYIR → Ayrı XML VD ikon olarak al
```

**Örnekler:**
- Telefon çerçevesi + ikonlar + rozetler (dekoratif) → **Tek XML VD**
- Supply chain diyagramı (satırlar + ikonlar + text) → **Compose kodu + ayrı ikon XML VD'ler**
- Yeşil daire arka plan → **Compose kodu** (`Box + CircleShape + background`)
- QR kodu ikonu → **Ayrı XML VD**

---

## AŞAMA 2: EXTRACT — Asset Çıkarma

### MCP Asset Kuralları (ZORUNLU)
1. MCP localhost URL döndürüyorsa → **doğrudan kullan**
2. Yeni ikon paketi **ekleme** — tüm asset'ler Figma payload'ından gelir
3. Localhost source varsa **placeholder oluşturma**
4. Asset'leri **önce indir**, sonra implement et

### İndirme
```bash
# MCP otomatik indirme
get_design_context(nodeId, dirForAssetWrites="/path/to/figma-assets/")

# Veya REST API ile
python3 scripts/extract_assets.py --token TOKEN --file-key KEY --node-id NODE
```

### Çıktı
```
figma-assets/
├── hash1.png  → Fotoğraf (hash isimli)
├── hash2.svg  → Vektör ikon (hash isimli)
└── ...
```

---

## AŞAMA 3: CONVERT — Dönüştürme

### SVG → XML Vector Drawable

#### Tek İkon Dönüşümü (MCP SVG → XML VD)
MCP'den gelen SVG:
```xml
<svg viewBox="0 0 33 24" fill="none" xmlns="http://www.w3.org/2000/svg">
  <path d="M7.5 24C6.25 24..." fill="var(--fill-0, #0F172A)"/>
</svg>
```
XML Vector Drawable'a dönüşür:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="17.5dp"
    android:viewportWidth="33"
    android:viewportHeight="24">
    <path
        android:pathData="M7.5 24C6.25 24..."
        android:fillColor="#0F172A" />
</vector>
```

**Dönüşüm kuralları:**
- `viewBox="x y w h"` → `viewportWidth="w"` `viewportHeight="h"`
- `width/height` → dp olarak orantılı hesapla (genellikle viewportWidth bazında)
- `fill="var(--fill-0, #COLOR)"` → `android:fillColor="#COLOR"`
- `d="..."` → `android:pathData="..."` (birebir kopyala)
- Regex'te `\s+d="([^"]+)"` kullan (`id=` yakalamasın)
- `e/E` scientific notation'da kullanılır, path komutu değil

#### Karmaşık İllüstrasyon Dönüşümü (Birden Fazla Element → Tek XML VD)
Figma'daki birden fazla dekoratif elementi TEK XML VD'ye birleştirme:
```xml
<vector android:viewportWidth="320" android:viewportHeight="320">
    <!-- Ana shape -->
    <path android:pathData="circle..." android:fillColor="#1A13EC49" />

    <!-- Alt element (group ile pozisyonla) -->
    <group android:translateX="80" android:translateY="32">
        <path android:pathData="rect..." android:fillColor="#0F172A" />
        <!-- İç ikon path'i -->
        <group android:translateX="57.5" android:translateY="95.24">
            <path android:pathData="icon..." android:fillColor="#0F172A" />
        </group>
    </group>
</vector>
```

**Karmaşık VD'de dikkat:**
- `<group>` ile pozisyonlama: `translateX`, `translateY`
- Rounded rect path formatı: `M(x+rx),y L(x+w-rx),y A(rx,ry,0,0,1,...) ...Z`
- Shadow → XML VD'de **desteklenmez**, Compose tarafında `Modifier.shadow()` ekle
- Blur/filter → XML VD'de **desteklenmez**, Compose'da `Brush.radialGradient` ile simüle et

#### Platform Format Desteği

| Format | Android | iOS | commonMain | Kullan? |
|--------|---------|-----|------------|---------|
| XML Vector Drawable (.xml) | ✅ | ✅ | ✅ | **EVET — her zaman** |
| SVG (.svg) | ❌ CRASH | ✅ | ❌ | **HAYIR** |
| PNG (.png) | ✅ | ✅ | ✅ | Sadece fotoğraflar için |
| WebP (.webp) | ✅ | ✅ | ✅ | Sadece fotoğraflar için |

### İsimlendirme
```
hash → anlamlı isim
e89ceb2e... → ic_google          (tekil ikon)
34f5e994... → ic_shield_large    (tekil ikon)
38aaf9eb... → img_hero_login     (illüstrasyon/fotoğraf)
```

### Hedefe Kopyala
```bash
# Vektörler (XML VD)
cp ic_google.xml composeApp/src/commonMain/composeResources/drawable/

# Fotoğraflar (PNG)
cp img_hero.png composeApp/src/commonMain/composeResources/drawable/
```

### Doğrula
```bash
python3 scripts/validate_svg_paths.py
```

---

## AŞAMA 4: TOKEN — Design Token Çıkarma

### MCP Çıktısından Token Okuma
```
bg-[#13EC49]     → Primary: Color(0xFF13EC49)
text-[#0F172A]   → TextPrimary: Color(0xFF0F172A)
font-['Plus_Jakarta_Sans:Bold'] text-[18px] → headlineSmall = 18sp Bold
```

### Eşleştirme Dosyaları
| Figma Token | Kotlin Dosya |
|------------|-------------|
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

## AŞAMA 5: IMPLEMENT — Compose Kodu Yazma

### Ekran Yapısı
```kotlin
@Screen(XxxRoute::class, XxxViewModel::class)
@Composable
fun XxxScreen(
    state: XxxUiState,
    onIntent: (XxxUiIntent) -> Unit,
) {
    // Layout kurallarına uy
}
```

### Layout Kuralları (ZORUNLU)
- **Max 20dp** doğrudan padding/spacing
- 20dp'den fazla boşluk → `Spacer(Modifier.weight(1f))` veya `Alignment`
- `Scaffold { innerPadding -> }` → innerPadding **mutlaka** uygulanmalı
- Hardcoded `Color(0xFF...)` **yasak** → `AppColors` veya `MaterialTheme` kullan

### Asset Kullanımı
```kotlin
// Vektör ikon (from composeResources) — MUTLAKA painterResource, asla Canvas
Icon(
    painter = painterResource(Res.drawable.ic_shield_large),
    contentDescription = null,
    tint = Color.Unspecified, // Renkli SVG için
    modifier = Modifier.size(70.dp),
)

// Fotoğraf (from composeResources)
Image(
    painter = painterResource(Res.drawable.img_hero_login),
    contentDescription = null,
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop,
)

// Shape → Kod olarak yaz, asset olarak alma
Box(
    modifier = Modifier
        .size(200.dp)
        .clip(CircleShape)
        .background(AppColors.primaryAlpha10),
)
```

### İkon — Canvas KESİNLİKLE YASAK
**İkonları asla `Canvas {}` ile çizme.** Her ikon Figma'dan SVG olarak export edilmeli, XML Vector Drawable'a dönüştürülmeli ve `painterResource` ile kullanılmalıdır.

❌ **Yanlış — YAPMA:**
```kotlin
Canvas(modifier = Modifier.size(24.dp)) {
    drawPath(googlePath, color = Color(0xFF4285F4))
    // ... Canvas ile ikon çizmek yasak
}
```

✅ **Doğru — HEP BÖYLE YAP:**
```kotlin
// 1. figma_execute ile SVG export et:
//    figma.exportAsync(node, {format: "SVG"})
// 2. SVG → XML Vector Drawable'a çevir → res/drawable/ic_xxx.xml
// 3. Compose'da kullan:
Icon(
    painter = painterResource(R.drawable.ic_xxx),
    contentDescription = null,
    tint = Color.Unspecified, // Renkli ikonlarda Unspecified kullan
)
```

### Component'lerde Dual Icon Support
Component'ler hem `ImageVector` hem `Painter` kabul etmeli:
```kotlin
@Composable
fun FeatureListItem(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,        // Material icon
    iconPainter: Painter? = null,     // composeResources asset
)
```

---

## AŞAMA 6: VALIDATE — Doğrulama

### Otomatik Doğrulama (Tüm Katmanlar)
```bash
make validate-all
```

Bu komut sırayla çalıştırır:
1. `validate_svg_paths.py` — SVG path data doğrulama
2. `validate_design_tokens.py` — Token eşleştirme + hardcoded scan

### Compose Screenshot Yakalama
```bash
# Instrumented test ile Preview'ı render et → PNG olarak kaydet
./gradlew :composeApp:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=org.example.project.screenshots.ScreenCaptureTest

# Cihazdan PNG'yi çek
adb pull /sdcard/Pictures/SCREEN_NAME_compose.png screenshots/compose/
```

### Pixel Diff Karşılaştırma
```bash
python3 scripts/pixel_diff.py \
  screenshots/figma/SCREEN_NAME.png \
  screenshots/compose/SCREEN_NAME_compose.png \
  -o screenshots/diff/SCREEN_NAME_diff.png \
  -t 5
```

### Claude Görsel Review
İki PNG'yi oku ve şu tabloyu doldur:

| Özellik | Figma | Compose | Durum |
|---------|-------|---------|-------|
| Layout | ... | ... | ✅/❌ |
| Typography | ... | ... | ✅/❌ |
| Renkler | ... | ... | ✅/❌ |
| İkonlar/Görseller | ... | ... | ✅/❌ |
| Spacing | ... | ... | ✅/❌ |
| Buton text/style | ... | ... | ✅/❌ |

---

## AŞAMA 7: FIX — Farkları Düzelt

Pixel diff %5'ten fazla veya Claude review'da ❌ varsa:

1. Farkları listele (tablo formatında)
2. Her fark için root cause belirle:
   - **Asset eksik/yanlış** → AŞAMA 2-3'e dön
   - **Token yanlış** → AŞAMA 4'e dön
   - **Layout/spacing hatası** → AŞAMA 5'e dön
   - **Metin farklı** → Figma'daki metinleri oku, güncelle
3. Düzelt
4. AŞAMA 6'yı tekrar çalıştır
5. Diff ≤ %5 olana kadar tekrarla

---

## AŞAMA 5.5: POST-IMPLEMENTATION CHECKLIST (ZORUNLU)

İmplementasyon sonrası her element için şu kontrolleri yap:

### İkon Doğrulama
Her `Icon()` veya `Image(painterResource(...))` kullanımı için:
1. Figma'daki SVG asset URL'sini indir (`curl localhost:3845/assets/xxx.svg`)
2. SVG path data'yı mevcut drawable ile karşılaştır
3. **Eşleşmiyorsa** → yeni XML VD oluştur

### MATERIAL ICONS YASAĞI
**Figma tasarımlarında Material Icons (`Icons.Filled.*`, `Icons.Default.*`) KULLANMA.**
Figma'daki her ikon özel bir SVG'dir. Material Icons farklı görünür ve tasarımla eşleşmez.
Tek istisna: Figma'da açıkça Material Design ikonu kullanılmışsa.

### Button Variant Kontrolü
Her buton için Figma'daki şu değerleri doğrula:
- Text rengi (`text-white` vs `text-[#0f172a]`)
- Background rengi
- Border
- Shadow
- Trailing/leading icon

### Component State Kontrolü
Her component çağrısında tüm parametrelerin doğru geçildiğini kontrol et:
- `isLocked`, `isVerified`, `isActive` gibi state flag'leri
- `opacity` / `alpha` değerleri
- Figma'daki her property'nin kod karşılığı olmalı

---

## HIZLI REFERANS: 15 Kural

1. **Sınıfla, sonra kodla** — Önce image/vector/shape ayrımı, sonra implementasyon
2. **Fotoğraflar raster kalır** — SVG'ye çevirme, PNG/JPG kullan
3. **Vektörler XML VD olur** — İkon ve logolar SVG → XML Vector Drawable (.xml)
4. **SVG DOSYASI YASAK** — Android'de crash olur, her zaman XML VD kullan
5. **Shape'ler kod olur** — Düz renk ve gradient'leri code, export etme
6. **Karmaşık illüstrasyonlar tek XML VD** — Etkileşimsiz, dekoratif öğeler birleştirilir
7. **MCP localhost URL'leri kutsaldır** — MCP URL verdiyse direkt kullan
8. **Önce mevcut asset'leri kontrol et** — Path data karşılaştırarak eşleşen drawable ara
9. **Max 20dp doğrudan padding** — Fazlası için `weight(1f)` veya `Alignment`
10. **Hardcoded Color yasak** — Her zaman `AppColors` veya `MaterialTheme` token'ı
11. **SVG dönüşümde `d=` vs `id=` dikkat** — Regex'te `\s+d=` kullan
12. **Scaffold innerPadding uygulanmalı** — Yoksa içerik nav bar altında kalır
13. **Önce screenshot al, sonra kodla** — Referans PNG olmadan başlama
14. **%5 pixel diff threshold** — Üstündeyse düzelt, altındaysa geç
15. **Shadow XML VD'de yok** — Compose'da `Modifier.shadow()` ile ekle
16. **İkonları Canvas ile çizme** — Figma'dan SVG export et → XML Vector Drawable → `painterResource`; `Canvas {}` ile ikon çizmek KESİNLİKLE YASAK

---

## TAM WORKFLOW ÖZETİ

```
Figma URL →
  [A0] HAZIRLIK: URL parse + referans screenshot kaydet
  [A1] DETECT: get_metadata + get_design_context → element sınıflandır
  [A2] EXTRACT: Asset'leri indir (PNG + SVG)
  [A3] CONVERT: SVG → XML VD + doğrula (validate_svg_paths.py)
  [A4] TOKEN: Renk/typography çıkar + eşleştir (validate_design_tokens.py)
  [A5] IMPLEMENT: Compose ekranları yaz (layout kurallarına uy)
  [A6] VALIDATE: make validate-all + screenshot + pixel diff + Claude review
  [A7] FIX: Farkları düzelt → A6'ya dön (diff ≤ %5 olana kadar)
```

---

## ARAÇLAR

| Araç | Komut | Ne Yapar |
|------|-------|----------|
| Figma MCP | `get_design_context` | Kod + asset'ler |
| Figma MCP | `get_screenshot` | Görsel referans |
| Figma MCP | `get_metadata` | Node ağacı |
| SVG Validator | `python3 scripts/validate_svg_paths.py` | Path doğrulama |
| Token Validator | `python3 scripts/validate_design_tokens.py` | Token eşleştirme |
| Pixel Diff | `python3 scripts/pixel_diff.py` | Screenshot karşılaştırma |
| Screenshot | `./scripts/capture_screen.sh` | Compose → PNG yakalama |
| Asset Extract | `python3 scripts/extract_assets.py` | REST API asset çekme |
| Validate All | `make validate-all` | Tüm doğrulamaları çalıştır |
