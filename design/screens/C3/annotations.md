# C3 — Adres Ekleme · Davranış notları

## Paketi okuma sırası (kod AI'ı için)
1. `card.yaml` → amaç, bölgeler, durumlar, `uiState`/`actions`, navigasyon, metin anahtarları, `sampleData`.
2. `structure.json` → her durumda hangi bileşen hangi property değerleriyle. `manifest` = `design/components.yaml` anahtarı (Compose sembolü + `spec`).
3. `states/*.png` → görsel referans (2x, 780 px = 390 dp). Karşılaştırma: `@Preview(name = "<ID>@<durum>", heightDp = structure.json size[1])` + `compare_design_package.py`.

## Yerleşim
- `ScreenHeader` + gövde (sp4, öğeler arası sp4) + `BottomActionBar`. Gövde: `InfoRow · site` → `FormSection · address-form` → fatura satırı.
- Fatura satırı: ilk siparişte `InlineNote neutral` ("Fatura bilgin bir sonraki adımda alınacak."), tekrar siparişte `InfoRow` + "Düzenle".

## Durumlar ve davranış
- `default-first-order`: tüm alanlar boş (`state=empty`: etiketler alan içinde), buton pasif.
- `dropdown-open`: gerçek uygulamada menü **açılır pencere**dir (içeriği itmez). Tasarımda ve karşılaştırma önizlemesinde menü akış içinde çizilir → kodda bu durumun önizlemesi `DomatDropdownOpenPreviewLayout` kullanır (Robolectric popup yakalamaz).
- `block-not-found`: "Listede bulamadım" seçilince dropdown bu metni gösterir; altında `TextField · block-free` + `InlineNote warning` belirir.
- `filled-returning`: blok + daire dolu → buton aktif. Not alanı çok satırlı olabilir (`singleLine=false`), yardımcı metin her zaman görünür.
- "Düzenle" → C4'e gider, Kişisel Bilgiler bloğuna odaklanır.

## Klavye
- Daire no: `KeyboardType.Number`; not: metin, IME `Done`.
