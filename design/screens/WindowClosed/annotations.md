# Pencere Kapandı · Davranış notları

## Paketi okuma sırası (kod AI'ı için)
1. `card.yaml` → amaç, bölgeler, durumlar, `uiState`/`actions`, navigasyon, metin anahtarları, `sampleData`.
2. `structure.json` → her durumda hangi bileşen hangi property değerleriyle. `manifest` = `design/components.yaml` anahtarı (Compose sembolü + `spec`).
3. `states/*.png` → görsel referans (2x, 780 px = 390 dp). Karşılaştırma: `@Preview(name = "<ID>@<durum>", heightDp = structure.json size[1])` + `compare_design_package.py`.

## Yerleşim
- Başlık çubuğu ve geri yok (`noBack`). Gövde dikeyde **ortalı** `StatusHero neutral`; altta `BottomActionBar` + `PrimaryButton` "Pazar'a Dön".

## Davranış
- Kayıt geri alınmaz, kullanıcı giriş yapmış kalır; sepet cihazda durur. "Pazar'a Dön" → `replaceTo` Pazar.
- Metin Akış B'nin B3 Edge Case 2 diliyle birebir aynı olmalı (kart `strings`).
