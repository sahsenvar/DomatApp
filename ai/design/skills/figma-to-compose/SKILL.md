---
name: figma-to-compose
description: >
  Implement a DomatApp screen in Compose from its approved design package (design/screens/<ID>/)
  and the design contract (design/tokens/DESIGN.md, design/components.yaml). Use whenever the task is
  "implement screen C4", "tasarımı koda dök", a Figma URL for a DomatApp screen, or building UI that
  matches a design. Replaces the March 2026 Figma-first pipeline.
---

# Tasarım paketi → Compose

Sırayla uygula. Adım atlama.

## 0. Sözleşmeyi oku (kod yazmadan önce)

1. `design/README.md` — kurallar.
2. `design/screens/<ID>/annotations.md` — davranış, yerleşim, durum geçişleri.
3. `design/screens/<ID>/card.yaml` — bölgeler, `states`, `data.uiState`, `actions`, `navigation`, `api`, `strings`.
4. `design/screens/<ID>/structure.json` — her durumda kullanılan bileşen örnekleri ve property değerleri.
   Her örneğin `manifest` alanı `design/components.yaml` anahtarıdır.
5. `design/components.yaml` — `manifest` → Compose sembolü + Figma property → Kotlin parametre eşlemesi.
6. `design/tokens/DESIGN.md` — token'lar ve Do/Don't.
7. `states/*.png` — görsel referans (2x; 780 px = 390 dp).

Canlı Figma (`source.json` → fileKey) yalnızca paketin kapsamadığı bir ayrıntı için kullanılır
(`get_design_context` React+Tailwind döndürür; onu yalnızca açıklama olarak oku, kopyalama).

## 1. Bileşen planı

- `structure.json`'daki tüm `manifest` değerlerini listele.
- Her biri için `components.yaml` → `status`:
  - `existing` → doğrudan çağır.
  - `change` → önce bileşende belirtilen değişikliği yap (ör. `PrimaryButton(loading)`), sonra çağır.
  - `promote` → feature modülündeki bileşeni `:core:presentation/component/`'a taşı, sonra çağır.
  - `new` → `:core:presentation/component/<paket>/` altında yaz; Figma property'leri = Kotlin parametreleri.
- **Ekran dosyasında yeni görsel bileşen tanımlama.** Tekrar eden her görsel parça ya kataloğa girer ya da
  ekran-özel olduğu `components.yaml`'a not düşülerek gerekçelendirilir.
- `FormSection` ve `BottomActionBar` Figma'da frame'dir (`kind: frame`); kodda bileşendir.

## 2. Ekran iskeleti (Gezgin + MVI)

- Route: `card.yaml → route` (önerilen ise `:core:navigation`'daki grafiğe ekle, kenarları `navigation`'dan).
- `XxxUiState` alanları `data.uiState`'ten, `XxxIntent` `actions`'tan, `XxxEffect` navigasyon/yan etkilerden.
- `@ViewModelOf(Route::class)`, `@Effects(Route::class)`, `@Screen(Route::class)` bağları; imza
  `ColumnScope.(S, (I) -> Unit)` (bkz. CLAUDE.md → Navigation).
- Her `states[]` durumu için bir `@Preview` (ör. `C4FirstOrderPreview`) — durum başına sahte `UiState`.

## 3. Token kuralları (kesin)

| Tasarımda | Kodda |
|---|---|
| `color/<rol>` | `MaterialTheme.colorScheme.<rol>` veya `MaterialTheme.domatColors.<rol>` (success/warning) |
| `spacing/spN` | `MaterialTheme.spacing.spN` |
| `radius/xs..xl`, `full` | `MaterialTheme.shapes.extraSmall..extraLarge`, `CircleShape` |
| text style `title-large` | `MaterialTheme.typography.titleLarge` |

Yasak: `Color(0x…)`, `colorResource(...)`, `RoundedCornerShape(<n>.dp)`, çıplak boşluk `dp`, stil dışı
`fontSize`/`fontWeight` kopyaları. Bileşen içi **boyutlar** (yükseklik, ikon boyutu, kenarlık kalınlığı) serbest.

## 4. Metin ve ikon

- Metinler: `card.yaml → strings` anahtarlarıyla `core/resource/src/commonMain/composeResources/values/strings.xml`.
  Konumsal biçim `%1$s`, `%1$d`. Ekranda `stringResource(Res.string.x)`.
- İkonlar: `components.yaml → icons`. `painterResource(Res.drawable.ic_<ad>)`. Yeni ikon gerekiyorsa
  Figma `Icon/<ad>` SVG'si → XML Vector Drawable (`.svg` dosyası Android'de çöker). `Canvas` ile ikon çizme.

## 5. Doğrulama

1. `./gradlew :composeApp:assembleDebug` (uyarılar hata sayılır).
2. Tarama (0 sonuç beklenir, bileşen dosyaları hariç):
   `grep -rnE "Color\(0x|colorResource\(|RoundedCornerShape\([0-9]" <ekran paketi>`
3. Her durum için preview render → `ai/design/scripts/pixel_diff.py states/<durum>.png <render>.png` (hedef ≤ %5)
   ve aşağıdaki tabloyu doldur:

| Özellik | Tasarım | Compose | ✅/❌ |
|---|---|---|---|
| Bölge sırası (`card.yaml regions`) | | | |
| Kullanılan bileşenler (`structure.json`) | | | |
| Renk/token | | | |
| Tipografi | | | |
| Boşluklar | | | |
| Metinler | | | |
| Durum kapsaması (`states`) | | | |

4. ❌ varsa kök nedene dön (bileşen / token / yerleşim / metin), düzelt, tekrar doğrula.

## Eski boru hattından kalanlar

`ai/design/scripts/*` (pixel_diff, extract_assets, validate_svg_paths…) kullanılabilir. `validate_design_tokens.py`
eski `AppColors` düzenini varsayabilir — sonucunu tek başına kabul kriteri sayma.
