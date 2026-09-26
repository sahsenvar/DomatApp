#!/usr/bin/env python3
"""Tasarım zinciri öğrenim günlüğü: doğrulama ve rapor.

  python3 ai/design/scripts/learnings.py check     # şema doğrulama (hata varsa çıkış kodu 1)
  python3 ai/design/scripts/learnings.py report    # retro için özet: terfi adayları, aşama matrisi
  python3 ai/design/scripts/learnings.py next-id   # eklenecek kaydın kimliği

Günlük: ai/design/learnings.yaml. Alan açıklamaları dosyanın başında.
"""
import os, re, sys, datetime
from collections import Counter
import yaml

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
PATH = os.path.join(ROOT, "ai/design/learnings.yaml")

STAGES = ["flow", "card", "figma", "package", "code", "verify", "tooling"]
TYPES = ["contract-gap", "process-skipped", "tool-pitfall", "misinterpretation", "environment"]
KINDS = ["check", "contract", "skill", "code", "note"]
STATUSES = ["open", "mitigated", "retired"]
REQUIRED = ["id", "date", "stage_caused", "stage_detected", "type", "symptom", "root_cause", "fix",
            "occurrences", "seen_in", "measure", "status"]
STRENGTH = {k: i for i, k in enumerate(["note", "skill", "contract", "code", "check"])}


def load():
    with open(PATH) as f:
        return (yaml.safe_load(f) or {}).get("entries") or []


def check(entries):
    errors, ids = [], set()
    for i, e in enumerate(entries):
        where = e.get("id", f"#{i}")
        for k in REQUIRED:
            if k not in e:
                errors.append(f"{where}: '{k}' alanı eksik")
        if not re.fullmatch(r"L-\d{3}", str(e.get("id", ""))):
            errors.append(f"{where}: id biçimi L-000 olmalı")
        if e.get("id") in ids:
            errors.append(f"{where}: id tekrar ediyor")
        ids.add(e.get("id"))
        try:
            datetime.date.fromisoformat(str(e.get("date")))
        except ValueError:
            errors.append(f"{where}: date YYYY-MM-DD olmalı")
        for k, allowed in (("stage_caused", STAGES), ("stage_detected", STAGES), ("type", TYPES), ("status", STATUSES)):
            if e.get(k) not in allowed:
                errors.append(f"{where}: {k}='{e.get(k)}' geçersiz ({' | '.join(allowed)})")
        if not isinstance(e.get("occurrences"), int) or e.get("occurrences", 0) < 1:
            errors.append(f"{where}: occurrences ≥ 1 tam sayı olmalı")
        if not isinstance(e.get("seen_in"), list):
            errors.append(f"{where}: seen_in liste olmalı")
        m = e.get("measure")
        if m is not None:
            if not isinstance(m, dict) or m.get("kind") not in KINDS or not m.get("ref"):
                errors.append(f"{where}: measure {{kind: {'|'.join(KINDS)}, ref: ...}} ya da null olmalı")
        if e.get("status") == "mitigated" and m is None:
            errors.append(f"{where}: status mitigated ama measure yok")
        if e.get("status") == "open" and m is not None and m.get("kind") in ("check", "code"):
            errors.append(f"{where}: kontrol/kod önlemi varken status open (mitigated olmalı mı?)")
    return errors


def report(entries):
    live = [e for e in entries if e.get("status") != "retired"]
    print(f"Toplam kayıt: {len(entries)} (aktif {len(live)})\n")

    promote = [e for e in live if e["occurrences"] >= 2 and (e.get("measure") is None or e["measure"]["kind"] in ("note",))]
    weak = [e for e in live if e["occurrences"] >= 2 and e.get("measure") and e["measure"]["kind"] == "skill"]
    open_ = [e for e in live if e["status"] == "open"]
    print("## Terfi adayları (≥2 kez, önlemi yok ya da yalnızca not)")
    for e in promote or []:
        print(f"- {e['id']} ×{e['occurrences']} [{e['stage_caused']}→{e['stage_detected']}] {e['symptom']}")
    if not promote: print("- yok")
    print("\n## Güçlendirme adayları (≥2 kez, önlem yalnızca skill metni — kontrole çevrilebilir mi?)")
    for e in weak or []:
        print(f"- {e['id']} ×{e['occurrences']} → {e['measure']['ref']}: {e['symptom']}")
    if not weak: print("- yok")
    print("\n## Açık kayıtlar")
    for e in open_ or []:
        print(f"- {e['id']}: {e['symptom']}")
    if not open_: print("- yok")

    print("\n## Nerede yapıldı → nerede fark edildi (kayıt sayısı, tekrarlar dahil değil)")
    m = Counter((e["stage_caused"], e["stage_detected"]) for e in live)
    print("| yapıldı \\ fark edildi | " + " | ".join(STAGES) + " |")
    print("|---" * (len(STAGES) + 1) + "|")
    for a in STAGES:
        row = [str(m.get((a, b), "")) for b in STAGES]
        if any(row): print(f"| {a} | " + " | ".join(row) + " |")

    print("\n## Tür ve önlem dağılımı")
    print("tür:   " + ", ".join(f"{k} {v}" for k, v in Counter(e["type"] for e in live).most_common()))
    print("önlem: " + ", ".join(f"{k} {v}" for k, v in Counter((e.get('measure') or {}).get('kind', 'yok') for e in live).most_common()))

    late = [e for e in live if STAGES.index(e["stage_detected"]) - STAGES.index(e["stage_caused"]) >= 2]
    print(f"\n## Geç fark edilenler (≥2 aşama sonra): {len(late)}")
    for e in late:
        print(f"- {e['id']} {e['stage_caused']}→{e['stage_detected']}: {e['symptom']}")
    print("\nNot: geç fark edilen hatalar için hatanın yapıldığı aşamaya bir kontrol eklemek en yüksek getiriyi verir.")


def next_id(entries):
    nums = [int(e["id"][2:]) for e in entries if re.fullmatch(r"L-\d{3}", str(e.get("id", "")))]
    return f"L-{(max(nums) + 1) if nums else 1:03d}"


if __name__ == "__main__":
    cmd = sys.argv[1] if len(sys.argv) > 1 else "check"
    entries = load()
    if cmd == "check":
        errs = check(entries)
        for e in errs: print("✗", e)
        print(f"{'✗' if errs else '✓'} {len(entries)} kayıt, {len(errs)} hata")
        sys.exit(1 if errs else 0)
    elif cmd == "report":
        errs = check(entries)
        if errs: print(f"⚠ {len(errs)} şema hatası var; önce `check` çalıştır.\n")
        report(entries)
    elif cmd == "next-id":
        print(next_id(entries))
    else:
        sys.exit(__doc__)
