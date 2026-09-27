# C4 — Ödeme · Davranış notları (tasarım paketi)

Bu dosya görselde görünmeyen davranışı anlatır. Görsel: `states/*.png`. Yapı: `structure.json`. İçerik ve durum mantığı: `card.yaml`.

## Paketi okuma sırası (kod AI'ı için)
1. `card.yaml` → ekranın amacı, bölgeler, durumlar, `uiState`/`actions` taslağı, navigasyon, metin anahtarları.
2. `structure.json` → her durumda hangi bileşenin hangi property değerleriyle kullanıldığı. `manifest` alanı = `design/components.yaml` anahtarı → Compose sembolü oradan.
3. `states/*.png` → yerleşim ve görsel doğrulama referansı (2x, 780 px genişlik = 390 dp).
4. `tokens-used.json` → ekran seviyesinde kullanılan token'lar.

## Yerleşim
- Ekran: `ScreenHeader` (sabit üstte) + **kaydırılabilir** gövde + `BottomActionBar` (sabit altta, klavye açıkken klavyenin üstünde).
- Gövde: yatay `sp4`, dikey `sp4` iç boşluk; bölümler arası `sp4`.
- `FormSection · <id>` katmanları bir **Figma frame'idir** (bileşen örneği değil) → Compose'da `FormSection(title) { … }`.
- `BottomActionBar · cta` bir frame'dir → Compose'da `BottomActionBar { … }` (üst kenarlık + surface zemin + sistem gezinme çubuğu boşluğu).
- Ekran arka planı `background` (#F6F8F6); bölüm kartları `surface` (#FFFFFF).

## Durum geçişleri
- `first-order` ↔ `returning`: `isFirstOrder` ve `savedCards` belirler; aynı ekranın iki biçimi, iki ayrı ekran değil.
- `second-order` ve `cancellation-passed` şeritleri ödeme ekranı açılırken `GET /v1/deliveries/current` ile bir kez hesaplanır.
- `below-minimum`: `PrimaryButton · pay` yerine alt çubukta uyarı + `SecondaryButton · back-to-cart`. Mesafeli satış kutusu işaretsiz kalabilir (ödeme zaten mümkün değil).
- `card-declined` / `card-declined-3x`: form alanları korunur, `card-error` şeridi `payment-method` bölümünün hemen altına gelir; 3. ardışık retten sonra gövde metni değişir. CVV alanı temizlenir.
- Ödeme sürerken (`isPaying`): `PrimaryButton` `loading=true`, tüm girişler pasif; geri tuşu ödeme bitene kadar etkisiz.

## Etkileşim
- `TextField · email` doldurulduğunda `ConsentCheckbox · marketingConsent` görünür; boşaltılınca gizlenir ve değeri `false` olur.
- `ConsentCheckbox · distance-contract` "Metni oku" ile yerinde genişler (`expanded=true` varyantı kütüphanede var).
- `PaymentCardOption` seçilince CVV alanı odak alır; "Farklı kart kullan" yeni kart formunu açar (first-order'daki `payment-method` içeriği).
- `PrimaryButton · pay` yalnızca: ad/soyad dolu (ilk sipariş) + kart bilgisi/CVV geçerli + sözleşme işaretli iken aktif.

## Klavye ve odak
- Alan sırası: Ad → Soyad → E-posta → (kart numarası → SKT → CVV | CVV). IME aksiyonu son alanda `Done`.
- Kart numarası `4-4-4-4` biçimlenir; SKT `AA/YY`; CVV maskeli.

## Erişilebilirlik
- Tüm dokunma hedefleri ≥ 48 dp (checkbox satırın tamamı tıklanabilir).
- Şeritlerde ikon dekoratiftir (`contentDescription = null`); başlık + gövde birlikte okunur.
- Hata şeridi göründüğünde ekran okuyucuya duyurulur (live region).

## Bilinen tasarım sadeleştirmeleri (pilot kapsamı)
- Kart marka rozeti ("MC") geçici metin; gerçek marka logoları asset olarak eklenecek.
- İkonlar: yeni ikonlar Material Symbols Rounded (`ic_info`, `ic_warning`, `ic_error`, `ic_check`, `ic_check_circle`, `ic_credit_card`…). **`ic_arrow_back` ve `ic_lock` repoda zaten var** (eski özel çizim) — mevcut dosyalar kullanılır; ikon setinin birleştirilmesi ayrı karar.
- Dark mode çerçeveleri üretilmedi; renkler token'a bağlı olduğundan Figma'da mod değiştirerek görülebilir.
