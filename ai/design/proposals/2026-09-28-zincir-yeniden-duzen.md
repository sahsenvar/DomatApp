# Retro önerisi — Zincirin yeniden düzenlenmesi (Cowork ↔ Claude Code)

**Tarih:** 28.09.2026 · **Hazırlayan:** Cowork · **Onay:** Sahan (28.09.2026, sohbette onaylandı)
**Uygulayan:** Claude Code (repo tarafı) · Cowork (Cowork skill'leri, proje dokümanları)

## Neden

- Cowork (Claude Projects) buluttan GitHub'a **yazamıyor**, yalnızca okuyabiliyor. Repo'ya iki yüzeyin birden yazması karışıklık yarattı (bulut klonu, patch'ler, yarım kalan `main`).
- Kart ve Figma işini Cowork yapıyordu ama skill'i (`figma-screens`) ve `builder.js` repo'daydı. Hata kaydı da repo'daydı. Yani skill'in sahibi ile onu çalıştıran ayrı yerlerdeydi.
- Akış ile kod arasında iki devir noktası vardı (kart ve Figma). Tek devir noktasına iniyoruz.

## Karar: sahiplik

| Adım | Skill | Sahibi | Nerede yaşar |
|---|---|---|---|
| Doküman → akış | — | Chat / Cowork | Proje dokümanları |
| Akış → kart | `flow-to-card` | Cowork | Cowork skill'i; kart taslağı Figma › Handoff notunda |
| Kart → Figma (+ prototip) | `card-to-figma` + `builder.js` | Cowork | Cowork skill'i; `builder.js` proje dokümanında |
| **Devir** | — | Cowork → Claude Code | Figma › **Handoff** sayfası |
| Figma → paket | `figma-to-package` | Claude Code | `.claude/skills/figma-to-package/` |
| Paket → Compose | `package-to-compose` | Claude Code | `.claude/skills/package-to-compose/` |
| UI + test + doğrulama | `ui-test-verify` | Claude Code | `.claude/skills/ui-test-verify/` |
| Bileşen kütüphanesi | `design-system-change` | Claude Code | değişmez |
| Retro | `design-chain-retro` | Claude Code (repo kaydı) + Cowork (iki kaydı birlikte okur) | değişmez |
| Tasarım incelemesi | — | Cowork | Dalı okur, Handoff notunun "İnceleme" bölümüne yazar |

Kural: **repo'ya yalnızca Claude Code yazar.** `design/cards/` repo'da kalır, çünkü kart kodun sözleşmesidir (strings, navigation, uiState/actions, states → `@Preview` adları, api). Ama kartın yazarı Cowork'tür. Claude Code kartı Handoff notundan **olduğu gibi** repo'ya işler.

## Değişiklikler (Claude Code uygular)

### R-1 · `figma-screens`'i böl

- §0 "Ön kontrol" ve §1 "Ekranları kur" bölümleri, **Tuzaklar** tablosuyla birlikte Cowork'e taşınır. Repo'dan silinir.
- `builder.js` repo'dan silinir. Cowork kopyası proje dokümanında: `claude/araclar/builder.js`.
  **Silmeden önce** bu dosyanın Cowork tarafında olduğunu Handoff notundan teyit et.
- §2 "Paketi dışa aktar" ve §3 → yeni **`figma-to-package`** skill'i. `export.js` onunla gider.
- `figma-to-package`'a yeni **§0 Devir** eklenir:
  1. Figma › Handoff sayfasında ilgili notu oku (`get_metadata` / `use_figma`).
  2. DURUM etiketini `kodlanıyor` yap.
  3. Notun "Kartlar" bölümündeki her `card: <ID>.yaml` metin katmanını `design/cards/<ID>.yaml`'a **birebir** yaz.
  4. `python3 ai/design/scripts/check_card.py <ID...>` çalıştır. Hata varsa kartı **düzeltme**: notun "Sorular" bölümüne yaz, kullanıcıya sor, dur. Kartın sahibi Cowork.
  5. Notta "Bileşen talebi" varsa önce `design-system-change`.

### R-2 · `figma-to-compose` → `package-to-compose`

- Yalnızca ad ve referanslar değişir; içerik aynı.

### R-3 · `design-verify` → `ui-test-verify` (test üretimi eklenir)

Mevcut doğrulamaya (Roborazzi, `compare_design_package.py`, `check_design_texts.py`, göz kontrolü) ek olarak şu testler üretilir:

- **Maestro:** kartın `acceptance` maddelerinden `maestro/domatapp/<ID>.yaml`. Her madde bir akış adımı ya da doğrulama olur.
- **Birim testleri:** ViewModel'de her `actions` → beklenen state / effect; her `navigation` kenarı → doğru navigator çağrısı.
- **Roborazzi:** kartın her `states` maddesi için `@Preview(name = "<ID>@<durum>")`. Mevcut kural 15, değişmez.
- Son adım: PR aç → Handoff notunun DURUM etiketi `incelemede · PR #<no>`. PR açıklamasına Handoff notunun node ID'si yazılır.
- Not: CLAUDE.md'deki "test source sets disabled" notu bu adımla güncellenmeli. Hangi modüllerde test açılacağı ayrı bir karardır; önce sor.

### R-4 · Kart şemasına `acceptance`

```yaml
acceptance:
  - { id: AC-1, text: "Geçerli 10 haneli numara girilince 'Kod Gönder' aktifleşir.", test: maestro }
  - { id: AC-2, text: "KVKK bağlantısına dokununca J4 açılır.", test: maestro }
  - { id: AC-3, text: "Geçersiz numarada hata metni j1_error_phone_invalid görünür.", test: unit }
```

- `test`: `maestro | unit | roborazzi | manual`.
- `check_card.py`: `acceptance` yoksa **uyarı** (hata değil; mevcut 47 kart geriye dönük bozulmasın). `id` benzersiz olmalı, `test` değeri listede olmalı.
- `domatapp-flow-to-card` (Cowork) bundan sonra her karta `acceptance` yazar. Kaynağı akış dokümanındaki edge case kararlarıdır.

### R-5 · Hata kaydı kuralı

`design/README.md` ve skill'lerin "retro" adımına eklenir:

> **Kayıt, düzeltilecek skill'in yanında durur, hatayı fark eden tarafın değil.**
> - Repo skill'lerinin (`figma-to-package`, `package-to-compose`, `ui-test-verify`, `design-system-change`, script'ler) hataları → `ai/design/learnings.yaml` (`L-…`).
> - Cowork skill'lerinin (`flow-to-card`, `card-to-figma`, `builder.js`) ve proje dokümanı kurallarının hataları → proje dokümanı `claude/DomatApp_Ogrenme_Kaydi_Cowork.md` (`CW-…`, aynı alanlar).
> - Hatayı bulan taraf sahibi değilse Handoff notuna yazar; sahibi kendi kaydına işler.
> - `stage_caused: card | figma` olan bir hata, kart ya da Figma çizimi kaynaklıysa Cowork'e aittir. Kart **şeması** ya da `check_card.py` kaynaklıysa repo'ya aittir.

Mevcut L-001…L-042 kayıtları repo'da kalır, taşınmaz.

### R-6 · `design/README.md` ve `CLAUDE.md`

- `design/README.md` tablosunda `cards/<ID>.yaml` satırı: "Yazarı Cowork (Figma › Handoff), repo'ya Claude Code işler. Kod için sözleşme."
- Zincir satırı: **akış → kart → Figma → [devir] → paket → Compose → UI/test doğrulama.**
- `CLAUDE.md` "Design → Code" bölümü: skill adları güncellenir, yukarıdaki sahiplik tablosunun kısa hali ve Handoff akışı eklenir:
  > Tasarım işi Figma › Handoff sayfasındaki bir notla gelir. Notu oku, DURUM etiketini güncelle, kartları birebir işle, açık konularda tahmin etme, sor.

### R-7 · Eksik öğrenim kayıtları

PR #21'e girmesi gereken **L-041** ve **L-042** girmedi (Cowork'ün Mac üzerinden yaptığı commit'te kaybolmuş). `ai/design/learnings.yaml` sonuna ekle, `learnings.py check` çalıştır. Metin Handoff notunda.

Bunlara ek olarak şu kayıt açılır (repo tarafı, süreç hatası):

- **L-043**: Cowork'ün yerel (Mac) köprüyle yaptığı commit, doğrulanmadan PR'a gitti. Önlem: repo'ya yalnızca Claude Code yazar (bu öneri).

## Cowork tarafında yapılacaklar (Cowork uygular)

- `domatapp-flow-to-card` skill'i güncellenir: çıktı yeri Figma › Handoff notu, `acceptance` alanı, repo'ya yazma yok.
- Yeni `card-to-figma` skill'i (figma-screens §0–1 + Tuzaklar + prototip yardımcıları). `builder.js` → `claude/araclar/builder.js`.
- `claude/DomatApp_Ogrenme_Kaydi_Cowork.md` açılır.
- Handoff not şablonu Figma'da (Handoff sayfası › "Şablon").

## Sıra

1. Cowork: skill'ler kaydedilir (Sahan onaylar) + `builder.js` projeye + şablon.
2. Claude Code: R-7 → R-1…R-6. Bu dosyayı `ai/design/proposals/2026-09-28-zincir-yeniden-duzen.md` olarak ekle. Tek PR.
3. İlk gerçek devir: Handoff · Akış J (PR #21 birleştikten sonra).
