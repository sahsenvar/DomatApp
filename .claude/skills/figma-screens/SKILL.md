---
name: figma-screens
description: >
  Design a DomatApp screen in Figma from its screen card (design/cards/<ID>.yaml) using only library
  components, then export the design package to design/screens/<ID>/. Use for "C3'ü Figma'da tasarla",
  "B akışının ekranlarını çiz", "tasarım paketini dışa aktar", or any card → Figma → package work.
  Requires the Figma MCP (use_figma, download_assets). Does not write Compose code (see figma-to-compose).
---

# Kart → Figma → tasarım paketi

Sözleşme kuralları `design/README.md`'de; bu skill yalnızca **süreci** ve **araç tuzaklarını** anlatır.
Figma dosyası: `5hpUTmJJ1ie3E9boGSPXje` · ekran sayfası: `Screens · Akış <X>` · kütüphane: aynı dosya.
Figma MCP günlük 200 çağrı; bir ekran grubu ≈ 10–20 çağrı. Çağrıları grupla.

## 0. Ön kontrol (Figma'ya dokunmadan)

1. `design/cards/<ID>.yaml` oku. Şunlar yoksa **dur ve kartı düzelt/raporla** (L-023):
   `regions` (her biri `component`), `states`, `strings`, **`sampleData`**, `navigation`.
2. Karttaki her `component` `design/components.yaml`'da var mı? Yoksa ya da `status: new|change` ise önce
   `design-system-change` skill'ini çalıştır. Ekrana kütüphane dışı görsel parça **eklenmez**.
3. Karttaki durumlar arasında `focused` gibi bir etkileşim durumu varsa çıkar (kural 4).

## 1. Ekranları kur (`builder.js`)

- `builder.js`'i use_figma çağrısının başına **olduğu gibi** yapıştır; ekranları altına tanımla. Helper'lar
  bilinen tuzakları zaten önler (L-010, L-011, L-014). Kendi yardımcını yazma, gerekirse builder'ı genişlet.
- Çerçeve adı `<ID>@<durum>`; katman adları `<Bileşen> · <bölge-id>` (bölge-id = karttaki `regions[].id`);
  düz metin `text · <bölge-id>`; FormSection ve BottomActionBar **çerçevedir**.
- Metinler **yalnızca** kartın `strings`'i ve `sampleData`'sından (kural 2). Metni elle uydurma.
- Boş alanlar `Input/TextField state=empty`; pasif butonlar `enabled=false` varyantı (opaklığı elle verme).
- Popup'lar (menü, diyalog) akış içinde çizilir (kural 6).
- Bir çağrıda en fazla bir ekranın tüm durumları; sonra `get_screenshot` ile en karmaşık durumu **gözle** kontrol et.

## 2. Paketi dışa aktar

1. **structure** — `export.js` (ONLY + MAN ayarla). Çıktı 20 KB sınırı: `TOO_LONG` dönerse ekranı böl (L-013).
   Dönen JSON'u `/tmp/<ID>_raw.json`'a yaz, sonra:
   `python3 ai/design/scripts/build_structure.py /tmp/<ID>_raw.json`
   Manifestte olmayan örnek uyarısı çıkarsa düzelt (kütüphane dışı parça var demektir).
2. **PNG** — her durum için `download_assets(nodeId, defaultFormat: png, defaultScale: 2)` →
   `curl -sL -o design/screens/<ID>/states/<durum>.png <url>` (780 px genişlik = 390 dp).
3. **card.yaml** — `cp design/cards/<ID>.yaml design/screens/<ID>/card.yaml`.
4. **source.json** — fileKey, sayfa, `frames: {durum: nodeId}`, `exportedAt`, kart ve manifest sürümü (örnek: `design/screens/C3/source.json`).
5. **annotations.md** — görselde görünmeyen davranış: yerleşim (sabit 844 mi, kayan mı), durum geçişleri,
   klavye/odak sırası, erişilebilirlik, popup notu. Örnek: `design/screens/C3/annotations.md`.
6. **Kontrol:** `python3 ai/design/scripts/check_design_texts.py <ID>` → ✓ olmadan paket bitmiş sayılmaz.

## 3. Bir şeyi düzeltince

Kütüphane bileşeni değiştiyse **etkilenen tüm ekranların** PNG'lerini yeniden indir (örnekler otomatik
güncellenir, PNG'ler güncellenmez). Hangi çerçevelerin etkilendiğini use_figma ile bul, listeyi raporla.

## Tuzaklar (Figma Plugin API)

| Kayıt | Tuzak | Ne yap |
|---|---|---|
| L-018 | Hata veren use_figma çağrısı **tamamen geri alınır**. | Adımları tekrar çalıştırılabilir yaz (önce var mı kontrol et). |
| L-009 | Varyant `clone()` edilince `componentPropertyReferences` kopyalanmaz. | Klondan sonra tüm referansları yeniden bağla ve metinleri kontrol et. |
| L-010 | `resize()` auto-layout boyutlandırmasını FIXED yapar. | `fixWidth()` kullan ya da sonra `*SizingMode='AUTO'`. |
| L-011 | Değişkene bağlı dolgunun `opacity`'si örneklere geçmez. | Opaklığı **düğüm** üzerinde ver. |
| L-012 | `resetOverrides()` katman adını da sıfırlar. | Önce adı sakla, sonra geri yaz. Mümkünse hiç kullanma. |
| L-013 | use_figma çıktısı 20 KB. | Dışa aktarımı ekran başına böl. |
| L-014 | Düğümlere özel alan eklenemez (`node._x`). | `Map` kullan (builder'daki `meta`). |
| L-020 | `opacity: 0` ile gizlenen metin "görünür" sayılır, pakete sızar. | Gizlemek için `visible = false`. |
| — | Yazı tipi yüklenmeden metin düzenlenemez. | builder'ın ilk satırı fontları yükler; ayrı çağrılarda da yükle. |

## Son adım: retro (zorunlu)

İş bitince bu oturumda karşılaşılan her hatayı `ai/design/learnings.yaml`'a yaz:
- Aynı kök neden zaten kayıtlıysa `occurrences`'ı artır ve `seen_in`'e ekle; yeni kayıt açma.
- Yeniyse `python3 ai/design/scripts/learnings.py next-id` ile kimlik al; `stage_caused` hatanın yapıldığı,
  `stage_detected` fark edildiği aşama.
- `python3 ai/design/scripts/learnings.py check` geçmeli.
Hata olmadıysa bir şey yazma. Skill'i kendin değiştirme — değişiklik `design-chain-retro` ile önerilir.
