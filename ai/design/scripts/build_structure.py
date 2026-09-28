#!/usr/bin/env python3
"""export.js çıktısından design/screens/<ID>/structure.json üretir.

  python3 ai/design/scripts/build_structure.py raw1.json [raw2.json ...]

Her raw dosya, figma-to-package/export.js'in döndürdüğü JSON'dur ({"C1": {"default": {...}}, ...}).
Birden fazla dosya verilebilir (20 KB sınırı yüzünden ekranlar ayrı çağrılarda dışa aktarılır).
Aynı ekranın durumları birleştirilir; componentsUsed ve image alanları eklenir.
"""
import json, os, sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
NOTE = ("Figma use_figma ile dışa aktarıldı; \"manifest\" alanı design/components.yaml anahtarıdır. Katman adı "
        "\"<Bileşen> · <bölge-id>\" biçimindedir; bölge-id ekran kartındaki regions[].id ile eşleşir. \"texts\" = örnekte "
        "görünen metinler (check_design_texts.py bunları kullanır).")

merged = {}
for p in sys.argv[1:]:
    raw = open(p).read().strip()
    if raw.startswith("TOO_LONG"):
        sys.exit(f"{p}: dışa aktarım 20 KB'ı aştı — export.js'de ONLY'yi daralt.")
    for screen, states in json.loads(raw).items():
        merged.setdefault(screen, {}).update(states)

for screen, states in merged.items():
    out = {"screen": screen, "note": NOTE, "states": {}}
    for st, v in states.items():
        comps = set()
        def walk(n):
            if isinstance(n, dict):
                if n.get("manifest"): comps.add(n["manifest"])
                for c in n.get("children", []): walk(c)
        walk(v["tree"])
        missing = []
        def unmapped(n):
            if isinstance(n, dict):
                if "component" in n and not n.get("manifest"): missing.append(n["component"])
                for c in n.get("children", []): unmapped(c)
        unmapped(v["tree"])
        if missing:
            print(f"⚠ {screen}@{st}: manifestte olmayan bileşen örnekleri: {sorted(set(missing))}")
        out["states"][st] = {"figmaNodeId": v["figmaNodeId"], "size": v["size"], "image": f"states/{st}.png",
                             "componentsUsed": sorted(comps), "tree": v["tree"]}
    d = os.path.join(ROOT, "design/screens", screen)
    os.makedirs(os.path.join(d, "states"), exist_ok=True)
    json.dump(out, open(os.path.join(d, "structure.json"), "w"), ensure_ascii=False, indent=1)
    heights = ", ".join(f"{k}={int(v['size'][1])}" for k, v in states.items())
    print(f"✓ {screen}: {len(states)} durum → design/screens/{screen}/structure.json (yükseklikler: {heights})")
