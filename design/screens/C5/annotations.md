# C5 — Sipariş Onay · Davranış notları

## Paketi okuma sırası (kod AI'ı için)
1. `card.yaml` → amaç, bölgeler, durumlar, `uiState`/`actions`, navigasyon, metin anahtarları, `sampleData`.
2. `structure.json` → her durumda hangi bileşen hangi property değerleriyle. `manifest` = `design/components.yaml` anahtarı (Compose sembolü + `spec`).
3. `states/*.png` → görsel referans (2x, 780 px = 390 dp). Karşılaştırma: `@Preview(name = "<ID>@<durum>", heightDp = structure.json size[1])` + `compare_design_package.py`.

## Yerleşim
- **Başlık çubuğu ve geri yok** (`noBack`). Gövde kaydırılır (yükseklik içeriğe göre: first-order 911, addition 851) + `BottomActionBar · actions` sabit altta.
- Gövde: `StatusHero success` → `FormSection · delivery` (2 × InfoRow) → `FormSection · payment` (SummaryRow emphasis + InlineNote) → `CommunityProgressCard`.

## Durumlar
- `first-order`: "Siparişin alındı!" + `SecondaryButton` "WhatsApp'ta Paylaş" (`leadingIcon = ic_share`; marka logosu çizilmez) + `GhostButton` "Pazar'a Dön".
- `addition`: "Eklemen tamamlandı!", WhatsApp butonu **yok**; topluluk kartı iki siparişin toplam etkisini gösterir (örnekte %75).

## Davranış
- "Pazar'a Dön" → `replaceTo` Pazar (geri yığını temizlenir). Paylaş → WhatsApp intent (efekt).
- Hero başlığı ekran okuyucuda ilk odak (heading).
