---
name: design-chain-retro
description: >
  Review the DomatApp design-chain learning log (ai/design/learnings.yaml) and screen metrics, find
  recurring or late-detected failures, and PROPOSE updates to checks, the design contract and skills for
  Sahan's approval. Use after a flow is finished, when asked for "retro", "hatalardan ders çıkar",
  "skill'leri güncelle", or when learnings.py report shows promotion candidates.
---

# Tasarım zinciri retrosu

Amaç: aynı hatanın ikinci kez olmaması. Bu skill **öneri üretir**; skill'leri, sözleşmeyi ya da kontrolleri
onaysız değiştirmez (yanlış yorumlanmış bir hata kalıcı kurala dönüşmesin).

## 1. Veriyi topla

```bash
python3 ai/design/scripts/learnings.py check
python3 ai/design/scripts/learnings.py report
```
Cowork'ün kaydı (`claude/DomatApp_Ogrenme_Kaydi_Cowork.md`, `CW-…`) repo'da değildir; Cowork iki kaydı
birlikte okur. Burada yalnızca repo kaydı işlenir; Cowork kaynaklı bir örüntü görürsen öneriyi Cowork'e
yönelt (aşağıda). Kayıtların nerede durduğu: `design/README.md` → Hata kaydı.
Ayrıca `ai/design/screen-metrics.yaml`: akıştan akışa `first_pass_diff`, `fix_rounds`, `text_check_fails`,
`core_changes` eğilimi. İyileşme yoksa döngü çalışmıyor demektir — nedenini ara.

## 2. Her aday için önlem seç

Tercih sırası (güçlüden zayıfa): **check** (otomatik kontrol) > **code** (helper/tema/araç kodunda önlem) >
**contract** (`design/README.md`, `components.yaml`) > **skill** (tuzak tablosu) > **note**.

- `≥2 kez` ve önlemi yok/not → önlem öner.
- `≥2 kez` ve önlemi yalnızca skill metni → kontrole ya da koda çevrilebilir mi? (ör. L-010 Cowork `builder.js`'deki `fixWidth`).
- **Geç fark edilenler** (yapıldığı aşamadan ≥2 aşama sonra) → hatanın yapıldığı aşamaya kontrol ekle.
  Örnek: Figma'da yapılıp doğrulamada bulunan hatalar için Figma aşamasında bir tarama (use_figma ile
  kütüphane dışı örnek, dolgu opaklığı, 48 dp altı hedef, focused varyantı kullanımı).
- Artık geçerli olmayan kayıtlar → `status: retired`; ilgili skill satırı budanır. Skill'ler şişerse okunmaz.

## 3. Öneriyi hazırla

Her öneri için: kayıt kimlikleri, önlem türü, değişecek dosya ve **önerilen metin/diff**. İki hedef:

- **Repo (Claude Code tarafı):** `.claude/skills/*` (`figma-to-package`, `package-to-compose`, `ui-test-verify`,
  `design-system-change`, bu skill), `design/README.md`, `components.yaml`, kart şeması, `ai/design/scripts/*`
  → diff olarak göster; Sahan onaylarsa uygula ve commit et.
- **Cowork tarafı** (`flow-to-card`, `card-to-figma`, `builder.js`, proje dokümanları): repo'ya yazılmaz.
  Öneriyi Figma › Handoff notuna yaz; Cowork uygular, Sahan onaylar.

## 4. Kaydı kapat

Onaylanan önlemlerden sonra ilgili kayıtlarda `measure` ve `status: mitigated` güncellenir; `learnings.py check`.
Retro özeti (bulgular, alınan önlemler, eğilim) proje raporuna eklenir.
