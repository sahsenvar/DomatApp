# J1 — Telefon Numarası (Giriş / Kayıt) · Davranış notları

## Paketi okuma sırası (kod AI'ı için)
1. `card.yaml` → amaç, bölgeler, durumlar, `uiState`/`actions`, navigasyon, metin anahtarları, `sampleData`, `acceptance`.
2. `structure.json` → her durumda hangi bileşen hangi property değerleriyle. `manifest` = `design/components.yaml` anahtarı (Compose sembolü + `spec`).
3. `states/*.png` → görsel referans (2x, 780 px = 390 dp). Karşılaştırma: `@Preview(name = "<ID>@<durum>", heightDp = structure.json size[1])` + `compare_design_package.py`.

## Yerleşim
- `ScreenHeader` (geri var) + gövde (`text · rationale` → `PhoneNumberField · phone` → `text · kvkk-notice`) + `BottomActionBar` (sabit altta, klavye açıkken klavyenin üstünde). Ekran 844 dp; gövde taşarsa kayar.
- C1'in yerini alır (`supersedes: C1`). C1'deki KVKK onay kutusu ve "Metni oku" genişletmesi **yok**.

## Durumlar ve davranış
- `default` / `default-account`: aynı ekran, yalnızca gerekçe metni `origin`'e göre değişir (Checkout → `j1_rationale_checkout`, Account → `j1_rationale_account`). Alan boş (`state=empty`), buton pasif.
- `ready`: 10 hane, 5 ile başlıyor → buton aktif. Önek "+90" yalnızca etiket yüzerken görünür (M3).
- `phone-invalid`: `isError` + `j1_phone_error` (alanın destek metni); buton pasif.
- `sending`: `POST /v1/auth/otp/request` (`kvkkNoticeVersion` ile) sürerken `PrimaryButton loading=true`; başarıda J2'ye `phoneNumber` ile gidilir.
- Geri: AuthFlow sonuçsuz kapanır (ResultFlow cancelled), çağırana (Sepet / Hesap) dönülür.

## KVKK bildirimi
- Pasif metin (body-small on-surface-variant); içindeki "KVKK Aydınlatma Metni" aralığı bağlantıdır (label-medium, tertiary, altı çizili) → J4.
  Figma'da tek metin katmanı, karışık stil (structure.json'da `style` boş). Kodda `AnnotatedString` + `LinkAnnotation`; dokunma alanı ≥48 dp (kural 10).
- J4'ten geri dönünce girilen numara korunur (AC-6) — durum ViewModel'de tutulur.

## Klavye
- `KeyboardType.Phone`, IME `Done` → geçerliyse "Kod Gönder" ile aynı eylem.
