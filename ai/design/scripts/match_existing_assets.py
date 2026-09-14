#!/usr/bin/env python3
"""Match Figma SVGs with existing XML Vector Drawable assets.

Usage:
    python3 match_existing_assets.py --svg figma-assets/abc.svg
    python3 match_existing_assets.py --svg-dir figma-assets/
"""
import argparse, os, re, sys
from xml.etree import ElementTree as ET

DRAWABLE_DIR = "composeApp/src/commonMain/composeResources/drawable"

def normalize_path(d: str) -> str:
    return re.sub(r'\s+', ' ', d.strip())

def extract_paths_from_svg(svg_path: str) -> list[str]:
    tree = ET.parse(svg_path)
    root = tree.getroot()
    paths = []
    for el in root.iter('{http://www.w3.org/2000/svg}path'):
        d = el.get('d', '').strip()
        if d:
            paths.append(normalize_path(d))
    return sorted(paths)

def extract_paths_from_xml_vd(xml_path: str) -> list[str]:
    tree = ET.parse(xml_path)
    root = tree.getroot()
    paths = []
    for el in root.iter():
        pd = el.get('{http://schemas.android.com/apk/res/android}pathData', '').strip()
        if pd:
            paths.append(normalize_path(pd))
    return sorted(paths)

def path_similarity(a: list[str], b: list[str]) -> float:
    if not a or not b:
        return 0.0
    set_a, set_b = set(a), set(b)
    intersection = set_a & set_b
    union = set_a | set_b
    return (len(intersection) / len(union)) * 100 if union else 100.0

def find_matches(svg_path: str, drawable_dir: str, threshold: float = 80.0):
    svg_paths = extract_paths_from_svg(svg_path)
    matches = []
    xml_files = [f for f in os.listdir(drawable_dir) if f.endswith('.xml')]
    for xml_file in xml_files:
        xml_full = os.path.join(drawable_dir, xml_file)
        try:
            vd_paths = extract_paths_from_xml_vd(xml_full)
            sim = path_similarity(svg_paths, vd_paths)
            if sim >= threshold:
                matches.append((xml_file, sim))
        except ET.ParseError:
            continue
    matches.sort(key=lambda x: x[1], reverse=True)
    return matches

def main():
    parser = argparse.ArgumentParser(description='Match Figma SVG with existing drawables')
    parser.add_argument('--svg', help='Single SVG file')
    parser.add_argument('--svg-dir', help='SVG directory')
    parser.add_argument('--drawable-dir', default=DRAWABLE_DIR)
    parser.add_argument('--threshold', type=float, default=80.0)
    args = parser.parse_args()

    svgs = []
    if args.svg:
        svgs = [args.svg]
    elif args.svg_dir:
        svgs = [os.path.join(args.svg_dir, f) for f in sorted(os.listdir(args.svg_dir)) if f.endswith('.svg')]
    else:
        parser.error("--svg or --svg-dir required")

    found, missing = 0, 0
    for svg in svgs:
        matches = find_matches(svg, args.drawable_dir, args.threshold)
        name = os.path.basename(svg)
        if matches:
            best = matches[0]
            print(f"  MATCH  {name} -> {best[0]} ({best[1]:.0f}%)")
            found += 1
        else:
            print(f"  NEW    {name} -> no match (new asset needed)")
            missing += 1

    print(f"\nSummary: {found} matched, {missing} new assets needed")

if __name__ == '__main__':
    main()
