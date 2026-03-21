#!/usr/bin/env python3
"""SVG → Android XML Vector Drawable converter for Compose Multiplatform.

Usage:
    python3 svg_to_xml_vd.py input.svg -o output.xml
    python3 svg_to_xml_vd.py figma-assets/ -o composeResources/drawable/ --prefix ic_
"""
import argparse, re, os, sys
from xml.etree import ElementTree as ET

def parse_svg(svg_path: str) -> dict:
    """SVG dosyasini parse et."""
    tree = ET.parse(svg_path)
    root = tree.getroot()
    ns = 'http://www.w3.org/2000/svg'

    viewBox = root.get('viewBox', '0 0 24 24').split()
    vw, vh = float(viewBox[2]), float(viewBox[3])

    paths = []
    for path_el in root.iter(f'{{{ns}}}path'):
        d = path_el.get('d', '')
        if not d.strip():
            continue
        fill = path_el.get('fill', '#000000')
        fill_match = re.search(r'var\(--fill-\d+,\s*(#[0-9A-Fa-f]{6})\)', fill)
        if fill_match:
            fill = fill_match.group(1)
        if fill.lower() == 'none':
            continue
        # Clean degenerate repeated V/H commands
        d = re.sub(r'(V[\d.]+)(V[\d.]+)+', r'\1', d)
        d = re.sub(r'(H[\d.]+)(H[\d.]+)+', r'\1', d)
        paths.append({'d': d.strip(), 'fill': fill.upper()})

    return {'vw': vw, 'vh': vh, 'paths': paths}

def generate_xml_vd(data: dict) -> str:
    """XML Vector Drawable string uret."""
    vw, vh = data['vw'], data['vh']
    scale = 24.0 / max(vw, vh)
    w_dp = round(vw * scale, 1)
    h_dp = round(vh * scale, 1)

    lines = [
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        f'    android:width="{w_dp}dp"',
        f'    android:height="{h_dp}dp"',
        f'    android:viewportWidth="{vw}"',
        f'    android:viewportHeight="{vh}">',
    ]
    for path in data['paths']:
        lines.append('    <path')
        lines.append(f'        android:pathData="{path["d"]}"')
        lines.append(f'        android:fillColor="{path["fill"]}" />')
    lines.append('</vector>')
    return '\n'.join(lines) + '\n'

def convert_file(svg_path: str, output_path: str):
    data = parse_svg(svg_path)
    xml = generate_xml_vd(data)
    os.makedirs(os.path.dirname(output_path) or '.', exist_ok=True)
    with open(output_path, 'w') as f:
        f.write(xml)
    print(f"  {os.path.basename(svg_path)} -> {os.path.basename(output_path)} ({len(data['paths'])} path)")

def main():
    parser = argparse.ArgumentParser(description='SVG to XML Vector Drawable')
    parser.add_argument('input', help='SVG file or directory')
    parser.add_argument('-o', '--output', required=True, help='Output file or directory')
    parser.add_argument('--prefix', default='ic_', help='Filename prefix for batch (default: ic_)')
    args = parser.parse_args()

    if os.path.isdir(args.input):
        os.makedirs(args.output, exist_ok=True)
        svgs = sorted(f for f in os.listdir(args.input) if f.endswith('.svg'))
        if not svgs:
            print("No SVG files found.")
            return
        print(f"Converting {len(svgs)} SVGs:\n")
        for svg in svgs:
            name = args.prefix + os.path.splitext(svg)[0].replace('-', '_') + '.xml'
            convert_file(os.path.join(args.input, svg), os.path.join(args.output, name))
        print(f"\nDone: {len(svgs)} files -> {args.output}")
    else:
        convert_file(args.input, args.output)
        print("Done.")

if __name__ == '__main__':
    main()
