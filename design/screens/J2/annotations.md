# J2 — Kod Doğrulama (OTP) · Davranış notları

## Paketi okuma sırası (kod AI'ı için)
1. `card.yaml` → amaç, bölgeler, durumlar, `uiState`/`actions`, navigasyon, metin anahtarları, `sampleData`, `acceptance`.
2. `structure.json` → her durumda hangi bileşen hangi property değerleriyle. `manifest` = `design/components.yaml` anahtarı (Compose sembolü + `spec`).
3. `states/*.png` → görsel referans (2x, 780 px = 390 dp). Karşılaştırma: `@Preview(name = "<ID>@<durum>", heightDp = structure.json size[1])` + `compare_design_package.py`.

## Yerleşim
- `ScreenHeader` (geri var) + gövde: `text · description` → `TextLink · change-number` → `OtpCodeField · code` → `TextLink · resend` + `text · resend-countdown`.
- **Birincil buton ve `BottomActionBar` yok**: 6. hane girilince doğrulama otomatik başlar.
- C2'nin yerini alır; C2'deki "mevcut hesaba bağlandık" bandı kaldırıldı.

## Durumlar ve davranış
- `default`: kod boş, geri sayım çalışıyor (`j2_resend_countdown`, örnek 45 sn), "Kodu Tekrar Gönder" pasif.
- `verifying`: 6 hane → `OtpCodeField` `enabled=false` (manifest: disabled = doğrulanıyor); altında yükleniyor göstergesi (`LoadingIndicator · verifying`, bkz. *Açık*). Geri sayım satırları bu durumda çizilmemiş.
- `error`: `OtpCodeField isError` + alanın altında `j2_error_wrong_code` (Figma'da ayrı `text · code-error` katmanı, body-small). Geri sayım sürer.
- `resend-available`: geri sayım bitti → "Kodu Tekrar Gönder" aktif, sayaç metni gizli.
- "Numarayı Değiştir" → geri (J1, numara korunur).
- Doğrulama başarılı: `isNewUser` → J3 (`mode=Register`) `replaceTo` (J2 yığından çıkar); değilse akış `AuthResult` ile biter.
- Doğrulama anında cihazda sepet varsa `POST /v1/cart/merge`; 409 `window_closed` ve origin=Checkout → `CheckoutGraph.WindowClosedRoute`.

## Klavye / erişilebilirlik
- SMS otomatik doldurma (`ContentType.SmsOtpCode` / iOS `oneTimeCode`), `KeyboardType.NumberPassword`.

## Açık
- `verifying` durumundaki spinner kütüphane bileşeni değil (`components.yaml`'da karşılığı yok) — Handoff notu › Sorular.
