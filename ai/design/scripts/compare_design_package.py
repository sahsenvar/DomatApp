#!/usr/bin/env python3
"""Compare a screen's design package against Roborazzi preview renders.

Usage:
  ./gradlew :composeApp:recordRoborazziDebug
  python3 ai/design/scripts/compare_design_package.py C4 [--threshold 3.5] [--out build/design-diff]

Matching: a preview named "<ID>@<state>" (the @Preview `name`) is compared with
design/screens/<ID>/states/<state>.png. Both are expected at 2x (390 dp -> 780 px).
Output per state: <out>/<ID>/<state>.png = [design | compose | diff] and a summary table.
Diff % = share of pixels whose max channel difference exceeds --tolerance against EVERY compose pixel
within --shift px (default 1 px = 0.5 dp). Figma and Skia rasterise glyph edges differently; without the
neighbourhood check every text edge counts as a difference (~5 % on a text-heavy screen) and hides real
layout drift. "raw" (no neighbourhood) is printed for reference; the threshold applies to "diff".
Exit code 1 if any state exceeds the threshold or has no render.
"""
import argparse, glob, os, re, sys
import numpy as np
from PIL import Image

ap = argparse.ArgumentParser()
ap.add_argument("screen")
ap.add_argument("--threshold", type=float, default=3.5, help="max %% differing pixels (calibrated: whole screen shifted 2 dp ~ 3 %%, 4 dp ~ 4.2 %%)")
ap.add_argument("--tolerance", type=int, default=24, help="per-channel difference ignored as AA noise")
ap.add_argument("--shift", type=int, default=1, help="px neighbourhood tolerated for AA / sub-pixel offsets")
ap.add_argument("--renders", default="composeApp/build/outputs/roborazzi")
ap.add_argument("--out", default="build/design-diff")
a = ap.parse_args()

states_dir = f"design/screens/{a.screen}/states"
states = sorted(os.path.splitext(f)[0] for f in os.listdir(states_dir) if f.endswith(".png"))
renders = glob.glob(os.path.join(a.renders, "*.png"))
os.makedirs(os.path.join(a.out, a.screen), exist_ok=True)
rows, failed = [], False
for st in states:
    pat = re.compile(re.escape(f"{a.screen}@{st}") + r"(_W\d+dp_H\d+dp)?\.png$")
    match = [r for r in renders if pat.search(os.path.basename(r))]
    if not match:
        rows.append((st, "-", "-", "-", "NO RENDER")); failed = True; continue
    d = Image.open(os.path.join(states_dir, st + ".png")).convert("RGB")
    c = Image.open(max(match, key=os.path.getmtime)).convert("RGB")
    w, h = max(d.width, c.width), max(d.height, c.height)
    dd = Image.new("RGB", (w, h), "white"); dd.paste(d, (0, 0))
    cc = Image.new("RGB", (w, h), "white"); cc.paste(c, (0, 0))
    D = np.asarray(dd, dtype=np.int16); C = np.asarray(cc, dtype=np.int16)
    raw_m = np.abs(D - C).max(axis=2) > a.tolerance
    best = np.full(raw_m.shape, 255, dtype=np.int16)
    k = a.shift
    Cp = np.pad(C, ((k, k), (k, k), (0, 0)), mode="edge")
    for dy in range(-k, k + 1):
        for dx in range(-k, k + 1):
            sh = Cp[k + dy:k + dy + h, k + dx:k + dx + w]
            best = np.minimum(best, np.abs(D - sh).max(axis=2))
    mask = best > a.tolerance
    pct = 100.0 * mask.mean(); raw = 100.0 * raw_m.mean()
    m = Image.fromarray((mask * 255).astype(np.uint8))
    heat = Image.new("RGB", (w, h), (255, 255, 255)); heat.paste((255, 0, 64), mask=m)
    heat = Image.blend(dd, heat, 0.6)
    sheet = Image.new("RGB", (w * 3 + 40, h), (40, 40, 40))
    sheet.paste(dd, (0, 0)); sheet.paste(cc, (w + 20, 0)); sheet.paste(heat, (2 * w + 40, 0))
    sheet.save(os.path.join(a.out, a.screen, st + ".png"))
    ok = pct <= a.threshold and d.size == c.size
    failed |= not ok
    rows.append((st, f"{d.size}", f"{c.size}", f"{raw:.2f}%", f"{pct:.2f}% {'OK' if ok else 'FAIL'}"))
print(f"| state | design px | compose px | raw | diff |\n|---|---|---|---|---|")
for r in rows: print("| " + " | ".join(r) + " |")
sys.exit(1 if failed else 0)
