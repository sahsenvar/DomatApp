# C2 — OTP Doğrulama · Davranış notları

## Paketi okuma sırası (kod AI'ı için)
1. `card.yaml` → amaç, bölgeler, durumlar, `uiState`/`actions`, navigasyon, metin anahtarları, `sampleData`.
2. `structure.json` → her durumda hangi bileşen hangi property değerleriyle. `manifest` = `design/components.yaml` anahtarı (Compose sembolü + `spec`).
3. `states/*.png` → görsel referans (2x, 780 px = 390 dp). Karşılaştırma: `@Preview(name = "<ID>@<durum>", heightDp = structure.json size[1])` + `compare_design_package.py`.

## Yerleşim
- `ScreenHeader` + gövde (sp4, öğeler arası sp4). **Birincil buton yok**: 6. hane girilince doğrulama otomatik başlar.
- Sıra: açıklama metni → "Numarayı Değiştir" (TextLink) → `OtpCodeField` → (hata metni) → "Kodu Tekrar Gönder" + geri sayım.
- Hata metni ve geri sayım ekrana özgü düz metindir: hata `body-small on-error-container`, geri sayım `body-small on-surface-variant`.

## Durumlar ve davranış
- `default`: kod boş, ilk hücre vurgulu (vurgu `value.length`'e göre), geri sayım çalışıyor → "Kodu Tekrar Gönder" pasif (on-surface %38).
- `verifying`: 6 hane girildi → hücreler pasif (`enabled=false`), altında 24 dp dairesel ilerleme (M3 `CircularProgressIndicator`, primary). Geri tuşu etkisiz.
- `error`: yanlış kod → `isError` + `c2_error_wrong_code`; hücreler hata kenarlığında, rakamlar kalır; kullanıcı yazmaya başlayınca kod temizlenir ve hata kalkar.
- `resend-available`: geri sayım 0 → bağlantı aktif, geri sayım metni gizlenir.
- `linked-account`: doğrulama sonrası mevcut hesaba bağlandıysa `InfoBanner info` kısa süre (≈1,5 sn) gösterilir, ardından `navigation`'daki hedefe geçilir.

## Otomatik doldurma
- `OtpCodeField` `ContentType.SmsOtpCode` ile işaretli (Android autofill / iOS oneTimeCode). Android SMS Retriever entegrasyonu ayrı iş (akış doc).
