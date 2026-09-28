---
name: figma-to-package
description: >
  Take over a DomatApp design handoff from the Figma Handoff page and export the design package to
  design/screens/<ID>/: read the Handoff note, set its DURUM label, write its cards verbatim to
  design/cards/, run check_card.py, then export structure.json, state PNGs, card.yaml, source.json and
  annotations.md. Use for "Handoff notunu oku", "Akış J devrini al", "tasarım paketini dışa aktar", or any
  Figma → package work. Requires the Figma MCP (use_figma, download_assets). Does not draw screens in Figma
  (that is Cowork's card-to-figma) and does not write Compose code (see package-to-compose).
---

# Figma › Handoff → tasarım paketi

Sözleşme kuralları `design/README.md`'de; bu skill yalnızca **süreci** anlatır.
Figma dosyası: `5hpUTmJJ1ie3E9boGSPXje` · devir sayfası: `Handoff` · ekranlar: `Screens` sayfası.
Figma MCP günlük 200 çağrı; bir ekran grubu ≈ 10–20 çağrı. Çağrıları grupla.

Kart ve Figma çizimi **Cowork'ündür** (`flow-to-card`, `card-to-figma`). Bu skill onları değiştirmez; yalnızca
devralır. Repo'ya yalnızca Claude Code yazar.

## 0. Devir

1. Figma › Handoff sayfasında ilgili notu oku (`get_metadata` / `use_figma`: notun tüm metin katmanları).
2. DURUM etiketini `kodlanıyor` yap (notun `status` çerçevesindeki metin).
3. Notun "Kartlar" bölümündeki her `card: <ID>.yaml` metin katmanını `design/cards/<ID>.yaml`'a **birebir** yaz.
   Metni `characters` olarak al, elle yeniden yazma; yazdıktan sonra uzunluk/özet karşılaştırmasıyla doğrula.
4. `python3 ai/design/scripts/check_card.py <ID...>` çalıştır. Hata varsa kartı **düzeltme**: notun "Sorular"
   bölümüne yaz, kullanıcıya sor, dur. Kartın sahibi Cowork.
5. Notta "Bileşen talebi" varsa önce `design-system-change`.

Notta belirsiz ya da eksik bir şey varsa (hangi ekran, hangi durum, hangi davranış) tahmin etme: "Sorular"
bölümüne yaz ve sor.

## 1. Paketi dışa aktar

use_figma çağrılarında [`ai/design/figma-plugin-api.md`](../../../ai/design/figma-plugin-api.md)
tablosuna uy (özellikle L-013 çıktı sınırı, L-018 geri alma, L-020 gizli metin).

1. **structure** — `export.js` (ONLY + MAN ayarla). Çıktı 20 KB sınırı: `TOO_LONG` dönerse ekranı böl (L-013).
   Dönen JSON'u `/tmp/<ID>_raw.json`'a yaz, sonra:
   `python3 ai/design/scripts/build_structure.py /tmp/<ID>_raw.json`
   Manifestte olmayan örnek uyarısı çıkarsa bu bir Figma çizim hatasıdır (kütüphane dışı parça): Handoff
   notuna yaz, Cowork düzeltir.
2. **PNG** — her durum için `download_assets(nodeId, defaultFormat: png, defaultScale: 2)` →
   `curl -sL -o design/screens/<ID>/states/<durum>.png <url>` (780 px genişlik = 390 dp).
3. **card.yaml** — `cp design/cards/<ID>.yaml design/screens/<ID>/card.yaml`.
4. **source.json** — fileKey, sayfa, `frames: {durum: nodeId}`, `exportedAt`, kart ve manifest sürümü, Handoff
   notunun node ID'si (örnek: `design/screens/C3/source.json`).
5. **annotations.md** — görselde görünmeyen davranış: yerleşim (sabit 844 mi, kayan mı), durum geçişleri,
   klavye/odak sırası, erişilebilirlik, popup notu. Örnek: `design/screens/C3/annotations.md`.
6. **Kontrol:** `python3 ai/design/scripts/check_design_texts.py <ID>` → ✓ olmadan paket bitmiş sayılmaz.

Sonra `package-to-compose`.

## 2. Bir şeyi düzeltince

Kütüphane bileşeni değiştiyse **etkilenen tüm ekranların** PNG'lerini yeniden indir (örnekler otomatik
güncellenir, PNG'ler güncellenmez). Hangi çerçevelerin etkilendiğini use_figma ile bul, listeyi raporla.

## Son adım: retro (zorunlu)

Kayıt, düzeltilecek skill'in yanında durur, hatayı fark eden tarafın değil (`design/README.md` → Hata kaydı).
- Bu skill'in, `export.js`'in, script'lerin ya da kart **şemasının** hatası → `ai/design/learnings.yaml`:
  aynı kök neden varsa `occurrences`++ ve `seen_in`'e ekle; yeniyse
  `python3 ai/design/scripts/learnings.py next-id`. `python3 ai/design/scripts/learnings.py check` geçmeli.
- Kartın **içeriği** ya da Figma **çizimi** kaynaklı hata → Cowork'ündür: Handoff notuna yaz, Cowork kendi
  kaydına (`CW-…`) işler.
Hata olmadıysa bir şey yazma. Skill'i kendin değiştirme — değişiklik `design-chain-retro` ile önerilir.
