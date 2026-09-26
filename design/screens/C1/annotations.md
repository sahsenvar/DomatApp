# C1 — Telefon Numarası Girişi · Davranış notları

## Paketi okuma sırası (kod AI'ı için)
1. `card.yaml` → amaç, bölgeler, durumlar, `uiState`/`actions`, navigasyon, metin anahtarları, `sampleData`.
2. `structure.json` → her durumda hangi bileşen hangi property değerleriyle. `manifest` = `design/components.yaml` anahtarı (Compose sembolü + `spec`).
3. `states/*.png` → görsel referans (2x, 780 px = 390 dp). Karşılaştırma: `@Preview(name = "<ID>@<durum>", heightDp = structure.json size[1])` + `compare_design_package.py`.

## Yerleşim
- `ScreenHeader` + gövde (sp4 iç boşluk, öğeler arası sp4) + `BottomActionBar` (sabit altta, klavye açıkken klavyenin üstünde). Ekran 844 dp sabit; gövde kalan alanı doldurur, taşarsa kayar.
- Gerekçe metni ekrana özgü düz metindir (`text · rationale`, body-medium on-surface-variant).

## Durumlar ve davranış
- `default`: numara boş (`PhoneNumberField` → TextField `state=empty`: etiket alan içinde), KVKK işaretsiz, buton pasif.
- `ready`: 10 hane + KVKK işaretli → `PrimaryButton` aktif. Önek "+90" yalnızca etiket yüzerken görünür (M3).
- `phone-invalid`: numara 10 hane değil/5 ile başlamıyor → `isError` + `c1_phone_error`. Hata, alan odaktan çıkınca ya da butona basılınca gösterilir; yazarken değil.
- `kvkk-expanded`: "Metni oku" yerinde genişletir, bağlantı "Metni gizle" olur. Metin uzun olabilir (hukuk); gövde kayar.
- `sending`: `POST /v1/users/me/kvkk-consent` → `POST /v1/auth/otp/request`; buton `loading=true`, girişler pasif.

## Klavye / erişilebilirlik
- Klavye `Phone`, IME `Done` → geçerliyse Kod Gönder ile aynı eylem.
- Checkbox satırın tamamı tıklanabilir (≥48 dp); "Metni oku" ayrı hedef.
