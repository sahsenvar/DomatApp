# Öneri: figma-screens skill'i — tek prototip sayfası ve prototip adımı

Kaynak kayıtlar: L-032, L-033, L-034, L-035, L-036, L-040 (hepsi builder.js v2'de kodla önlendi).
Skill metni onaysız değiştirilmedi; aşağıdaki değişiklikler Sahan onayı bekliyor.

## SKILL.md değişiklikleri

1. **Sayfa:** "ekran sayfası `Screens · Akış <X>`" → "tek sayfa `Screens`; her akış bir SECTION (`Akış <X>`).
   Prototip bağlantıları sayfalar arasında çalışmaz (L-032)." Koordinatlar bölüm içindedir: x = 200 + durum×450, y = 200 + ekran×1100.
2. **Çerçeve:** tüm ekranlar 390×844 sabit; içerik iç içe kaydırma gövdesinde (L-033). `annotations.md`'de "kayar" yazılır.
   Popup'lar `o.sheet` / `o.dialog` ile akış içinde çizilir (kural 6).
3. **Yeni adım — 1b. Prototip:** tüm çerçeveler oluştuktan sonra ayrı bir çağrıda bağlanır:
   - geri: `ScreenHeader · header > back` → `backA()`; durumlar arası geçiş: `go('<ID>@<durum>', FADE|SMART)`; ekranlar arası: `PUSH`.
   - zamanlayıcılar: `{type:'AFTER_TIMEOUT', timeout:s}` (yükleniyor → içerik, snackbar kapanışı).
   - değişkenler: `Prototype` koleksiyonu, `proto/*`. Sayaç = FLOAT + STRING ikizi, `counter()` (L-034). Koşul: `when(eq(...))`.
   - etkileşimli bileşenler kütüphanede CHANGE_TO ile (Chip, TierCard, ConsentCheckbox, Dropdown, SelectableItem, RadioRow, ListRow switch, Segmented).
     Aynı katmana ekran bağlantısı eklemek bileşenin kendi etkileşimini ezer — gerekmedikçe ekleme.
   - akış başlangıçları en sonda, nodeId'ye göre tekilleştirilerek yazılır (L-040).
4. **Paket dışa aktarma** kod aşamasına bırakılabilir: prototip turunda yalnızca kart → Figma; paket, `figma-to-compose`'dan hemen önce çıkarılır (MCP çağrı bütçesi).

## design/README.md ek kural önerisi

- **19.** Prototip değişkenleri (`Prototype` koleksiyonu, `proto/*`) tasarım token'ı değildir; koda aktarılmaz.
- **20.** Kart, `components.yaml`'da olmayan bir varyant/prop değerini kullanamaz; gerekiyorsa `change` olarak işaretlenir (L-037).
  Kontrol önerisi: `check_card.py` prop/varyant değerlerini de doğrulasın (katalogdaki `VARIANT: a|b` listesine karşı).

## Kütüphane açık işleri (L-037) — design-system-change ile

InlineNote `error` tonu + `icon`; ConsentCheckbox `error`; ListRow `trailing: action|external`, `emphasis`; DomatBadge `Delivery`,
`size: large`; PrimaryButton `leadingIcon` (Figma), StatusHero `icon` prop'u; DomatIconButton Figma bileşeni; PinDots boş+hata;
DeliveryRow `note`, `loading`; SelectableItem `caption`, `action`; ProgressSteps Figma bileşeni; Card/Status `tone`.
Ek ikonlar: account_circle, gavel, assignment_return, cloud_upload, qr_code_scanner, no_photography, flashlight_off, dialpad.
