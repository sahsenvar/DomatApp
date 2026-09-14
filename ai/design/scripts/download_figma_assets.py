#!/usr/bin/env python3
"""Download all assets from a Figma node via MCP.

Usage:
    python3 download_figma_assets.py --node-id 54:1021 -o figma-assets/
"""
import argparse, os, re, sys, json, http.client, urllib.request

def get_design_context(node_id: str) -> str:
    """Get design context from Figma MCP."""
    conn = http.client.HTTPConnection('127.0.0.1', 3845, timeout=30)
    headers = {'Content-Type': 'application/json', 'Accept': 'application/json, text/event-stream'}

    conn.request('POST', '/mcp', json.dumps({
        'jsonrpc': '2.0', 'id': 1, 'method': 'initialize',
        'params': {'protocolVersion': '2024-11-05', 'capabilities': {},
                   'clientInfo': {'name': 'downloader', 'version': '1.0'}}
    }), headers)
    resp = conn.getresponse()
    session = resp.getheader('mcp-session-id')
    resp.read()

    conn2 = http.client.HTTPConnection('127.0.0.1', 3845, timeout=10)
    h2 = dict(headers); h2['mcp-session-id'] = session
    conn2.request('POST', '/mcp', json.dumps({
        'jsonrpc': '2.0', 'method': 'notifications/initialized'
    }), h2)
    conn2.getresponse().read()

    conn3 = http.client.HTTPConnection('127.0.0.1', 3845, timeout=30)
    h3 = dict(headers); h3['mcp-session-id'] = session
    conn3.request('POST', '/mcp', json.dumps({
        'jsonrpc': '2.0', 'id': 2, 'method': 'tools/call',
        'params': {'name': 'get_design_context', 'arguments': {'nodeId': node_id}}
    }), h3)
    raw = conn3.getresponse().read().decode()

    for line in raw.split('\n'):
        if line.startswith('data: '):
            try:
                data = json.loads(line[6:])
                for item in data.get('result', {}).get('content', []):
                    if item.get('type') == 'text':
                        return item['text']
            except:
                pass
    return ""

def extract_asset_urls(context: str) -> list[dict]:
    assets = []
    seen = set()
    pattern = r'http://localhost:3845/assets/([a-f0-9]+)\.(svg|png)'
    for match in re.finditer(pattern, context):
        url = match.group(0)
        hash_id = match.group(1)
        ext = match.group(2)
        if hash_id not in seen:
            seen.add(hash_id)
            assets.append({'url': url, 'hash': hash_id, 'ext': ext})
    return assets

def download_assets(assets: list[dict], output_dir: str):
    os.makedirs(output_dir, exist_ok=True)
    for asset in assets:
        filename = f"{asset['hash'][:12]}.{asset['ext']}"
        output_path = os.path.join(output_dir, filename)
        try:
            urllib.request.urlretrieve(asset['url'], output_path)
            size = os.path.getsize(output_path)
            print(f"  {filename} ({size:,} bytes) [{asset['ext'].upper()}]")
        except Exception as e:
            print(f"  FAIL {filename}: {e}")

def main():
    parser = argparse.ArgumentParser(description='Download Figma MCP assets')
    parser.add_argument('--node-id', required=True, help='Figma node ID (e.g. 54:1021)')
    parser.add_argument('-o', '--output', default='figma-assets/', help='Output directory')
    args = parser.parse_args()

    print(f"Fetching design context for node {args.node_id}...")
    context = get_design_context(args.node_id)
    if not context:
        print("ERROR: Could not get design context from MCP")
        sys.exit(1)

    assets = extract_asset_urls(context)
    print(f"Found {len(assets)} unique assets\n")
    if not assets:
        print("No assets found.")
        return

    download_assets(assets, args.output)
    print(f"\nDone: {len(assets)} assets -> {args.output}")

if __name__ == '__main__':
    main()
