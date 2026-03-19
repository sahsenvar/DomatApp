#!/usr/bin/env python3
"""
Figma Asset Extractor — Classify and export images/SVGs from a Figma file.

Usage:
  python extract_assets.py --token FIGMA_TOKEN --file-key FILE_KEY --node-id NODE_ID [--output ./assets]

Requirements:
  pip install requests

What it does:
  1. Fetches node tree from Figma REST API
  2. Classifies each node (RASTER_IMAGE / VECTOR_SVG / SHAPE / TEXT / CONTAINER)
  3. Exports raster images as PNG (2x scale)
  4. Exports vectors as SVG
  5. Generates asset-manifest.json
"""

import argparse
import json
import os
import sys
import time
from pathlib import Path

try:
    import requests
except ImportError:
    print("Install requests: pip install requests")
    sys.exit(1)

BASE_URL = "https://api.figma.com/v1"


def figma_get(endpoint: str, token: str, params: dict = None) -> dict:
    """Make authenticated GET request to Figma API."""
    headers = {"X-FIGMA-TOKEN": token}
    url = f"{BASE_URL}/{endpoint}"
    resp = requests.get(url, headers=headers, params=params)
    if resp.status_code == 429:
        wait = int(resp.headers.get("Retry-After", 30))
        print(f"  Rate limited, waiting {wait}s...")
        time.sleep(wait)
        return figma_get(endpoint, token, params)
    resp.raise_for_status()
    return resp.json()


def classify_node(node: dict) -> str:
    """Classify a Figma node into asset categories."""
    fills = node.get("fills", [])
    node_type = node.get("type", "")
    bbox = node.get("absoluteBoundingBox", {})
    w = bbox.get("width", 0)
    h = bbox.get("height", 0)

    # Image fills → raster photo
    if any(f.get("type") == "IMAGE" and f.get("visible", True) for f in fills):
        return "RASTER_IMAGE"

    # Vector node types
    if node_type in ("VECTOR", "BOOLEAN_OPERATION", "LINE", "STAR", "POLYGON"):
        return "VECTOR_SVG"

    # Text
    if node_type == "TEXT":
        return "TEXT"

    # Small component/frame likely an icon
    if node_type in ("COMPONENT", "INSTANCE", "FRAME", "GROUP"):
        if 0 < w <= 64 and 0 < h <= 64:
            children = node.get("children", [])
            if children and all(
                classify_node(c) in ("VECTOR_SVG", "SHAPE") for c in children
            ):
                return "VECTOR_SVG"

    # Container types
    if node_type in ("FRAME", "GROUP", "SECTION", "COMPONENT_SET", "COMPONENT", "INSTANCE"):
        return "CONTAINER"

    # Shapes with solid/gradient fills
    fill_types = {f.get("type", "") for f in fills}
    if fill_types & {"SOLID", "GRADIENT_LINEAR", "GRADIENT_RADIAL", "GRADIENT_ANGULAR", "GRADIENT_DIAMOND"}:
        return "SHAPE"

    return "UNKNOWN"


def walk_nodes(node: dict, results: list, depth: int = 0):
    """Recursively walk node tree and classify each node."""
    classification = classify_node(node)
    node_id = node.get("id", "")
    name = node.get("name", "unnamed")
    bbox = node.get("absoluteBoundingBox", {})

    if classification in ("RASTER_IMAGE", "VECTOR_SVG"):
        results.append({
            "id": node_id,
            "name": name,
            "type": classification,
            "figma_type": node.get("type", ""),
            "width": bbox.get("width", 0),
            "height": bbox.get("height", 0),
            "depth": depth,
        })

    for child in node.get("children", []):
        walk_nodes(child, results, depth + 1)


def sanitize_filename(name: str) -> str:
    """Make a safe filename from Figma layer name."""
    safe = "".join(c if c.isalnum() or c in "-_" else "_" for c in name)
    return safe.strip("_")[:60] or "unnamed"


def export_assets(token: str, file_key: str, assets: list, output_dir: Path):
    """Export classified assets from Figma."""
    images_dir = output_dir / "images"
    icons_dir = output_dir / "icons"
    images_dir.mkdir(parents=True, exist_ok=True)
    icons_dir.mkdir(parents=True, exist_ok=True)

    raster_ids = [a["id"] for a in assets if a["type"] == "RASTER_IMAGE"]
    vector_ids = [a["id"] for a in assets if a["type"] == "VECTOR_SVG"]

    exported = {}

    # Export rasters as PNG @2x
    if raster_ids:
        print(f"\n📸 Exporting {len(raster_ids)} raster images as PNG @2x...")
        # Figma API accepts comma-separated IDs (max ~50 at a time)
        for batch_start in range(0, len(raster_ids), 50):
            batch = raster_ids[batch_start:batch_start + 50]
            ids_str = ",".join(batch)
            data = figma_get(f"images/{file_key}", token, {
                "ids": ids_str,
                "format": "png",
                "scale": 2,
            })
            for node_id, url in data.get("images", {}).items():
                if url:
                    asset = next(a for a in assets if a["id"] == node_id)
                    filename = f"{sanitize_filename(asset['name'])}@2x.png"
                    filepath = images_dir / filename
                    print(f"  ↓ {filename}")
                    img_data = requests.get(url).content
                    filepath.write_bytes(img_data)
                    exported[node_id] = {
                        "path": str(filepath.relative_to(output_dir)),
                        "format": "png",
                        "scale": 2,
                    }

    # Export vectors as SVG
    if vector_ids:
        print(f"\n🎨 Exporting {len(vector_ids)} vectors as SVG...")
        for batch_start in range(0, len(vector_ids), 50):
            batch = vector_ids[batch_start:batch_start + 50]
            ids_str = ",".join(batch)
            data = figma_get(f"images/{file_key}", token, {
                "ids": ids_str,
                "format": "svg",
                "svg_outline_text": "true",
                "svg_include_id": "true",
            })
            for node_id, url in data.get("images", {}).items():
                if url:
                    asset = next(a for a in assets if a["id"] == node_id)
                    filename = f"{sanitize_filename(asset['name'])}.svg"
                    filepath = icons_dir / filename
                    print(f"  ↓ {filename}")
                    svg_data = requests.get(url).text
                    filepath.write_text(svg_data, encoding="utf-8")
                    exported[node_id] = {
                        "path": str(filepath.relative_to(output_dir)),
                        "format": "svg",
                    }

    return exported


def main():
    parser = argparse.ArgumentParser(description="Extract and classify Figma assets")
    parser.add_argument("--token", required=True, help="Figma personal access token")
    parser.add_argument("--file-key", required=True, help="Figma file key")
    parser.add_argument("--node-id", required=True, help="Root node ID to scan")
    parser.add_argument("--output", default="./assets", help="Output directory")
    parser.add_argument("--dry-run", action="store_true", help="Classify only, don't download")
    args = parser.parse_args()

    output_dir = Path(args.output)
    output_dir.mkdir(parents=True, exist_ok=True)

    # Fetch node tree
    print(f"🔍 Fetching node tree for {args.node_id}...")
    node_data = figma_get(f"files/{args.file_key}/nodes", args.token, {"ids": args.node_id})

    nodes = node_data.get("nodes", {})
    if not nodes:
        print("❌ No nodes found. Check file key and node ID.")
        sys.exit(1)

    root_node = list(nodes.values())[0].get("document", {})

    # Classify
    assets = []
    walk_nodes(root_node, assets)

    print(f"\n📊 Classification Results:")
    rasters = [a for a in assets if a["type"] == "RASTER_IMAGE"]
    vectors = [a for a in assets if a["type"] == "VECTOR_SVG"]
    print(f"  📸 Raster images: {len(rasters)}")
    for a in rasters:
        print(f"     - {a['name']} ({a['width']:.0f}x{a['height']:.0f})")
    print(f"  🎨 Vector SVGs:   {len(vectors)}")
    for a in vectors:
        print(f"     - {a['name']} ({a['width']:.0f}x{a['height']:.0f})")

    if args.dry_run:
        print("\n(Dry run — no files downloaded)")
        manifest = {"assets": assets, "exported": {}}
    else:
        exported = export_assets(args.token, args.file_key, assets, output_dir)
        manifest = {"assets": assets, "exported": exported}

    # Write manifest
    manifest_path = output_dir / "manifest.json"
    manifest_path.write_text(json.dumps(manifest, indent=2, ensure_ascii=False))
    print(f"\n✅ Manifest written to {manifest_path}")
    print(f"📁 Assets in {output_dir}/")


if __name__ == "__main__":
    main()
