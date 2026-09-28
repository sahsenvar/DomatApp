#!/usr/bin/env python3
"""Ekran kartı kontrolü — kart aşamasında yakalanabilecek hataları erken yakalar (L-023, L-007).

  python3 ai/design/scripts/check_card.py C1 [C2 ...]     # argümansız: design/cards/*.yaml

Kontroller:
  - zorunlu alanlar: id, title, source, route, purpose, regions, states, strings, sampleData, navigation
  - her bölgedeki (ve iç içe content/alternative içindeki) component: components.yaml'da, 'text' ya da 'NEW:<Ad>'
  - her 'str.<anahtar>' referansı kartın strings'inde
  - kullanılmayan strings anahtarları (uyarı)
  - durum kimlikleri benzersiz; odak (focus/focused) bir durum değil (kural 4)
  - NEW: bileşenler listelenir (tasarımdan önce design-system-change gerekir)
  - acceptance: yoksa uyarı (eski kartlar bozulmasın); varsa her madde {id, text, test}, id benzersiz,
    test ∈ maestro | unit | roborazzi | manual (öneri 2026-09-28, R-4)
"""
import glob, os, re, sys
import yaml

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
os.chdir(ROOT)
MANIFEST = set((yaml.safe_load(open("design/components.yaml")).get("components") or {}).keys())
REQUIRED = ["id", "title", "source", "route", "purpose", "regions", "states", "strings", "sampleData", "navigation"]
ACCEPTANCE_TESTS = ["maestro", "unit", "roborazzi", "manual"]


def components(node, out):
    if isinstance(node, dict):
        if "component" in node:
            out.append(str(node["component"]))
        for v in node.values():
            components(v, out)
    elif isinstance(node, list):
        for v in node:
            components(v, out)
    return out


def check(path):
    card = yaml.safe_load(open(path))
    raw = open(path).read()
    errs, warns = [], []
    for k in REQUIRED:
        # sampleData: {} geçerli ("dinamik veri yok" açıkça söylenmiş); diğerleri boş olamaz.
        if k not in card or card[k] is None or (k != "sampleData" and card[k] in ("", [], {})):
            errs.append(f"'{k}' eksik ya da boş" + (" (kural 3: tasarımdaki dinamik veri)" if k == "sampleData" else ""))
    new = []
    for c in components(card.get("regions"), []):
        name = re.sub(r"\(.*\)$", "", c)
        if name == "text":
            continue
        if name.startswith("NEW:"):
            new.append(name[4:]); continue
        if name not in MANIFEST:
            errs.append(f"bileşen '{name}' components.yaml'da yok (yeni ise 'NEW:{name}' yaz)")
    strings = card.get("strings") or {}
    refs = set(re.findall(r"str\.([a-z0-9_]+)", raw))
    for r in sorted(refs - set(strings)):
        errs.append(f"'str.{r}' referansı strings'de yok")
    for k in sorted(set(strings) - refs):
        warns.append(f"strings '{k}' hiçbir bölgede kullanılmıyor (kontrol et)")
    ids = [s.get("id") for s in (card.get("states") or []) if isinstance(s, dict)]
    if len(ids) != len(set(ids)):
        errs.append("durum kimlikleri tekrar ediyor")
    for s in ids:
        if s and re.search(r"focus", s):
            errs.append(f"durum '{s}': odak bir ekran durumu değil (kural 4)")
    acceptance = card.get("acceptance")
    if acceptance is None:
        warns.append("'acceptance' yok (ui-test-verify testleri kabul maddelerinden üretir)")
    elif not isinstance(acceptance, list) or not acceptance:
        errs.append("'acceptance' boş olmayan bir liste olmalı")
    else:
        seen = set()
        for i, a in enumerate(acceptance):
            if not isinstance(a, dict) or not a.get("id") or not a.get("text"):
                errs.append(f"acceptance[{i}]: {{id, text, test}} olmalı")
                continue
            if a["id"] in seen:
                errs.append(f"acceptance '{a['id']}' tekrar ediyor")
            seen.add(a["id"])
            if a.get("test") not in ACCEPTANCE_TESTS:
                errs.append(f"acceptance '{a['id']}': test='{a.get('test')}' geçersiz ({' | '.join(ACCEPTANCE_TESTS)})")
    return errs, warns, new


paths = [f"design/cards/{a}.yaml" for a in sys.argv[1:]] or sorted(glob.glob("design/cards/*.yaml"))
failed = False
for p in paths:
    errs, warns, new = check(p)
    sid = os.path.splitext(os.path.basename(p))[0]
    print(f"{'✗' if errs else '✓'} {sid}" + (f"  (yeni bileşen: {', '.join(new)} → önce design-system-change)" if new else ""))
    for e in errs: print(f"    ✗ {e}")
    for w in warns: print(f"    · {w}")
    failed |= bool(errs)
sys.exit(1 if failed else 0)
