#!/usr/bin/env python3
"""Detect Figma token drift vs AppColors.kt.

Usage:
    python3 detect_token_drift.py --figma-node 54:1021
    python3 detect_token_drift.py --figma-code context.txt
"""
import argparse, re, os, sys

APPCOLORS_PATH = "composeApp/src/commonMain/kotlin/org/example/project/theme/AppColors.kt"

def parse_figma_colors(code: str) -> dict[str, int]:
    colors: dict[str, int] = {}
    # bg-[#HEX], text-[#HEX], etc.
    for match in re.finditer(r'(?:bg|text|border|shadow|from|to|via)-\[#([0-9A-Fa-f]{6})\]', code):
        c = f"#{match.group(1).upper()}"
        colors[c] = colors.get(c, 0) + 1
    # rgba(R,G,B,A)
    for match in re.finditer(r'rgba\((\d+),\s*(\d+),\s*(\d+),\s*([\d.]+)\)', code):
        r, g, b, a = int(match.group(1)), int(match.group(2)), int(match.group(3)), float(match.group(4))
        c = f"#{r:02X}{g:02X}{b:02X}"
        if a < 1:
            c += f" (a={a})"
        colors[c] = colors.get(c, 0) + 1
    return dict(sorted(colors.items(), key=lambda x: -x[1]))

def parse_app_colors(path: str) -> set[str]:
    defined = set()
    with open(path) as f:
        content = f.read()
    for match in re.finditer(r'Color\(0x([0-9A-Fa-f]{8})\)', content):
        full = match.group(1).upper()
        alpha, rgb = full[:2], full[2:]
        defined.add(f"#{rgb}")
        if alpha != "FF":
            a = round(int(alpha, 16) / 255, 2)
            defined.add(f"#{rgb} (a={a})")
    return defined

def main():
    parser = argparse.ArgumentParser(description='Detect Figma token drift')
    parser.add_argument('--figma-node', help='Figma node ID')
    parser.add_argument('--figma-code', help='Saved design context file')
    parser.add_argument('--app-colors', default=APPCOLORS_PATH)
    args = parser.parse_args()

    if args.figma_code:
        with open(args.figma_code) as f:
            code = f.read()
    elif args.figma_node:
        sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
        from download_figma_assets import get_design_context
        print(f"Fetching design context for {args.figma_node}...")
        code = get_design_context(args.figma_node)
        if not code:
            print("ERROR: Could not get design context")
            sys.exit(1)
    else:
        parser.error("--figma-node or --figma-code required")
        return

    figma_colors = parse_figma_colors(code)
    app_colors = parse_app_colors(args.app_colors)

    matched, missing = [], []
    for color, count in figma_colors.items():
        base = color.split(' ')[0]
        if base in app_colors or color in app_colors:
            matched.append((color, count))
        else:
            missing.append((color, count))

    print("=" * 50)
    print("  TOKEN DRIFT REPORT")
    print("=" * 50)
    print(f"\nMatched ({len(matched)}):")
    for c, n in matched:
        print(f"  OK  {c} (x{n})")
    if missing:
        print(f"\nDrift ({len(missing)}):")
        for c, n in missing:
            print(f"  !!  {c} (x{n})")
    else:
        print(f"\nAll Figma colors found in AppColors!")
    print(f"\nTotal: {len(figma_colors)} Figma, {len(app_colors)} AppColors")

if __name__ == '__main__':
    main()
