---
name: figma-to-compose
description: >
  Implement a DomatApp screen in Compose from its approved design package (design/screens/<ID>/) and the
  design contract (design/README.md, design/components.yaml, design/tokens/DESIGN.md). Use whenever the
  task is "implement screen C4", "tasarımı koda dök", "B3'ü yaz", a Figma URL for a DomatApp screen, or
  building UI that must match a design. Verification is done with the design-verify skill.
---

# Tasarım paketi → Compose

Sözleşme kuralları `design/README.md`'de (numaralı). Bu skill süreci anlatır. Sırayla uygula, adım atlama.

## 0. Paketi oku (kod yazmadan önce)

1. `design/README.md` — kurallar (özellikle 4–6, 13–15).
2. `design/screens/<ID>/annotations.md` — yerleşim (sabit 844 mi, kayan mı), durum geçişleri, klavye, popup notları.
3. `card.yaml` — `regions`, `states`, `data.uiState`, `actions`, `navigation`, `api`, `strings`, `sampleData`.
4. `structure.json` — her durumda kullanılan örnekler ve property değerleri; `manifest` = `components.yaml` anahtarı.
5. `design/components.yaml` — `compose` sembolü, `props` eşlemesi, **`spec`** (iç ölçüler).
6. `states/*.png` — görsel referans (2x; 780 px = 390 dp).

Önce paketin kendi tutarlılığı: `python3 ai/design/scripts/check_design_texts.py <ID>`. ✗ ise koda başlama,
farkı raporla (kart kazanır).

Canlı Figma (`source.json`) yalnızca paketin kapsamadığı bir ayrıntı için. `get_design_context` React+Tailwind
döndürür; açıklama olarak oku, kopyalama.

## 1. Bileşen planı

- `structure.json`'daki tüm `manifest` değerlerini listele; her biri için `components.yaml → status`:
  - `existing` → doğrudan çağır.
  - `change` / `promote` / `new` → **önce `design-system-change` skill'i**, sonra ekran.
- Ekran dosyasında yeni görsel bileşen tanımlama. Tek istisna: paketin açıkça tarif ettiği ekran-özel
  öğe (ör. C2'deki 24 dp ilerleme göstergesi) — raporda gerekçesiyle belirt.
- Bir bileşen `spec`'ine uymuyor gibi görünüyorsa ekranda telafi etme (padding ekleyip kaydırma vb.);
  bileşeni `spec`'e göre düzelt ya da raporla.

## 2. Ekran iskeleti (Gezgin + MVI)

- Route: `card.yaml → route`; kenarlar `navigation`'dan (`:core:navigation` → `DomatGraph.kt`).
  `replaceTo`'da `clearUpTo` kart söylemiyorsa en makul noktayı seç ve raporla.
- `XxxUiState` ← `data.uiState`, `XxxIntent` ← `actions`, `XxxEffect` ← navigasyon/yan etkiler.
  Kartta olmayan ama davranış için gereken intent (ör. odak kaybı) eklenebilir — raporla.
- `@ViewModelOf`, `@Effects`, `@Screen` bağları; imza `ColumnScope.(S, (I) -> Unit)` (CLAUDE.md → Navigation).
- Yerleşim: `ScreenHeader` → gövde (`padding sp4`, `spacedBy sp4`) → `BottomActionBar` (`imePadding`).
  Sabit ekranda gövde `weight(1f)` + `verticalScroll`; kayan ekranda (hug) tüm içerik kayar.

## 3. Önizlemeler

- Her `states[]` için: `@Preview(name = "<ID>@<durum>", widthDp = 390, heightDp = <structure.json size[1]>)`.
  Yükseklik yanlışsa karşılaştırma boyut hatası verir.
- Sahte `UiState` kartın `sampleData`'sından; metinler PNG ile birebir.
- Popup içeren durum (menü açık): statik düzen (ör. `DomatDropdownOpenPreviewLayout`) (kural 6).
- Odak gerektiren görünüm: `FocusInteraction` taklidi; gerçek odak önizlemede yok (kural 4).

## 4. Token ve metin kuralları

| Tasarımda | Kodda |
|---|---|
| `color/<rol>` | `MaterialTheme.colorScheme.<rol>` / `MaterialTheme.domatColors.<rol>` |
| `spacing/spN` | `MaterialTheme.spacing.spN` |
| `radius/xs..xl`, `full` | `MaterialTheme.shapes.extraSmall..extraLarge`, `CircleShape` |
| text style `title-large` | `MaterialTheme.typography.titleLarge` |

- Yasak (ekran dosyası): `Color(0x…)`, `colorResource`, `RoundedCornerShape(<n>.dp)`, çıplak boşluk `dp`.
- Metinler: kartın `strings` anahtarlarıyla `strings.xml`; `stringResource(Res.string.x)`; konumsal `%1$s`.
- İkonlar: `components.yaml → icons`, Material Symbols Rounded, XML vector drawable (`.svg` Android'de çöker).

## 5. Doğrulama

`design-verify` skill'ini çalıştır. Kabul: derleme uyarısız, tarama temiz, tüm durumlar OK, fark sayfaları
gözle kontrol edildi.

## Son adım: retro (zorunlu)

- `ai/design/screen-metrics.yaml`'a ekran kaydı ekle (ilk render farkı, tur sayısı, metin hatası, core değişikliği).
- Karşılaşılan her hatayı `ai/design/learnings.yaml`'a yaz (aynı kök neden varsa `occurrences`++).
  Özellikle: paketten çıkaramadığın/tahmin ettiğin her şey bir `contract-gap` kaydıdır.
- `python3 ai/design/scripts/learnings.py check` geçmeli. Skill'leri kendin değiştirme.
