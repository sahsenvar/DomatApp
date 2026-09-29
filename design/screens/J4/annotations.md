# J4 — KVKK Aydınlatma Metni · Davranış notları

## Paketi okuma sırası (kod AI'ı için)
1. `card.yaml` → amaç, bölgeler, durumlar, `uiState`/`actions`, navigasyon, metin anahtarları, `sampleData`, `acceptance`.
2. `structure.json` → her durumda hangi bileşen hangi property değerleriyle. `manifest` = `design/components.yaml` anahtarı (Compose sembolü + `spec`).
3. `states/*.png` → görsel referans (2x, 780 px = 390 dp). Karşılaştırma: `@Preview(name = "<ID>@<durum>", heightDp = structure.json size[1])` + `compare_design_package.py`.

## Yerleşim
- `ScreenHeader` (geri var) + kayan gövde. `BottomActionBar` yok. I6 (Sözleşme Metni) ile aynı düzen.
- `content`: `text · version` (body-small on-surface-variant, `j4_version`) → `text · body` (body-medium on-surface). Metin sunucudan gelir; hukuk metni şimdilik yer tutucu.

## Durumlar ve davranış
- `loading`: 6 iskelet satır (`skeleton · body`), açılışta `GET /v1/legal/kvkk/current` sürerken. Bkz. *Açık*.
- `error`: `StatusHero tone=error` (`j4_error_title` / `j4_error_body`) + `SecondaryButton` "Tekrar dene" → yeniden yükler (`loading`'e döner).
- Geri → J1; J1'de girilen numara korunur.

## Açık
- İskelet satırlar kütüphane bileşeni değil (`components.yaml`'da iskelet yalnızca `ProductCard state=loading` içinde) — Handoff notu › Sorular.
