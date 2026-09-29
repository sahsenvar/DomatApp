# J3 — Adres Bilgisi · Davranış notları

## Paketi okuma sırası (kod AI'ı için)
1. `card.yaml` → amaç, bölgeler, durumlar, `uiState`/`actions`, navigasyon, metin anahtarları, `sampleData`, `acceptance`.
2. `structure.json` → her durumda hangi bileşen hangi property değerleriyle. `manifest` = `design/components.yaml` anahtarı (Compose sembolü + `spec`).
3. `states/*.png` → görsel referans (2x, 780 px = 390 dp). Karşılaştırma: `@Preview(name = "<ID>@<durum>", heightDp = structure.json size[1])` + `compare_design_package.py`.

## Yerleşim
- `ScreenHeader` + gövde (`text · intro` [yalnızca Register] → `InfoRow · site` → `FormSection · address-form` → `InlineNote · invoice` [yalnızca Checkout + ilk sipariş]) + `BottomActionBar` (`PrimaryButton` + Register'da `GhostButton · skip`).
- `Snackbar · snackbar` kök çerçevenin son çocuğu: içeriğin üstünde, alt barın hemen üstünde süzülür (akışta yer kaplamaz).
- C3'ün yerini alır.

## Modlar
| | Register | Checkout |
|---|---|---|
| Başlık | `j3_title_register` | `j3_title_checkout` |
| Geri | yok (`showBack=false`) | var |
| Giriş cümlesi | var | yok |
| Birincil buton | "Kaydet" | "Devam Et" |
| "Şimdi değil" | var | yok |
| Fatura notu | yok | ilk siparişte `InlineNote neutral` |
| Snackbar | ilk açılışta "Hesabın oluşturuldu." | yok |

## Durumlar ve davranış
- `default`: Register, snackbar görünür, alanlar boş (`state=empty`), buton pasif.
- `dropdown-open`: gerçek uygulamada menü **açılır pencere**dir (içeriği itmez); en altta "Listede bulamadım". Karşılaştırma önizlemesi C3'teki gibi `DomatDropdownOpenPreviewLayout` kullanır (Robolectric popup yakalamaz).
- `block-not-found`: dropdown "Listede bulamadım" gösterir; altında `DomatTextField · block-free` ("Blok adı") + `InlineNote warning`.
- `filled`: blok + daire dolu → buton aktif. Not alanı çok satırlı, yardımcı metin her zaman görünür.
- `checkout-mode`: yukarıdaki tablo.
- Buton etkin: blok seçili (ya da serbest blok dolu) **ve** daire dolu. Kayıt sürerken `loading`.
- Register "Kaydet" → `PUT /v1/addresses/me` → akış `AuthResult` ile biter; "Şimdi değil" kaydetmeden biter. Checkout "Devam Et" → kaydet → `CheckoutGraph.PaymentRoute` (C4).

## Klavye
- Daire no: `KeyboardType.Number`; not: metin, IME `Done`.
