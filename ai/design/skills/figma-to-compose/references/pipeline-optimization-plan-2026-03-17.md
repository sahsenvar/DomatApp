# Figma Pipeline Optimization — 6 Araç

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Figma-to-Compose pipeline'ını 6 yeni araçla otomatize et: SVG dönüştürücü, screenshot karşılaştırma, asset eşleştirme, batch indirici, Roborazzi entegrasyonu, token drift algılama.

**Architecture:** Her araç bağımsız Python/Kotlin scripti olarak çalışır. `make validate-all` altında birleşir. Screenshot karşılaştırma mevcut `ScreenCaptureTest` + `pixel_diff.py` üzerine inşa edilir. Roborazzi Desktop modülü JVM'de composable render eder.

**Tech Stack:** Python 3, Kotlin, Roborazzi (JVM/Desktop), ADB, Figma MCP (localhost:3845), Compose Multiplatform

---

## Dosya Haritası

### Yeni dosyalar:
| Dosya | Sorumluluk |
|-------|-----------|
| `ai/design/scripts/svg_to_xml_vd.py` | SVG → XML Vector Drawable dönüştürücü |
| `ai/design/scripts/match_existing_assets.py` | Figma SVG vs mevcut drawable eşleştirme |
| `ai/design/scripts/download_figma_assets.py` | MCP'den toplu SVG/PNG indirme |
| `ai/design/scripts/detect_token_drift.py` | Figma token vs AppColors/AppTypography karşılaştırma |
| `ai/design/scripts/compare_screens.py` | Figma screenshot + compose screenshot + pixel diff orchestrator |

### Değiştirilecek dosyalar:
| Dosya | Değişiklik |
|-------|-----------|
| `composeApp/src/androidInstrumentedTest/.../ScreenCaptureTest.kt` | Tüm @Screen composable'ları otomatik capture |
| `Makefile` | Yeni script komutları ekleme |
| `build.gradle.kts` (composeApp) | Roborazzi Desktop bağımlılığı (Task 5) |
| `ai/design/skills/figma-to-compose/SKILL.md` | Yeni araç referansları |

---

## Paralellik Haritası

```
┌─────────────────────────────────────────────┐
│  PARALEL GRUP A (bağımsız scriptler)        │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐ │
│  │ Task 1   │ │ Task 3   │ │ Task 4       │ │
│  │ SVG→XML  │ │ Asset    │ │ MCP Batch    │ │
│  │ Converter│ │ Matcher  │ │ Downloader   │ │
│  └──────────┘ └──────────┘ └──────────────┘ │
│  ┌──────────┐                                │
│  │ Task 6   │                                │
│  │ Token    │                                │
│  │ Drift    │                                │
│  └──────────┘                                │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  PARALEL GRUP B (altyapı)                   │
│  ┌──────────────┐  ┌──────────────────────┐ │
│  │ Task 2       │  │ Task 5              │ │
│  │ Screenshot   │  │ Roborazzi Desktop   │ │
│  │ Comparison   │  │ (araştırma+entegre) │ │
│  └──────────────┘  └──────────────────────┘ │
└─────────────────────────────────────────────┘
```

Grup A ve B birbirine bağımlı değil, paralel çalışabilir.

---

## Task 1: SVG → XML Vector Drawable Converter

**Files:**
- Create: `ai/design/scripts/svg_to_xml_vd.py`
- Modify: `Makefile` (yeni komut)

- [ ] **Step 1: Script iskeletini yaz**

```python
#!/usr/bin/env python3
"""SVG → Android XML Vector Drawable converter for Compose Multiplatform.

Usage:
    python3 svg_to_xml_vd.py input.svg -o output.xml
    python3 svg_to_xml_vd.py figma-assets/ -o composeResources/drawable/  (batch)
"""
import argparse, re, os, sys
from xml.etree import ElementTree as ET

def parse_svg(svg_path: str) -> dict:
    """SVG dosyasını parse et → viewBox, paths, fills döndür."""
    tree = ET.parse(svg_path)
    root = tree.getroot()
    ns = {'svg': 'http://www.w3.org/2000/svg'}

    viewBox = root.get('viewBox', '0 0 24 24').split()
    vw, vh = float(viewBox[2]), float(viewBox[3])

    paths = []
    for path_el in root.iter('{http://www.w3.org/2000/svg}path'):
        d = path_el.get('d', '')
        fill = path_el.get('fill', '#000000')
        # var(--fill-0, #COLOR) → #COLOR
        fill_match = re.search(r'var\(--fill-\d+,\s*(#[0-9A-Fa-f]{6})\)', fill)
        if fill_match:
            fill = fill_match.group(1)
        if fill == 'none':
            continue
        paths.append({'d': d, 'fill': fill.upper()})

    return {'vw': vw, 'vh': vh, 'paths': paths}

def generate_xml_vd(data: dict, name: str) -> str:
    """XML Vector Drawable string üret."""
    vw, vh = data['vw'], data['vh']
    # width/height dp olarak — viewport'un büyüğünü 24dp'ye normalize et
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
    """Tek SVG dosyasını XML VD'ye çevir."""
    data = parse_svg(svg_path)
    name = os.path.splitext(os.path.basename(svg_path))[0]
    xml = generate_xml_vd(data, name)
    with open(output_path, 'w') as f:
        f.write(xml)
    print(f"✅ {svg_path} → {output_path} ({len(data['paths'])} path)")

def main():
    parser = argparse.ArgumentParser(description='SVG → XML Vector Drawable')
    parser.add_argument('input', help='SVG dosya veya dizin')
    parser.add_argument('-o', '--output', required=True, help='Çıktı dosya veya dizin')
    parser.add_argument('--prefix', default='ic_', help='Dosya adı prefix (default: ic_)')
    args = parser.parse_args()

    if os.path.isdir(args.input):
        os.makedirs(args.output, exist_ok=True)
        svgs = [f for f in os.listdir(args.input) if f.endswith('.svg')]
        for svg in svgs:
            name = args.prefix + os.path.splitext(svg)[0].replace('-', '_') + '.xml'
            convert_file(
                os.path.join(args.input, svg),
                os.path.join(args.output, name),
            )
        print(f"\n🎉 {len(svgs)} SVG dönüştürüldü")
    else:
        convert_file(args.input, args.output)

if __name__ == '__main__':
    main()
```

- [ ] **Step 2: Test et — tek dosya**

```bash
# Test SVG oluştur
echo '<svg viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M8 0L16 16H0Z" fill="var(--fill-0, #13EC49)"/></svg>' > /tmp/test.svg
python3 ai/design/scripts/svg_to_xml_vd.py /tmp/test.svg -o /tmp/test.xml
cat /tmp/test.xml
```

Expected: Valid XML VD with `fillColor="#13EC49"` and correct viewport.

- [ ] **Step 3: Test et — batch dizin**

```bash
mkdir -p /tmp/svgs
# Mevcut projedeki bir MCP SVG'yi kopyala simülasyonu
cp /tmp/test.svg /tmp/svgs/arrow.svg
python3 ai/design/scripts/svg_to_xml_vd.py /tmp/svgs/ -o /tmp/output/ --prefix ic_
ls /tmp/output/
```

Expected: `ic_arrow.xml` dosyası oluşmuş olmalı.

- [ ] **Step 4: Makefile'a ekle**

```makefile
# Mevcut validate-all altına veya ayrı target olarak:
convert-assets:
	python3 ai/design/scripts/svg_to_xml_vd.py figma-assets/ -o composeApp/src/commonMain/composeResources/drawable/ --prefix ic_
```

- [ ] **Step 5: Commit**

```bash
git add ai/design/scripts/svg_to_xml_vd.py Makefile
git commit -m "feat: add SVG to XML Vector Drawable converter script"
```

---

## Task 2: Otomatik Screenshot Karşılaştırma

**Files:**
- Create: `ai/design/scripts/compare_screens.py`
- Modify: `composeApp/src/androidInstrumentedTest/.../ScreenCaptureTest.kt`
- Modify: `Makefile`

- [ ] **Step 1: ScreenCaptureTest'i genişlet — tüm ekranları capture et**

Mevcut `ScreenCaptureTest.kt` sadece tek ekran için. Tüm `@Screen` composable'ları capture edecek şekilde genişlet:

```kotlin
package org.example.project.screenshots

import android.graphics.Bitmap
import android.os.Environment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import org.example.project.onboarding.*
import org.example.project.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import java.io.File

class ScreenCaptureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun captureScreen(name: String, content: @Composable () -> Unit) {
        composeTestRule.setContent {
            AppTheme { content() }
        }
        composeTestRule.waitForIdle()
        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "compose_screenshots"
        )
        dir.mkdirs()
        val file = File(dir, "${name}_compose.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun captureOnboardingEffortless() {
        captureScreen("effortless") {
            OnboardingEffortlessScreen(
                uiState = OnboardingUiState(),
                onIntent = {},
            )
        }
    }

    @Test
    fun captureOnboardingPricing() {
        captureScreen("pricing") {
            OnboardingPricingScreen(
                uiState = OnboardingUiState(),
                onIntent = {},
            )
        }
    }

    // Diğer ekranlar için benzer test metodları ekle...
}
```

- [ ] **Step 2: compare_screens.py orchestrator yaz**

```python
#!/usr/bin/env python3
"""Figma screenshot vs Compose screenshot karşılaştırma orchestrator.

Usage:
    python3 compare_screens.py --screen effortless --figma-node 54:1021
    python3 compare_screens.py --screen pricing --figma-node 54:772
"""
import argparse, subprocess, os, json, base64, http.client, sys

SCREENSHOTS_DIR = "screenshots"
FIGMA_DIR = os.path.join(SCREENSHOTS_DIR, "figma")
COMPOSE_DIR = os.path.join(SCREENSHOTS_DIR, "compose")
DIFF_DIR = os.path.join(SCREENSHOTS_DIR, "diff")

def ensure_dirs():
    for d in [FIGMA_DIR, COMPOSE_DIR, DIFF_DIR]:
        os.makedirs(d, exist_ok=True)

def capture_figma_screenshot(node_id: str, screen_name: str) -> str:
    """MCP JSON-RPC ile Figma screenshot'ını al ve kaydet."""
    output = os.path.join(FIGMA_DIR, f"{screen_name}_figma.png")

    conn = http.client.HTTPConnection('127.0.0.1', 3845, timeout=30)
    headers = {'Content-Type': 'application/json', 'Accept': 'application/json, text/event-stream'}

    # Initialize
    conn.request('POST', '/mcp', json.dumps({
        'jsonrpc': '2.0', 'id': 1, 'method': 'initialize',
        'params': {'protocolVersion': '2024-11-05', 'capabilities': {},
                   'clientInfo': {'name': 'compare', 'version': '1.0'}}
    }), headers)
    resp = conn.getresponse()
    session = resp.getheader('mcp-session-id')
    resp.read()

    # Initialized
    conn2 = http.client.HTTPConnection('127.0.0.1', 3845, timeout=10)
    h2 = dict(headers); h2['mcp-session-id'] = session
    conn2.request('POST', '/mcp', json.dumps({
        'jsonrpc': '2.0', 'method': 'notifications/initialized'
    }), h2)
    conn2.getresponse().read()

    # Screenshot
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
                        print(f"📸 Figma: {output}")
                        return output
            except:
                pass

    print("❌ Figma screenshot alınamadı")
    sys.exit(1)

def capture_compose_screenshot(screen_name: str) -> str:
    """Android cihazda instrumented test çalıştır ve screenshot'ı çek."""
    test_class = "org.example.project.screenshots.ScreenCaptureTest"
    test_method = f"capture{screen_name.title()}"

    print(f"🔨 Instrumented test çalıştırılıyor: {test_method}...")
    result = subprocess.run([
        "./gradlew", ":composeApp:connectedDebugAndroidTest",
        f"-Pandroid.testInstrumentationRunnerArguments.class={test_class}#{test_method}"
    ], capture_output=True, text=True, timeout=300)

    if result.returncode != 0:
        print(f"❌ Test başarısız:\n{result.stderr[-500:]}")
        sys.exit(1)

    # Cihazdan screenshot'ı çek
    remote_path = f"/sdcard/Pictures/compose_screenshots/{screen_name}_compose.png"
    local_path = os.path.join(COMPOSE_DIR, f"{screen_name}_compose.png")
    subprocess.run(["adb", "pull", remote_path, local_path], check=True)
    print(f"📸 Compose: {local_path}")
    return local_path

def run_pixel_diff(figma_path: str, compose_path: str, screen_name: str) -> float:
    """pixel_diff.py çalıştır ve diff yüzdesini döndür."""
    diff_path = os.path.join(DIFF_DIR, f"{screen_name}_diff.png")
    result = subprocess.run([
        "python3", "ai/design/scripts/pixel_diff.py",
        figma_path, compose_path,
        "-o", diff_path, "-t", "5"
    ], capture_output=True, text=True)

    print(result.stdout)
    # Parse diff percentage from output
    for line in result.stdout.split('\n'):
        if 'diff' in line.lower() and '%' in line:
            import re
            match = re.search(r'(\d+\.?\d*)%', line)
            if match:
                return float(match.group(1))
    return -1

def main():
    parser = argparse.ArgumentParser(description='Figma vs Compose screenshot comparison')
    parser.add_argument('--screen', required=True, help='Screen adı (effortless, pricing, etc.)')
    parser.add_argument('--figma-node', required=True, help='Figma node ID (e.g. 54:1021)')
    parser.add_argument('--skip-compose', action='store_true', help='Compose capture atla (önceki PNG kullan)')
    parser.add_argument('--skip-figma', action='store_true', help='Figma capture atla (önceki PNG kullan)')
    args = parser.parse_args()

    ensure_dirs()

    # 1. Figma screenshot
    if not args.skip_figma:
        figma_path = capture_figma_screenshot(args.figma_node, args.screen)
    else:
        figma_path = os.path.join(FIGMA_DIR, f"{args.screen}_figma.png")

    # 2. Compose screenshot
    if not args.skip_compose:
        compose_path = capture_compose_screenshot(args.screen)
    else:
        compose_path = os.path.join(COMPOSE_DIR, f"{args.screen}_compose.png")

    # 3. Pixel diff
    diff_pct = run_pixel_diff(figma_path, compose_path, args.screen)

    if diff_pct >= 0:
        status = "✅ PASS" if diff_pct <= 5 else "❌ FAIL"
        print(f"\n{'='*50}")
        print(f"  {args.screen}: {diff_pct:.1f}% fark — {status}")
        print(f"  Diff görsel: screenshots/diff/{args.screen}_diff.png")
        print(f"{'='*50}")

if __name__ == '__main__':
    main()
```

- [ ] **Step 3: Makefile'a ekle**

```makefile
compare-screen:
	python3 ai/design/scripts/compare_screens.py --screen $(SCREEN) --figma-node $(NODE)

compare-all:
	python3 ai/design/scripts/compare_screens.py --screen effortless --figma-node 54:1021
	python3 ai/design/scripts/compare_screens.py --screen pricing --figma-node 54:772
```

- [ ] **Step 4: Test et**

```bash
make compare-screen SCREEN=effortless NODE=54:1021
```

- [ ] **Step 5: Commit**

```bash
git add ai/design/scripts/compare_screens.py composeApp/src/androidInstrumentedTest/ Makefile
git commit -m "feat: add automated Figma vs Compose screenshot comparison"
```

---

## Task 3: Mevcut Asset Eşleştirme Scripti

**Files:**
- Create: `ai/design/scripts/match_existing_assets.py`

- [ ] **Step 1: Script yaz**

```python
#!/usr/bin/env python3
"""Figma SVG'yi projedeki mevcut XML VD drawable'larla eşleştir.

Usage:
    python3 match_existing_assets.py --svg figma-assets/abc.svg
    python3 match_existing_assets.py --svg-dir figma-assets/
"""
import argparse, os, re, sys
from xml.etree import ElementTree as ET

DRAWABLE_DIR = "composeApp/src/commonMain/composeResources/drawable"

def extract_paths_from_svg(svg_path: str) -> list[str]:
    """SVG'den path data'ları çıkar."""
    tree = ET.parse(svg_path)
    root = tree.getroot()
    paths = []
    for el in root.iter('{http://www.w3.org/2000/svg}path'):
        d = el.get('d', '').strip()
        if d:
            # Normalize: fazla boşlukları temizle
            d = re.sub(r'\s+', ' ', d)
            paths.append(d)
    return sorted(paths)

def extract_paths_from_xml_vd(xml_path: str) -> list[str]:
    """XML VD'den pathData'ları çıkar."""
    tree = ET.parse(xml_path)
    root = tree.getroot()
    ns = {'android': 'http://schemas.android.com/apk/res/android'}
    paths = []
    for el in root.iter():
        pd = el.get('{http://schemas.android.com/apk/res/android}pathData', '').strip()
        if pd:
            pd = re.sub(r'\s+', ' ', pd)
            paths.append(pd)
    return sorted(paths)

def path_similarity(a: list[str], b: list[str]) -> float:
    """İki path listesinin benzerlik oranını hesapla (0-100)."""
    if not a or not b:
        return 0.0
    # Basit: aynı path sayısı / toplam unique path sayısı
    set_a, set_b = set(a), set(b)
    if not set_a and not set_b:
        return 100.0
    intersection = set_a & set_b
    union = set_a | set_b
    return (len(intersection) / len(union)) * 100

def find_matches(svg_path: str, drawable_dir: str, threshold: float = 80.0):
    """SVG'yi tüm drawable'larla karşılaştır."""
    svg_paths = extract_paths_from_svg(svg_path)
    svg_name = os.path.basename(svg_path)

    matches = []
    xml_files = [f for f in os.listdir(drawable_dir) if f.endswith('.xml')]

    for xml_file in xml_files:
        xml_path = os.path.join(drawable_dir, xml_file)
        try:
            vd_paths = extract_paths_from_xml_vd(xml_path)
            sim = path_similarity(svg_paths, vd_paths)
            if sim >= threshold:
                matches.append((xml_file, sim))
        except ET.ParseError:
            continue

    matches.sort(key=lambda x: x[1], reverse=True)
    return matches

def main():
    parser = argparse.ArgumentParser(description='Match Figma SVG with existing drawables')
    parser.add_argument('--svg', help='Tek SVG dosya')
    parser.add_argument('--svg-dir', help='SVG dizini')
    parser.add_argument('--drawable-dir', default=DRAWABLE_DIR)
    parser.add_argument('--threshold', type=float, default=80.0, help='Min benzerlik % (default: 80)')
    args = parser.parse_args()

    svgs = []
    if args.svg:
        svgs = [args.svg]
    elif args.svg_dir:
        svgs = [os.path.join(args.svg_dir, f) for f in os.listdir(args.svg_dir) if f.endswith('.svg')]

    for svg in svgs:
        matches = find_matches(svg, args.drawable_dir, args.threshold)
        name = os.path.basename(svg)
        if matches:
            best = matches[0]
            print(f"✅ {name} → {best[0]} ({best[1]:.0f}% eşleşme)")
            for m in matches[1:]:
                print(f"   Alternatif: {m[0]} ({m[1]:.0f}%)")
        else:
            print(f"❌ {name} → Eşleşme bulunamadı (yeni asset gerekli)")

if __name__ == '__main__':
    main()
```

- [ ] **Step 2: Test et**

```bash
# Mevcut bir SVG ile test (MCP'den indir)
curl -s "http://localhost:3845/assets/dc7f178be321aea067cde670723f7c7fcfd8681c.svg" > /tmp/test_qr.svg
python3 ai/design/scripts/match_existing_assets.py --svg /tmp/test_qr.svg
```

Expected: `✅ test_qr.svg → ic_qr_code.xml (100% eşleşme)`

- [ ] **Step 3: Commit**

```bash
git add ai/design/scripts/match_existing_assets.py
git commit -m "feat: add Figma SVG to existing drawable matcher script"
```

---

## Task 4: MCP Batch Asset Downloader

**Files:**
- Create: `ai/design/scripts/download_figma_assets.py`

- [ ] **Step 1: Script yaz**

```python
#!/usr/bin/env python3
"""Figma MCP'den bir node'un tüm asset'lerini toplu indir.

Usage:
    python3 download_figma_assets.py --node-id 54:1021 -o figma-assets/
"""
import argparse, os, re, sys, json, http.client, urllib.request

def get_design_context(node_id: str) -> str:
    """MCP'den design context al."""
    conn = http.client.HTTPConnection('127.0.0.1', 3845, timeout=30)
    headers = {'Content-Type': 'application/json', 'Accept': 'application/json, text/event-stream'}

    # Initialize
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
    """Design context'ten localhost asset URL'lerini çıkar."""
    assets = []
    # http://localhost:3845/assets/HASH.svg veya .png
    pattern = r'http://localhost:3845/assets/([a-f0-9]+)\.(svg|png)'
    for match in re.finditer(pattern, context):
        url = match.group(0)
        hash_id = match.group(1)
        ext = match.group(2)
        assets.append({'url': url, 'hash': hash_id, 'ext': ext})
    return assets

def download_assets(assets: list[dict], output_dir: str):
    """Asset'leri indir."""
    os.makedirs(output_dir, exist_ok=True)
    for asset in assets:
        filename = f"{asset['hash'][:12]}.{asset['ext']}"
        output_path = os.path.join(output_dir, filename)
        try:
            urllib.request.urlretrieve(asset['url'], output_path)
            size = os.path.getsize(output_path)
            print(f"✅ {filename} ({size} bytes) — {asset['ext'].upper()}")
        except Exception as e:
            print(f"❌ {filename} — {e}")

def main():
    parser = argparse.ArgumentParser(description='Download Figma MCP assets')
    parser.add_argument('--node-id', required=True, help='Figma node ID')
    parser.add_argument('-o', '--output', default='figma-assets/', help='Çıktı dizini')
    args = parser.parse_args()

    print(f"📡 Figma design context alınıyor: {args.node_id}...")
    context = get_design_context(args.node_id)

    if not context:
        print("❌ Design context alınamadı")
        sys.exit(1)

    assets = extract_asset_urls(context)
    print(f"🔍 {len(assets)} asset bulundu\n")

    if not assets:
        print("Hiç asset bulunamadı.")
        return

    download_assets(assets, args.output)
    print(f"\n🎉 Tamamlandı: {args.output}")

if __name__ == '__main__':
    main()
```

- [ ] **Step 2: Test et**

```bash
python3 ai/design/scripts/download_figma_assets.py --node-id 54:1021 -o /tmp/figma-test-assets/
ls /tmp/figma-test-assets/
```

Expected: 4 SVG dosyası indirilmiş olmalı (QR, truck, tomato, arrow).

- [ ] **Step 3: Commit**

```bash
git add ai/design/scripts/download_figma_assets.py
git commit -m "feat: add MCP batch asset downloader script"
```

---

## Task 5: Roborazzi Desktop Araştırma + Entegrasyon

**Files:**
- Modify: `composeApp/build.gradle.kts` veya yeni `screenshot-tests/build.gradle.kts` modül
- Create: `screenshot-tests/` (gerekirse)

> **NOT:** Bu task araştırma ağırlıklı. Roborazzi Desktop'un CMP ile çalışıp çalışmadığını doğrulamak ve entegre etmek.

- [ ] **Step 1: Roborazzi Desktop GitHub'ı incele**

Araştır:
- `takahirom/roborazzi` → `roborazzi-compose-desktop` modülü var mı?
- CMP `commonMain` composable'ları JVM/Desktop hedefinde render edebilir mi?
- Mevcut projenin JVM target'ı var mı? (convention plugin `techspiration.kmp.library` → "NO JVM target" notu var)

- [ ] **Step 2: JVM/Desktop target eklenmeli mi karar ver**

Seçenekler:
a. Ayrı `screenshot-tests` modülü: JVM target + Roborazzi Desktop + commonMain composable'ları import
b. `composeApp`'a Desktop target ekle (riskli — iOS/Android build'i etkileyebilir)
c. Roborazzi'den vazgeç, mevcut `ScreenCaptureTest` (instrumented) + `pixel_diff.py` ile devam et

- [ ] **Step 3: Seçilen yolu uygula**

Eğer Roborazzi Desktop uygunsa:
```kotlin
// screenshot-tests/build.gradle.kts
plugins {
    id("techspiration.kmp.compose")
    id("io.github.takahirom.roborazzi")
}

kotlin {
    jvm("desktop")
    sourceSets {
        val desktopTest by getting {
            dependencies {
                implementation(project(":composeApp"))
                implementation("io.github.takahirom.roborazzi:roborazzi-compose-desktop:X.Y.Z")
            }
        }
    }
}
```

Eğer uygun değilse:
- Mevcut instrumented test yaklaşımını belgelendir
- `compare_screens.py` (Task 2) ile yetinilir

- [ ] **Step 4: Karar ve sonuçları belgelendir**

Bulgular `docs/guides/screenshot_testing.md` veya SKILL.md'ye ekle.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: investigate and integrate Roborazzi Desktop for CMP screenshot testing"
```

---

## Task 6: Token Drift Algılama

**Files:**
- Create: `ai/design/scripts/detect_token_drift.py`

- [ ] **Step 1: Script yaz**

```python
#!/usr/bin/env python3
"""Figma design context'teki token'ları AppColors/AppTypography ile karşılaştır.

Usage:
    python3 detect_token_drift.py --figma-node 54:1021
    python3 detect_token_drift.py --figma-code design_context.txt  (önceden kaydedilmiş)
"""
import argparse, re, os, sys

APPCOLORS_PATH = "composeApp/src/commonMain/kotlin/org/example/project/theme/AppColors.kt"

def parse_figma_colors(code: str) -> dict[str, str]:
    """Figma design context kodundan renkleri çıkar."""
    colors = {}
    # bg-[#HEX], text-[#HEX], border-[#HEX]
    for match in re.finditer(r'(?:bg|text|border|shadow)-\[#([0-9A-Fa-f]{6})\]', code):
        hex_color = f"#{match.group(1).upper()}"
        colors[hex_color] = colors.get(hex_color, 0) + 1  # type: ignore

    # rgba(R,G,B,A) formatı
    for match in re.finditer(r'rgba\((\d+),(\d+),(\d+),([\d.]+)\)', code):
        r, g, b, a = int(match.group(1)), int(match.group(2)), int(match.group(3)), float(match.group(4))
        hex_color = f"#{r:02X}{g:02X}{b:02X}"
        alpha = f" (alpha={a})" if a < 1 else ""
        key = f"{hex_color}{alpha}"
        colors[key] = colors.get(key, 0) + 1  # type: ignore

    # Tekil renkleri döndür
    return {k: v for k, v in sorted(colors.items(), key=lambda x: -x[1])}

def parse_app_colors(path: str) -> set[str]:
    """AppColors.kt'den tanımlı renkleri çıkar."""
    defined = set()
    with open(path) as f:
        content = f.read()

    # Color(0xFFHHHHHH) veya Color(0xAAHHHHHH)
    for match in re.finditer(r'Color\(0x([0-9A-Fa-f]{8})\)', content):
        full = match.group(1).upper()
        alpha = full[:2]
        rgb = full[2:]
        hex_color = f"#{rgb}"
        if alpha != "FF":
            alpha_float = int(alpha, 16) / 255
            defined.add(f"#{rgb} (alpha={alpha_float:.2f})")
        defined.add(hex_color)

    return defined

def detect_drift(figma_colors: dict, app_colors: set) -> tuple[list, list]:
    """Drift'leri tespit et."""
    matched = []
    missing = []

    for color, count in figma_colors.items():
        # Sadece base hex'i karşılaştır
        base_hex = color.split(' ')[0]
        if base_hex in app_colors or color in app_colors:
            matched.append((color, count))
        else:
            missing.append((color, count))

    return matched, missing

def main():
    parser = argparse.ArgumentParser(description='Detect Figma token drift')
    parser.add_argument('--figma-node', help='Figma node ID (MCP ile çek)')
    parser.add_argument('--figma-code', help='Önceden kaydedilmiş design context dosyası')
    parser.add_argument('--app-colors', default=APPCOLORS_PATH)
    args = parser.parse_args()

    # Figma renkleri
    if args.figma_code:
        with open(args.figma_code) as f:
            figma_code = f.read()
    elif args.figma_node:
        # MCP'den al (download_figma_assets.py'deki get_design_context fonksiyonunu kullan)
        print("MCP'den design context çekiliyor...")
        sys.path.insert(0, os.path.join(os.path.dirname(__file__)))
        from download_figma_assets import get_design_context
        figma_code = get_design_context(args.figma_node)
    else:
        parser.error("--figma-node veya --figma-code gerekli")
        return

    figma_colors = parse_figma_colors(figma_code)
    app_colors = parse_app_colors(args.app_colors)

    matched, missing = detect_drift(figma_colors, app_colors)

    print("=" * 60)
    print("  TOKEN DRIFT RAPORU")
    print("=" * 60)
    print(f"\n✅ Eşleşen ({len(matched)}):")
    for color, count in matched:
        print(f"   {color} (×{count})")

    if missing:
        print(f"\n❌ Eksik / Drift ({len(missing)}):")
        for color, count in missing:
            print(f"   {color} (×{count}) — AppColors'a eklenmeli veya mevcut token'a eşlenmeli")
    else:
        print(f"\n🎉 Tüm Figma renkleri AppColors'ta tanımlı!")

    print(f"\nToplam: {len(figma_colors)} Figma renk, {len(app_colors)} AppColors tanımı")

if __name__ == '__main__':
    main()
```

- [ ] **Step 2: Test et**

```bash
python3 ai/design/scripts/detect_token_drift.py --figma-node 54:1021
```

Expected: Renklerin çoğu eşleşmeli (proje zaten Figma token'larını kullanıyor).

- [ ] **Step 3: Commit**

```bash
git add ai/design/scripts/detect_token_drift.py
git commit -m "feat: add Figma token drift detection script"
```

---

## Son Adımlar

- [ ] **Skill dosyasını güncelle**: Yeni araçları `ai/design/skills/figma-to-compose/SKILL.md` ARAÇLAR tablosuna ekle
- [ ] **Makefile'ı güncelle**: Tüm yeni komutları ekle
- [ ] **validate-all'ı genişlet**: Token drift'i de dahil et

```makefile
validate-all:
	python3 ai/design/scripts/validate_svg_paths.py
	python3 ai/design/scripts/validate_design_tokens.py
	python3 ai/design/scripts/detect_token_drift.py --figma-code $(FIGMA_CODE)

convert-assets:
	python3 ai/design/scripts/svg_to_xml_vd.py figma-assets/ -o composeApp/src/commonMain/composeResources/drawable/

match-assets:
	python3 ai/design/scripts/match_existing_assets.py --svg-dir figma-assets/

download-assets:
	python3 ai/design/scripts/download_figma_assets.py --node-id $(NODE) -o figma-assets/

compare-screen:
	python3 ai/design/scripts/compare_screens.py --screen $(SCREEN) --figma-node $(NODE)
```
