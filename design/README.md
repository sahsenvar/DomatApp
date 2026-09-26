# design/ — Tasarım → Kod sözleşmesi

| Yol | Ne | Doğru kaynak mı? |
|---|---|---|
| `tokens/DESIGN.md` | Renk, tipografi, boşluk, köşe token'ları | **Evet** — Compose teması ve Figma variables buradan türetilir |
| `components.yaml` | Figma bileşeni ↔ Compose sembolü ↔ parametre eşlemesi | **Evet** — bileşen kataloğu |
| `cards/<ID>.yaml` | Akış dokümanından türetilen ekran kartı | Hayır — akış dokümanının türevi |
| `screens/<ID>/` | Onaylanmış tasarım paketi (görsel + yapı) | Figma'nın o anki anlık görüntüsü |

## Kod yazarken kurallar
1. Önce `screens/<ID>/annotations.md` → `card.yaml` → `structure.json` oku; görseli en son doğrulama için kullan.
2. `structure.json`'daki her `manifest` değeri için `components.yaml`'daki Compose sembolünü **çağır**. Yeniden yazma.
3. Renk/boşluk/köşe/yazı stili yalnızca token'dan: `MaterialTheme.colorScheme.*`, `MaterialTheme.domatColors.*`, `MaterialTheme.spacing.*`, `MaterialTheme.shapes.*`, `MaterialTheme.typography.*`. `Color(0x…)` ve çıplak boşluk `dp` yasak.
4. `status: new` / `change` / `promote` bileşenler önce `:core:presentation`'da yazılır/değiştirilir, sonra ekranda kullanılır.
5. Metinler `card.yaml` → `strings` anahtarlarıyla `core/resource/.../values/strings.xml`'e eklenir.

Figma dosyası: https://www.figma.com/design/5hpUTmJJ1ie3E9boGSPXje (bkz. `screens/<ID>/source.json`).
