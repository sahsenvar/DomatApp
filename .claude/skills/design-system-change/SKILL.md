---
name: design-system-change
description: >
  Add, change, promote or migrate a DomatApp shared UI component or design-system element (component,
  icon set, token) so that design/components.yaml, the Figma library and the Compose code stay identical.
  Use for components with status new|change|promote, "bileşen ekle", "ikon setini değiştir",
  "LocationSelection'ı yeni bileşenlere geçir", token changes, or any edit under :core:presentation/component.
---

# Tasarım sistemi değişikliği (spec → Figma → kod → etkilenen ekranlar)

Kurallar: `design/README.md` kural 7–12. Temel ilke: **bileşenin iç ölçüleri sözleşmenin parçasıdır;
Figma ve kod aynı `spec`'ten üretilir, biri değişirse ikisi aynı commit'te değişir.**

## 1. Sınıflandır

| Durum | Anlamı | Figma'nın kaynağı |
|---|---|---|
| `new` | Kodda yok | `spec` (önce yazılır) |
| `change` | Kodda var, görünüm/API değişecek | `spec` (değişiklik önce oraya) |
| `promote` | Feature modülünde var, core'a taşınacak | Kodun **mevcut** görünümü; görünüm değişecekse ayrıca `change` |
| `existing` | Değişmiyor | Kodun gerçek görünümü (kural 8, L-001) |
| token / ikon seti | `DESIGN.md` / `components.yaml → icons` | O dosya |

## 2. `spec`'i yaz (önce)

`design/components.yaml` girdisi: `status`, `compose`, `figma {name, nodeId}`, `props` (Figma property →
Kotlin parametresi, adlar birebir), **`spec`**: dolgu, aralık, boyutlar, yazı stilleri, kenarlık, renk
rolleri, durumlar (empty/disabled/error…). Kodun M3 davranışı varsa (boş alanda etiket içeride, odakta
kenarlık) bunu varyant olarak yaz (kural 5).

## 3. Etkilenen ekranlar {#etkilenen-ekranlar}

Kod değişmeden önce listele (L-022):
```bash
grep -rln "<ComposeSembolü>(" --include=*.kt feature/ core/      # kodda kullananlar
grep -l '"manifest": "<Anahtar>"' design/screens/*/structure.json # paketlerde kullananlar
```
Figma'da kullanan çerçeveler: use_figma ile `findAll(n => n.type==='INSTANCE' && main.parent.name==='<Figma adı>')`.
Liste rapora ve `components.yaml → usedIn`'e yazılır.

## 4. Figma

[`ai/design/figma-plugin-api.md`](../../../ai/design/figma-plugin-api.md) tuzak tablosuna uy (klon referansları, resize, opaklık…). Bileşen seti:
property adları Kotlin parametreleriyle birebir; renk/boşluk/köşe yalnızca değişken; pasif içerik düğüm
opaklığı 0,38; dokunma hedefi ≥ 48 dp. Sonra `get_screenshot` ile varyantları gözle kontrol et.

## 5. Kod

`:core:presentation/component/<paket>/`; parametreler `props` ile birebir; ölçüler `spec`'ten; her varyant
için `@Preview`. `promote`'ta eski kopyayı sil ve çağıranları taşı.

## 6. Etkilenen ekranları yeniden doğrula

- Figma: etkilenen çerçevelerin PNG'lerini yeniden indir (örnekler güncellenir, PNG'ler güncellenmez).
- Kod: `ui-test-verify` → etkilenen **tüm** paketler. Paketi olmayan eski ekranlar (ör. LocationSelection)
  için önizleme görüntüsünü önce/sonra karşılaştır ve raporla.

## Örnek iş: ikon seti birleştirme (kural 12)

`components.yaml → icons` Material Symbols Rounded. Eski özel çizim ikonları (`ic_arrow_back`, `ic_lock`)
Figma'daki `Icon/<ad>` SVG'sinden XML vector drawable'a çevrilir (aynı dosya adı → çağıranlar değişmez).
Etkilenen ekranlar listelenir, tüm paketler yeniden doğrulanır. Tamamlanınca L-021 `mitigated`.

## Son adım: retro (zorunlu)

Kayıt, düzeltilecek skill'in yanında durur, hatayı fark eden tarafın değil (`design/README.md` → Hata kaydı).
Bu skill'in, bileşen kütüphanesinin ya da script'lerin hataları → `ai/design/learnings.yaml`; Cowork
skill'lerinin (`card-to-figma`, `builder.js`) hataları → Handoff notuna yaz, Cowork kendi kaydına işler.
`components.yaml`'da `note`/`usedIn` güncel olsun. `python3 ai/design/scripts/learnings.py check` geçmeli.
