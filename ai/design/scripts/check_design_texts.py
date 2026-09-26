#!/usr/bin/env python3
"""Text consistency: every text visible in a design package must come from the screen card.

Usage:
  python3 ai/design/scripts/check_design_texts.py C4 [C1 ...]     # no args = every package in design/screens

A text in design/screens/<ID>/structure.json ("text" of a text layer, "texts" shown inside an instance)
passes when it is
  1. a value of the card's `strings:` (positional args %1$s / %1$d match any value), or
  2. a shared string in core/resource/.../values/strings.xml (e.g. consent_read_text "Metni oku"), or
  3. listed in the card's `sampleData:` (dynamic data: names, amounts, codes — any nesting of lists/maps).
Anything else is copy that exists only in Figma — exactly the drift that made C4@card-declined-3x say
something the card does not. Exit code 1 lists those texts; fix the design or add the string to the card.
"""
import json, os, re, sys, glob
import xml.etree.ElementTree as ET
import yaml

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
os.chdir(ROOT)

def shared_strings():
    p = "core/resource/src/commonMain/composeResources/values/strings.xml"
    out = []
    for e in ET.parse(p).getroot().iter("string"):
        out.append((e.text or "").replace("\\'", "'").replace('\\"', '"'))
    return out

def flatten(v):
    if isinstance(v, dict):
        for x in v.values(): yield from flatten(x)
    elif isinstance(v, list):
        for x in v: yield from flatten(x)
    elif v is not None:
        yield str(v)

def pattern(s):
    parts = re.split(r"%\d+\$[sd]", s)
    return re.compile("^" + ".+".join(re.escape(p) for p in parts) + "$", re.S)

def texts(node):
    if isinstance(node, dict):
        if "text" in node: yield node["text"]
        for t in node.get("texts", []): yield t
        for c in node.get("children", []): yield from texts(c)

def check(screen):
    card_p = f"design/screens/{screen}/card.yaml"
    if not os.path.exists(card_p): card_p = f"design/cards/{screen}.yaml"
    card = yaml.safe_load(open(card_p))
    pats = [pattern(v) for v in (card.get("strings") or {}).values() if isinstance(v, str)]
    pats += [pattern(v) for v in shared_strings()]
    samples = set(flatten(card.get("sampleData")))
    st = json.load(open(f"design/screens/{screen}/structure.json"))
    bad = {}
    for state, s in st["states"].items():
        for t in texts(s["tree"]):
            t = t.strip()
            if not t or t in samples or any(p.match(t) for p in pats): continue
            bad.setdefault(t, []).append(state)
    return bad

screens = sys.argv[1:] or sorted(os.path.basename(os.path.dirname(p)) for p in glob.glob("design/screens/*/structure.json"))
failed = False
for sc in screens:
    bad = check(sc)
    if bad:
        failed = True
        print(f"✗ {sc}: {len(bad)} text(s) not in card strings / shared strings / sampleData")
        for t, sts in bad.items(): print(f"    {t!r}  ← {', '.join(sorted(set(sts)))}")
    else:
        print(f"✓ {sc}")
sys.exit(1 if failed else 0)
