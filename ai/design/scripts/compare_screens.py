#!/usr/bin/env python3
"""Compare Figma screenshots with Paparazzi snapshots.

Usage:
    python3 compare_screens.py --screen effortless --figma-node 54:1021
    python3 compare_screens.py --list  (show available Paparazzi snapshots)
"""
import argparse, subprocess, os, sys, json, base64, http.client, glob

SCREENSHOTS_DIR = "screenshots"
FIGMA_DIR = os.path.join(SCREENSHOTS_DIR, "figma")
DIFF_DIR = os.path.join(SCREENSHOTS_DIR, "diff")
PAPARAZZI_DIR = "composeApp/src/test/snapshots"

def ensure_dirs():
    for d in [FIGMA_DIR, DIFF_DIR]:
        os.makedirs(d, exist_ok=True)

def capture_figma(node_id: str, screen_name: str) -> str:
    output = os.path.join(FIGMA_DIR, f"{screen_name}_figma.png")
    conn = http.client.HTTPConnection('127.0.0.1', 3845, timeout=30)
    headers = {'Content-Type': 'application/json', 'Accept': 'application/json, text/event-stream'}

    conn.request('POST', '/mcp', json.dumps({
        'jsonrpc': '2.0', 'id': 1, 'method': 'initialize',
        'params': {'protocolVersion': '2024-11-05', 'capabilities': {},
                   'clientInfo': {'name': 'compare', 'version': '1.0'}}
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
        'params': {'name': 'get_screenshot', 'arguments': {'nodeId': node_id}}
    }), h3)
    raw = conn3.getresponse().read().decode()

    for line in raw.split('\n'):
        if line.startswith('data: '):
            try:
                data = json.loads(line[6:])
                for item in data.get('result', {}).get('content', []):
                    if item.get('type') == 'image':
                        with open(output, 'wb') as f:
                            f.write(base64.b64decode(item['data']))
                        print(f"  Figma: {output}")
                        return output
            except:
                pass
    print("ERROR: Could not capture Figma screenshot")
    sys.exit(1)

def find_paparazzi_snapshot(screen_name: str) -> str:
    pattern = os.path.join(PAPARAZZI_DIR, "**", f"*{screen_name}*")
    matches = glob.glob(pattern, recursive=True)
    pngs = [m for m in matches if m.endswith('.png')]
    if pngs:
        return pngs[0]
    print(f"ERROR: No Paparazzi snapshot found for '{screen_name}'")
    print(f"Run: ./gradlew :composeApp:recordPaparazziDebug")
    sys.exit(1)

def run_diff(figma: str, compose: str, screen_name: str):
    diff_path = os.path.join(DIFF_DIR, f"{screen_name}_diff.png")
    result = subprocess.run([
        "python3", "ai/scripts/pixel_diff.py",
        figma, compose, "-o", diff_path, "-t", "5"
    ], capture_output=True, text=True)
    print(result.stdout)
    if result.stderr:
        print(result.stderr)

def list_snapshots():
    if not os.path.isdir(PAPARAZZI_DIR):
        print(f"No snapshots dir: {PAPARAZZI_DIR}")
        print("Run: ./gradlew :composeApp:recordPaparazziDebug")
        return
    for root, _, files in os.walk(PAPARAZZI_DIR):
        for f in sorted(files):
            if f.endswith('.png'):
                print(f"  {os.path.join(root, f)}")

def main():
    parser = argparse.ArgumentParser(description='Compare Figma vs Paparazzi screenshots')
    parser.add_argument('--screen', help='Screen name (effortless, pricing, etc.)')
    parser.add_argument('--figma-node', help='Figma node ID')
    parser.add_argument('--list', action='store_true', help='List Paparazzi snapshots')
    parser.add_argument('--skip-figma', action='store_true')
    args = parser.parse_args()

    if args.list:
        list_snapshots()
        return

    if not args.screen or not args.figma_node:
        parser.error("--screen and --figma-node required")

    ensure_dirs()

    if not args.skip_figma:
        figma = capture_figma(args.figma_node, args.screen)
    else:
        figma = os.path.join(FIGMA_DIR, f"{args.screen}_figma.png")

    compose = find_paparazzi_snapshot(args.screen)
    print(f"  Compose: {compose}")

    run_diff(figma, compose, args.screen)

if __name__ == '__main__':
    main()
