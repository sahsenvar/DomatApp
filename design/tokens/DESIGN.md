---
version: alpha
name: DomatApp
description: >
  DomatApp tasarım sisteminin TEK DOĞRU KAYNAĞI. Compose teması (:core:design), Figma variables
  ve (gerekirse) Stitch tasarım sistemi bu dosyadan türetilir. Bu dosya değişmeden hiçbir
  türevde token değiştirilmez. Taslak v0.1 — 26 Eylül 2026.

# ─── RENKLER (light) ───────────────────────────────────────────────
# Adlar Material 3 rol adlarıdır (kebab-case). Kod karşılığı: on-primary → MaterialTheme.colorScheme.onPrimary
# 8 haneli hex CSS sırasıdır (#RRGGBBAA). Android'in ARGB sırası (0xAARRGGBB) ile karıştırmayın.
colors:
  primary: "#13EC49"
  on-primary: "#0F172A"
  primary-container: "#13EC4933"
  on-primary-container: "#102215"
  secondary: "#475569"
  on-secondary: "#FFFFFF"
  secondary-container: "#F1F5F9"
  on-secondary-container: "#0F172A"
  tertiary: "#1E3A8A"
  on-tertiary: "#FFFFFF"
  tertiary-container: "#DBEAFE"
  on-tertiary-container: "#1E3A8A"
  background: "#F6F8F6"            # KARAR 26.09.2026 — kodda şu an White, güncellenecek
  on-background: "#0F172A"
  surface: "#FFFFFF"
  on-surface: "#0F172A"
  surface-variant: "#F8FAFC"
  on-surface-variant: "#475569"
  error: "#EF4444"
  on-error: "#FFFFFF"
  error-container: "#FEE2E2"
  on-error-container: "#991B1B"
  outline: "#CBD5E1"
  outline-variant: "#E2E8F0"
  # ── Genişletilmiş roller (M3'te yok) — ÖNERİ, kodda henüz karşılığı yok ──
  success: "#047857"
  on-success: "#FFFFFF"
  success-container: "#D1FAE5"
  on-success-container: "#064E3B"
  warning-container: "#FFEDD5"
  on-warning-container: "#9A3412"
  scrim: "#0000008C"

# ─── RENKLER (dark) — DESIGN.md uzantısı (spec'te mod kavramı yok) ───
# Figma'da aynı koleksiyonun "Dark" modu, Compose'da domatDarkColorScheme() olur.
colorsDark:
  primary: "#13EC49"
  on-primary: "#102215"
  primary-container: "#102215"
  on-primary-container: "#13EC49"
  secondary: "#94A3B8"
  on-secondary: "#0F172A"
  secondary-container: "#1E293B"
  on-secondary-container: "#FFFFFF"
  tertiary: "#DBEAFE"
  on-tertiary: "#1E3A8A"
  tertiary-container: "#1E3A8A"
  on-tertiary-container: "#DBEAFE"
  background: "#0F172A"
  on-background: "#FFFFFF"
  surface: "#0F172A"
  on-surface: "#FFFFFF"
  surface-variant: "#1E293B"
  on-surface-variant: "#94A3B8"
  error: "#EF4444"
  on-error: "#FFFFFF"
  error-container: "#7F1D1D"
  on-error-container: "#FEE2E2"
  outline: "#94A3B8"
  outline-variant: "#334155"

# ─── TİPOGRAFİ ─────────────────────────────────────────────────────
# Kod karşılığı: title-large → MaterialTheme.typography.titleLarge
typography:
  display-large:   { fontFamily: Nunito Sans, fontSize: 48px, lineHeight: 60px, fontWeight: 800 }
  display-medium:  { fontFamily: Nunito Sans, fontSize: 36px, lineHeight: 40px, fontWeight: 800 }
  display-small:   { fontFamily: Nunito Sans, fontSize: 30px, lineHeight: 38px, fontWeight: 700 }
  headline-large:  { fontFamily: Nunito Sans, fontSize: 24px, lineHeight: 32px, fontWeight: 800 }
  headline-medium: { fontFamily: Nunito Sans, fontSize: 20px, lineHeight: 28px, fontWeight: 700 }
  headline-small:  { fontFamily: Nunito Sans, fontSize: 18px, lineHeight: 28px, fontWeight: 700 }
  title-large:     { fontFamily: Nunito Sans, fontSize: 16px, lineHeight: 24px, fontWeight: 700 }
  title-medium:    { fontFamily: Nunito Sans, fontSize: 16px, lineHeight: 24px, fontWeight: 500 }
  title-small:     { fontFamily: Nunito Sans, fontSize: 14px, lineHeight: 20px, fontWeight: 500 }
  body-large:      { fontFamily: Nunito Sans, fontSize: 16px, lineHeight: 26px, fontWeight: 400 }
  body-medium:     { fontFamily: Nunito Sans, fontSize: 14px, lineHeight: 20px, fontWeight: 400 }
  body-small:      { fontFamily: Nunito Sans, fontSize: 12px, lineHeight: 16px, fontWeight: 400 }
  label-large:     { fontFamily: Nunito Sans, fontSize: 14px, lineHeight: 20px, fontWeight: 700 }
  label-medium:    { fontFamily: Nunito Sans, fontSize: 12px, lineHeight: 16px, fontWeight: 700 }
  label-small:     { fontFamily: Nunito Sans, fontSize: 10px, lineHeight: 15px, fontWeight: 700 }

# ─── KÖŞE YARIÇAPLARI ──────────────────────────────────────────────
# Kod karşılığı: sm/md/lg/xl → MaterialTheme.shapes.small/medium/large/extraLarge
rounded:
  none: 0px
  xs: 4px          # Tag — M3 extraSmall
  sm: 8px
  md: 12px         # buton, kart, input (varsayılan)
  lg: 16px
  xl: 24px         # bottom sheet üst köşeleri
  full: 9999px     # pill, avatar, progress

# ─── BOŞLUK ────────────────────────────────────────────────────────
# Kod karşılığı: sp4 → MaterialTheme.spacing.sp4 (4px taban ızgarası)
spacing:
  sp1: 4px
  sp2: 8px
  sp3: 12px
  sp4: 16px
  sp5: 20px
  sp6: 24px
  sp8: 32px
  sp10: 40px
  sp12: 48px
  sp16: 64px
  sp20: 80px

# ─── BİLEŞEN STİL TOKEN'LARI (taslak — components.yaml ile kesinleşecek) ───
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.title-large}"
    rounded: "{rounded.md}"
    height: 56px
  button-primary-disabled:
    backgroundColor: "{colors.outline-variant}"
    textColor: "{colors.on-surface-variant}"
  button-secondary:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.title-large}"
    rounded: "{rounded.md}"
    height: 48px
  text-field:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.md}"
  text-field-focused:
    borderColor: "{colors.primary}"
  card:
    backgroundColor: "{colors.surface}"
    borderColor: "{colors.outline-variant}"
    rounded: "{rounded.md}"
    padding: "{spacing.sp4}"
  tag:
    typography: "{typography.label-small}"
    rounded: "{rounded.xs}"
  banner-info:
    backgroundColor: "{colors.tertiary-container}"
    textColor: "{colors.on-tertiary-container}"
    rounded: "{rounded.md}"
    padding: "{spacing.sp4}"
  banner-warning:
    backgroundColor: "{colors.warning-container}"
    textColor: "{colors.on-warning-container}"
    rounded: "{rounded.md}"
    padding: "{spacing.sp4}"
---

# DomatApp Tasarım Sistemi

## Overview

DomatApp, İstanbul'daki site sakinlerini haftalık topluluk alımıyla çiftçiye bağlayan bir mobil uygulama. Görsel dil **"komşu kollektifi"** hissini taşır: güven veren, sade, sıcak; "online manav" gürültüsünden uzak. Ana renk olan canlı yeşil (malachite) tazeliği ve topluluk başarısını anlatır. Nötrler soğuk arduvaz (slate) tonlarıdır ve yeşile yer açar.

Temel ilkeler:

- **Para hareket eden ekranlarda belirsizlik yok.** Provizyon, iptal hakkı, ikinci sipariş gibi bilgiler kapatılamayan, net şeritlerle gösterilir; kaygı değil açıklık dili kullanılır.
- **Tek ortak tasarım.** iOS ve Android aynı ekranı görür. Platform farkı yalnızca sistem öğelerinde (durum çubuğu, geri hareketi) vardır.
- **Metin Türkçedir** ve her zaman `strings.xml` anahtarından gelir. Tasarımdaki metin yalnızca görsel yer tutucudur.

## Colors

- **primary (`#13EC49`)** yalnızca dolgu olarak kullanılır: birincil buton, aktif ilerleme göstergesi, seçili kenarlık. Üstündeki metin her zaman `on-primary` (koyu). **Beyaz zemin üzerinde metin rengi olarak kullanılmaz** (kontrast ~1,6:1, WCAG'i geçmez).
- **background (`#F6F8F6`)** sayfa zeminidir; **surface (`#FFFFFF`)** kart, input ve sheet zeminidir. Bu ayrım kartların zeminden ayrışmasını sağlar.
- **error (`#EF4444`)** ikon ve kenarlık içindir. Hata **metni** için `on-error-container` kullanılır (`#EF4444` beyaz üzerinde ~3,8:1, küçük metinde yetersiz).
- **Genişletilmiş roller** (`success*`, `warning-container`, `on-warning-container`) Material 3'te olmayan ama ürünün ihtiyaç duyduğu anlamlardır: başarı (sipariş onayı, topluluk katkısı) ve uyarı (iptal hakkı kapanmış teslimat). **Öneri aşamasındadır; kodda karşılıkları henüz yoktur** (`DomatExtendedColors` olarak eklenmesi önerilir).
- Yarı saydam yeşiller (`primary-container` = %20 malachite) ikon zemini ve rozetler içindir.

## Typography

Tek yazı ailesi: **Nunito Sans**. Hiyerarşi Material 3'ün 15 stiliyle kurulur; ekranlarda stil dışı boyut kullanılmaz.

- Ekran başlıkları: `headline-small` (üst çubuk), `headline-large` (sayfa içi büyük başlık).
- Butonlar: `title-large` (büyük), `label-large` (küçük).
- Gövde ve form metinleri: `body-medium`; açıklama ve hukuki metin: `body-small`.

> **Teknik not (hata):** Repoda yalnızca `nunito_sans_regular.ttf` ve `nunito_sans_italic.ttf` var. Tipografi 500/700/800 ağırlıkları istiyor; bunlar şu an sistem tarafından **sahte kalınlık** (synthesized bold) ile çiziliyor ve Figma'daki gerçek Nunito Sans Bold ile piksel düzeyinde uyuşmayacak. Değişken font (`NunitoSans[wght].ttf`) ya da ağırlık dosyalarının eklenmesi gerekiyor.

## Layout

- **4px taban ızgarası.** Tüm boşluklar `spacing` ölçeğinden gelir (`sp1`=4 … `sp20`=80).
- Ekran yatay kenar boşluğu: `sp4` (16px).
- Bölümler arası: `sp6` (24px); bir bölüm içindeki öğeler arası: `sp3` (12px); form alanları arası: `sp4`.
- Alt aksiyon çubuğu (`BottomActionBar`): üstte `outline-variant` 1px çizgi, iç boşluk `sp4`, alt güvenli alan sisteme bırakılır (sabit 32px yazılmaz).
- Tasarım çerçevesi: **390 × 844** (iPhone 14/15 boyutu); küçük ekran kontrolü **360 × 640**.

## Elevation & Depth

Derinlik öncelikle **kenarlık ve zemin farkıyla** (surface üzerinde `outline-variant` 1px) verilir, gölge ikincildir. Gölge ölçeği: `none 0 · xs 1 · sm 2 · md 4 · lg 8 · xl 12` (dp). Kartlar `none` + kenarlık; bottom sheet ve dialog `lg`.

## Shapes

- Varsayılan köşe **`md` (12px)**: buton, kart, input, banner.
- `xs` (4px) yalnızca `Tag`; `full` pill, avatar ve ilerleme göstergeleri; `xl` (24px) bottom sheet üst köşeleri.
- Ölçek dışı yarıçap yazılmaz. (Mevcut `DomatTextField` 10px kullanıyor → `md`'ye çekilecek.)

## Components

Bileşenlerin tam listesi, parametreleri, Figma karşılıkları ve durumları `design/components.yaml` dosyasındadır. Bu bölüm yalnızca stil kurallarını verir; front matter'daki `components` token'ları taslaktır.

- **Butonlar:** birincil (primary dolgu), ikincil (secondary-container), hayalet (kenarlıksız). Bir ekranda en fazla bir birincil buton.
- **Banner'lar:** `banner-info` (bilgi — örn. "Bu, bu haftaki 2. siparişin") ve `banner-warning` (geri dönüşü olmayan durum — örn. iptal hakkı kapanmış). Kapatılamaz; ikon + başlık + gövde.
- **Onay kutuları:** yasal onaylar (KVKK, mesafeli satış) varsayılan **işaretsiz**; metin genişletilebilir.

## Do's and Don'ts

- ✅ Her renk, boşluk, yarıçap ve yazı stili bu dosyadaki bir token'dan gelir.
- ✅ Durum başına ayrı tasarım çerçevesi (`C4.S3@kart-reddi`).
- ❌ `Color(0xFF…)`, `colorResource(R.color.x)` ya da çıplak `dp` sayısı ekran kodunda yazılmaz.
- ❌ Beyaz zemin üzerinde `primary` renginde metin.
- ❌ Ölçek dışı değer (10px yarıçap, 17px boşluk gibi) — gerekiyorsa önce bu dosyaya token olarak eklenir.
- ❌ Tasarımda uydurma metin — metin ekran kartındaki anahtardan gelir.

## Kod ve Figma Eşlemesi (DomatApp uzantısı)

| DESIGN.md | Figma variable | Compose |
|---|---|---|
| `colors.on-primary` | `color/on-primary` (modlar: Light, Dark) | `MaterialTheme.colorScheme.onPrimary` |
| `colors.success` | `color/success` | `MaterialTheme.domatColors.success` *(eklenecek)* |
| `typography.title-large` | text style `title-large` | `MaterialTheme.typography.titleLarge` |
| `rounded.md` | `radius/md` | `MaterialTheme.shapes.medium` |
| `spacing.sp4` | `spacing/sp4` | `MaterialTheme.spacing.sp4` |

Kural: kebab-case token adı → camelCase Compose adı. Figma'da her variable'ın **Android code syntax** alanına Compose ifadesi yazılır; böylece Figma MCP kod AI'ına token adını verir.
