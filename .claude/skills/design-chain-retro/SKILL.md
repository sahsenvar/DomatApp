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
Ayrıca `ai/design/screen-metrics.yaml`: akıştan akışa `first_pass_diff`, `fix_rounds`, `text_check_fails`,
`core_changes` eğilimi. İyileşme yoksa döngü çalışmıyor demektir — nedenini ara.

## 2. Her aday için önlem seç

Tercih sırası (güçlüden zayıfa): **check** (otomatik kontrol) > **code** (helper/tema/araç kodunda önlem) >
**contract** (`design/README.md`, `components.yaml`) > **skill** (tuzak tablosu) > **note**.

- `≥2 kez` ve önlemi yok/not → önlem öner.
- `≥2 kez` ve önlemi yalnızca skill metni → kontrole ya da koda çevrilebilir mi? (ör. L-010 builder'daki `fixWidth`).
- **Geç fark edilenler** (yapıldığı aşamadan ≥2 aşama sonra) → hatanın yapıldığı aşamaya kontrol ekle.
  Örnek: Figma'da yapılıp doğrulamada bulunan hatalar için Figma aşamasında bir tarama (use_figma ile
  kütüphane dışı örnek, dolgu opaklığı, 48 dp altı hedef, focused varyantı kullanımı).
- Artık geçerli olmayan kayıtlar → `status: retired`; ilgili skill satırı budanır. Skill'ler şişerse okunmaz.

## 3. Öneriyi hazırla

Her öneri için: kayıt kimlikleri, önlem türü, değişecek dosya ve **önerilen metin/diff**. İki hedef:

- **Repo (Claude Code tarafı):** `.claude/skills/*`, `design/README.md`, `components.yaml`, `ai/design/scripts/*`
  → diff olarak göster; Sahan onaylarsa uygula ve commit et.
- **Claude Projects tarafı** (`domatapp-flow-to-card` skill'i): tam SKILL.md metnini hazırla ve
  `ai/design/proposals/<tarih>-flow-to-card.md`'ye yaz; Sahan Projects oturumunda skill güncelleme kartıyla onaylar.

## 4. Kaydı kapat

Onaylanan önlemlerden sonra ilgili kayıtlarda `measure` ve `status: mitigated` güncellenir; `learnings.py check`.
Retro özeti (bulgular, alınan önlemler, eğilim) proje raporuna eklenir.
