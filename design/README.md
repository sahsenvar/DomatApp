# design/ — Tasarım → Kod sözleşmesi

Bu klasör, akıştan koda uzanan zincirin **ürün kurallarını** tutar. Skill'ler (`.claude/skills/`) işin *nasıl*
yapılacağını anlatır; *neyin doğru olduğu* burada yazar. Bir kural değişirse burası değişir, skill'ler buraya
referans verir.

| Yol | Ne | Doğru kaynak mı? |
|---|---|---|
| `tokens/DESIGN.md` | Renk, tipografi, boşluk, köşe token'ları | **Evet** — Compose teması ve Figma variables buradan türetilir |
| `components.yaml` | Figma bileşeni ↔ Compose sembolü ↔ parametre eşlemesi + `spec` (iç ölçüler) | **Evet** — bileşen kataloğu |
| `cards/<ID>.yaml` | Akış dokümanından türetilen ekran kartı | Yazarı Cowork (Figma › Handoff), repo'ya Claude Code işler. Kod için sözleşme. |
| `screens/<ID>/` | Onaylanmış tasarım paketi (görsel + yapı) | Figma'nın o anki anlık görüntüsü |

Zincir: **akış → kart → Figma → [devir] → paket → Compose → UI/test doğrulama.**

| Adım | Skill | Sahibi |
|---|---|---|
| Akış → kart | `flow-to-card` | Cowork (kart taslağı Figma › Handoff notunda) |
| Kart → Figma (+ prototip) | `card-to-figma` + `builder.js` | Cowork (proje dokümanında) |
| **Devir** | — | Cowork → Claude Code, Figma › **Handoff** sayfası |
| Figma → paket | `figma-to-package` | Claude Code |
| Paket → Compose | `package-to-compose` | Claude Code |
| UI + test + doğrulama | `ui-test-verify` | Claude Code |
| Bileşen kütüphanesi | `design-system-change` | Claude Code |
| Retro | `design-chain-retro` | Claude Code (repo kaydı) + Cowork (iki kaydı birlikte okur) |

**Repo'ya yalnızca Claude Code yazar.** Kart repo'da kalır çünkü kodun sözleşmesidir; Claude Code onu Handoff
notundan **olduğu gibi** işler, içeriğini değiştirmez. Gerekçe ve karar:
`ai/design/proposals/2026-09-28-zincir-yeniden-duzen.md`.

## Hata kaydı {#hata-kaydi}

**Kayıt, düzeltilecek skill'in yanında durur, hatayı fark eden tarafın değil.**
- Repo skill'lerinin (`figma-to-package`, `package-to-compose`, `ui-test-verify`, `design-system-change`,
  script'ler) hataları → `ai/design/learnings.yaml` (`L-…`).
- Cowork skill'lerinin (`flow-to-card`, `card-to-figma`, `builder.js`) ve proje dokümanı kurallarının hataları →
  proje dokümanı `claude/DomatApp_Ogrenme_Kaydi_Cowork.md` (`CW-…`, aynı alanlar).
- Hatayı bulan taraf sahibi değilse Handoff notuna yazar; sahibi kendi kaydına işler.
- `stage_caused: card | figma` olan bir hata, kart ya da Figma çizimi kaynaklıysa Cowork'e aittir. Kart
  **şeması** ya da `check_card.py` kaynaklıysa repo'ya aittir.

Mevcut L-001…L-042 kayıtları repo'da kalır, taşınmaz.

## Sözleşme kuralları

Her kuralın yanında onu doğuran öğrenim kaydı (`L-…`) var.

### Kart ve metin
1. **Kart kazanır.** Akış dokümanı ile kart çelişirse akış dokümanı; kart ile tasarım/kod çelişirse kart doğrudur.
   Fark bulan taraf düzeltir ve farkı raporlar. (L-003)
2. **Tasarımdaki her metin** kartın `strings`'inden, ortak `strings.xml`'den ya da kartın `sampleData`'sından gelir.
   Kontrol: `ai/design/scripts/check_design_texts.py`. (L-003, L-023)
3. **Her kart `sampleData` içerir** — tasarımda ve önizlemede görünen dinamik veri (isim, tutar, kod, liste);
   dinamik veri yoksa `sampleData: {}`. Kart kontrolü: `ai/design/scripts/check_card.py` (bileşenler manifestte,
   `str.` referansları tanımlı, odak durumu yok). (L-023, L-025)
3a. **Kart `acceptance` içerir** — `{ id: AC-n, text, test: maestro | unit | roborazzi | manual }`; kaynağı akış
   dokümanındaki edge case kararlarıdır. `ui-test-verify` testleri bu maddelerden üretir. Eksikse
   `check_card.py` uyarır (eski kartlar için hata değil); `id` benzersiz, `test` listede olmalı.

### Durumlar
4. **Odak (focus) bir ekran durumu değildir.** Tasarım durumlarında alanlar odaksız çizilir. Zorunlu hallerde
   (ör. açık menünün anchor'ı) önizleme odağı `FocusInteraction` ile taklit eder. (L-007)
5. **Kodun davranışı Figma varyantı olarak modellenir.** Örnek: M3'te boş ve odaksız metin alanında etiket
   alanın içinde durur → `Input/TextField` `state=empty`. Boş alan `default` ile çizilmez. (L-015)
6. **Popup'lar (menü, diyalog, bottom sheet) Robolectric ekran görüntüsüne girmez.** Açık durumun önizlemesi
   statik bir düzenle yapılır (ör. `DomatDropdownOpenPreviewLayout`) ve tasarımda da akış içinde çizilir. (L-016)

### Bileşenler
7. **Önce `spec`, sonra Figma ve kod.** Yeni ya da değişen bileşenin iç ölçüleri (dolgu, aralık, ikon boyutu,
   yazı stili, kenarlık) `components.yaml → spec`'e yazılır; Figma ve Compose aynı değerlerle üretilir. Biri
   değişirse ikisi aynı commit'te değişir. (L-005)
8. **`existing` bir bileşenin Figma karşılığı koddaki gerçek görünümden türetilir.** Görünüm değişecekse bileşen
   `change` olarak işaretlenir. (L-001)
9. **Değişen/taşınan bileşenin etkilediği ekranlar listelenir** ve hepsinin görsel karşılaştırması yeniden
   çalıştırılır. (L-022)
10. **Dokunma hedefi en az 48 dp** — tasarımda da (TextLink, checkbox satırı, liste öğesi). (L-006)
11. **Pasif (disabled) içerik:** on-surface **%38**. Figma'da **düğüm opaklığı** ile verilir, dolgu opaklığıyla
    değil (değişkene bağlı dolgunun opaklığı örneklere geçmez). (L-011)
12. **İkonlar tek set:** Material Symbols Rounded (Apache 2.0). Özel çizim ikon eklenmez. (L-021)

### Kod
13. Ekran dosyasında `Color(0x…)`, `colorResource`, `RoundedCornerShape(<n>.dp)`, çıplak boşluk `dp` yok.
    Bileşen içi **boyutlar** (yükseklik, ikon boyutu) yalnızca bileşen dosyasında.
14. Metinler kartın `strings` anahtarlarıyla `core/resource/.../values/strings.xml`'e.
15. Her tasarım durumu için `@Preview(name = "<ID>@<durum>", widthDp = 390, heightDp = <structure.json size[1]>)`.

### Doğrulama (kabul)
16. `compare_design_package.py <ID>`: her durum ≤ **%3,5** (kalibrasyon: tüm ekran 2 dp kayması ≈ %3).
17. Metrik alan tabanlıdır; küçük alandaki renk/opaklık farkını kaçırır. **Fark sayfasına her zaman bakılır.** (L-019)
18. `check_design_texts.py <ID>` geçer; derleme uyarısız geçer.

## Kod yazarken sıra
1. `screens/<ID>/annotations.md` → `card.yaml` → `structure.json` oku; görseli en son doğrulama için kullan.
2. `structure.json`'daki her `manifest` değeri için `components.yaml`'daki Compose sembolünü **çağır**. Yeniden yazma.
3. `status: new` / `change` / `promote` bileşenler önce `:core:presentation`'da yazılır/değiştirilir.

Figma dosyası: https://www.figma.com/design/5hpUTmJJ1ie3E9boGSPXje (bkz. `screens/<ID>/source.json`).
