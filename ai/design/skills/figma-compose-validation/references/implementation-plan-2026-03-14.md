# Figma → Compose Katmanlı Doğrulama Sistemi

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Figma tasarımından Compose koduna giden her adımda otomatik doğrulama katmanları oluşturmak — asset sınıflandırma, dönüşüm validasyonu, token eşleştirme, screenshot karşılaştırma.

**Architecture:** Paparazzi ile screenshot'ları PNG olarak yakala, Python script ile Figma screenshot vs Compose preview pixel diff hesapla, Claude görsel review ile doğrula.

**Tech Stack:** Paparazzi (Square), Python PIL/pixelmatch, Figma MCP

---

## Dosya Yapısı

```
composeApp/
├── build.gradle.kts                   ← MODIFY: Paparazzi plugin ekle
└── src/androidUnitTest/kotlin/.../    ← YENİ: Paparazzi snapshot testleri

scripts/
├── validate_svg_paths.py              ← YENİ: SVG path data doğrulayıcı
├── validate_design_tokens.py          ← YENİ: Figma↔Kod token karşılaştırıcı
├── pixel_diff.py                      ← YENİ: Screenshot pixel diff karşılaştırıcı
└── extract_assets.py                  ← MEVCUT: Figma asset extractor

ai/design/skills/figma-compose-validation/
└── SKILL.md                           ← YENİ: Doğrulama workflow skill'i
```

---

## Chunk 1: Screenshot Capture (Paparazzi)

### Task 4: Paparazzi Setup

**Files:**
- Modify: `gradle/libs.versions.toml` — Paparazzi dependency
- Modify: `composeApp/build.gradle.kts` — Paparazzi plugin
- Create: `composeApp/src/test/kotlin/org/example/project/screenshots/ScreenshotTests.kt`

- [ ] **Step 1: libs.versions.toml'a Paparazzi ekle**

```toml
[versions]
paparazzi = "1.3.5"

[plugins]
paparazzi = { id = "app.cash.paparazzi", version.ref = "paparazzi" }
```

- [ ] **Step 2: composeApp/build.gradle.kts'ye Paparazzi plugin ekle**

NOT: Paparazzi KMP application modülünde sorun çıkarabilir. Alternatif olarak ayrı bir `screenshot-test` modülü oluşturmak gerekebilir. Önce direkt dene, sorun çıkarsa modül ayır.

```kotlin
plugins {
    // mevcut pluginler...
    alias(libs.plugins.paparazzi)
}
```

- [ ] **Step 3: Screenshot test dosyası oluştur**

```kotlin
package org.example.project.screenshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class OnboardingScreenshots {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material3.Light.NoActionBar",
    )

    // Her @Preview wrapper'ı burada render et
    // Paparazzi generate edilen preview'ları kullanır
}
```

- [ ] **Step 4: Paparazzi baseline kaydet**

Run: `./gradlew :composeApp:recordPaparazziDebug`
Expected: `composeApp/src/test/snapshots/` altına PNG dosyaları oluşur

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml composeApp/build.gradle.kts composeApp/src/test/
git commit -m "feat: add Paparazzi screenshot testing setup"
```

**NOT:** Paparazzi KMP'de sorun çıkarırsa, fallback plan:
- Ayrı bir `screenshot-test` Android library modülü oluştur
- composeApp'e dependency ekle
- Paparazzi'yi o modülde çalıştır

---

## Chunk 3: Doğrulama Scriptleri

### Task 5: SVG Path Validator

**Files:**
- Create: `scripts/validate_svg_paths.py`

- [ ] **Step 1: Script oluştur**

```python
#!/usr/bin/env python3
"""Validate XML Vector Drawable path data for common SVG conversion errors."""

import re
import sys
import os
from pathlib import Path

VALID_COMMANDS = set("MmLlHhVvCcSsQqTtAaZz")
DRAWABLE_DIR = "composeApp/src/commonMain/composeResources/drawable"


def validate_path_data(path_data: str, filename: str) -> list[str]:
    """Check path data for invalid commands."""
    errors = []

    # Check for common regex bugs (id captured instead of d)
    if path_data in ("Icon", "Vector", "Margin", "Container", "SVG"):
        errors.append(f"  pathData is '{path_data}' — SVG element name captured instead of path data")
        return errors

    # Check for too-short path data
    if len(path_data) < 3:
        errors.append(f"  pathData too short: '{path_data}'")
        return errors

    # Check each command character
    tokens = re.findall(r'[A-Za-z]', path_data)
    for token in tokens:
        if token not in VALID_COMMANDS and token not in "eE":  # e/E is part of scientific notation
            errors.append(f"  Invalid path command: '{token}' in pathData")

    return errors


def validate_file(filepath: Path) -> list[str]:
    """Validate a single XML Vector Drawable file."""
    content = filepath.read_text()
    errors = []

    # Extract all pathData values
    path_datas = re.findall(r'android:pathData="([^"]*)"', content)

    if not path_datas:
        errors.append(f"  No pathData found in file")
        return errors

    for pd in path_datas:
        errors.extend(validate_path_data(pd, filepath.name))

    # Check for missing fillColor/strokeColor
    paths = re.findall(r'<path[^>]+/>', content)
    for path in paths:
        has_fill = 'android:fillColor' in path
        has_stroke = 'android:strokeColor' in path
        if not has_fill and not has_stroke:
            errors.append(f"  Path missing both fillColor and strokeColor")

    return errors


def main():
    drawable_dir = Path(DRAWABLE_DIR)
    if not drawable_dir.exists():
        print(f"Directory not found: {DRAWABLE_DIR}")
        sys.exit(1)

    xml_files = sorted(drawable_dir.glob("ic_*.xml"))
    total_errors = 0

    print(f"Validating {len(xml_files)} vector drawable files...\n")

    for f in xml_files:
        errors = validate_file(f)
        if errors:
            print(f"FAIL: {f.name}")
            for e in errors:
                print(e)
            total_errors += len(errors)
        else:
            print(f"  OK: {f.name}")

    print(f"\n{'PASS' if total_errors == 0 else 'FAIL'}: {len(xml_files)} files, {total_errors} errors")
    sys.exit(1 if total_errors > 0 else 0)


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Çalıştır ve doğrula**

Run: `python3 scripts/validate_svg_paths.py`
Expected: Tüm ic_*.xml dosyaları PASS

- [ ] **Step 3: Commit**

```bash
git add scripts/validate_svg_paths.py
git commit -m "feat: add SVG path data validator script"
```

---

### Task 6: Design Token Validator

**Files:**
- Create: `scripts/validate_design_tokens.py`

- [ ] **Step 1: Script oluştur**

```python
#!/usr/bin/env python3
"""Compare Figma design tokens with project AppColors/AppTypography values."""

import re
import sys
from pathlib import Path

COLORS_FILE = "composeApp/src/commonMain/kotlin/org/example/project/theme/AppColors.kt"
TYPO_FILE = "composeApp/src/commonMain/kotlin/org/example/project/theme/AppTypography.kt"

# Figma token referans değerleri (design_system.md'den)
FIGMA_COLORS = {
    "Green500": "0xFF13EC49",
    "Slate900": "0xFF0F172A",
    "Slate800": "0xFF1E293B",
    "Slate700": "0xFF334155",
    "Slate600": "0xFF475569",
    "Slate500": "0xFF64748B",
    "Slate400": "0xFF94A3B8",
    "Slate200": "0xFFE2E8F0",
    "Slate100": "0xFFF1F5F9",
    "Slate50": "0xFFF8FAFC",
    "Background": "0xFFF6F8F6",
}

FIGMA_TYPOGRAPHY = {
    "displayLarge": {"size": "36", "weight": "ExtraBold"},
    "displayMedium": {"size": "30", "weight": "Bold"},
    "headlineLarge": {"size": "24", "weight": "Bold"},
    "headlineSmall": {"size": "18", "weight": "Bold"},
    "titleLarge": {"size": "18", "weight": "Bold"},
    "titleMedium": {"size": "16", "weight": "Bold"},
    "bodyLarge": {"size": "18", "weight": "Medium"},
    "bodyMedium": {"size": "14", "weight": "Normal"},
    "labelLarge": {"size": "12", "weight": "SemiBold"},
}


def validate_colors():
    """Check AppColors.kt against Figma reference."""
    content = Path(COLORS_FILE).read_text()
    errors = []

    for name, expected_hex in FIGMA_COLORS.items():
        pattern = rf'{name}\s*=\s*Color\(({expected_hex})\)'
        if not re.search(pattern, content, re.IGNORECASE):
            # Check if variable exists with different value
            var_pattern = rf'{name}\s*=\s*Color\((0x[0-9A-Fa-f]+)\)'
            match = re.search(var_pattern, content)
            if match:
                actual = match.group(1)
                errors.append(f"  COLOR MISMATCH: {name} = {actual} (expected {expected_hex})")
            else:
                errors.append(f"  COLOR MISSING: {name} not found in AppColors.kt")

    return errors


def validate_typography():
    """Check AppTypography.kt against Figma reference."""
    content = Path(TYPO_FILE).read_text()
    errors = []

    for slot, props in FIGMA_TYPOGRAPHY.items():
        # Find the slot block
        slot_pattern = rf'{slot}\s*=\s*TextStyle\(([\s\S]*?)\),'
        match = re.search(slot_pattern, content)
        if not match:
            errors.append(f"  TYPO MISSING: {slot} not found")
            continue

        block = match.group(1)

        # Check fontSize
        size_match = re.search(rf'fontSize\s*=\s*({props["size"]})\.sp', block)
        if not size_match:
            actual_size = re.search(r'fontSize\s*=\s*(\d+(?:\.\d+)?)\.sp', block)
            actual = actual_size.group(1) if actual_size else "?"
            errors.append(f"  TYPO SIZE: {slot} fontSize={actual}sp (expected {props['size']}sp)")

        # Check fontWeight
        weight_match = re.search(rf'fontWeight\s*=\s*FontWeight\.{props["weight"]}', block)
        if not weight_match:
            actual_weight = re.search(r'fontWeight\s*=\s*FontWeight\.(\w+)', block)
            actual = actual_weight.group(1) if actual_weight else "?"
            errors.append(f"  TYPO WEIGHT: {slot} weight={actual} (expected {props['weight']})")

    return errors


def check_hardcoded_colors():
    """Scan composable files for hardcoded Color() values that should use AppColors."""
    errors = []
    pattern = re.compile(r'Color\(0x[0-9A-Fa-f]+\)')

    scan_dirs = [
        "composeApp/src/commonMain/kotlin/org/example/project/components/",
        "composeApp/src/commonMain/kotlin/org/example/project/onboarding/",
    ]

    for dir_path in scan_dirs:
        p = Path(dir_path)
        if not p.exists():
            continue
        for f in p.glob("*.kt"):
            content = f.read_text()
            matches = pattern.findall(content)
            if matches:
                errors.append(f"  HARDCODED: {f.name} has {len(matches)} hardcoded Color() values: {matches[:3]}")

    return errors


def main():
    print("Validating design tokens...\n")

    print("=== Colors ===")
    color_errors = validate_colors()
    for e in color_errors:
        print(e)
    if not color_errors:
        print("  All colors match Figma tokens")

    print("\n=== Typography ===")
    typo_errors = validate_typography()
    for e in typo_errors:
        print(e)
    if not typo_errors:
        print("  All typography matches Figma tokens")

    print("\n=== Hardcoded Colors ===")
    hardcoded_errors = check_hardcoded_colors()
    for e in hardcoded_errors:
        print(e)
    if not hardcoded_errors:
        print("  No hardcoded colors found")

    total = len(color_errors) + len(typo_errors) + len(hardcoded_errors)
    print(f"\n{'PASS' if total == 0 else 'FAIL'}: {total} issues found")
    sys.exit(1 if total > 0 else 0)


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Çalıştır**

Run: `python3 scripts/validate_design_tokens.py`
Expected: Tüm token'lar eşleşmeli

- [ ] **Step 3: Commit**

```bash
git add scripts/validate_design_tokens.py
git commit -m "feat: add design token validator script"
```

---

### Task 7: Pixel Diff Comparator

**Files:**
- Create: `scripts/pixel_diff.py`

- [ ] **Step 1: Script oluştur**

```python
#!/usr/bin/env python3
"""Compare Figma screenshot with Compose preview screenshot using pixel diff."""

import argparse
import sys
from pathlib import Path

try:
    from PIL import Image, ImageChops, ImageDraw
except ImportError:
    print("Install Pillow: pip3 install Pillow")
    sys.exit(1)


def pixel_diff(img1_path: str, img2_path: str, output_path: str = None, threshold: float = 5.0) -> dict:
    """Compare two images and return diff statistics."""
    img1 = Image.open(img1_path).convert("RGB")
    img2 = Image.open(img2_path).convert("RGB")

    # Resize to match (use the smaller dimensions)
    w = min(img1.width, img2.width)
    h = min(img1.height, img2.height)
    img1 = img1.resize((w, h), Image.LANCZOS)
    img2 = img2.resize((w, h), Image.LANCZOS)

    # Calculate diff
    diff = ImageChops.difference(img1, img2)
    pixels = list(diff.getdata())
    total = len(pixels)

    # Count differing pixels (threshold per channel: 30/255)
    channel_threshold = 30
    diff_count = sum(
        1 for r, g, b in pixels
        if r > channel_threshold or g > channel_threshold or b > channel_threshold
    )

    diff_percent = (diff_count / total) * 100

    result = {
        "dimensions": f"{w}x{h}",
        "total_pixels": total,
        "diff_pixels": diff_count,
        "diff_percent": round(diff_percent, 2),
        "passed": diff_percent <= threshold,
        "threshold": threshold,
    }

    # Generate diff image if output path provided
    if output_path:
        # Create highlighted diff image
        highlight = img2.copy()
        draw = ImageDraw.Draw(highlight)
        for y in range(h):
            for x in range(w):
                r, g, b = diff.getpixel((x, y))
                if r > channel_threshold or g > channel_threshold or b > channel_threshold:
                    draw.point((x, y), fill=(255, 0, 0))
        highlight.save(output_path)
        result["diff_image"] = output_path

    return result


def main():
    parser = argparse.ArgumentParser(description="Pixel diff between two screenshots")
    parser.add_argument("image1", help="First image (Figma screenshot)")
    parser.add_argument("image2", help="Second image (Compose preview)")
    parser.add_argument("--output", "-o", help="Output diff image path")
    parser.add_argument("--threshold", "-t", type=float, default=5.0, help="Max diff percentage (default: 5%%)")
    args = parser.parse_args()

    result = pixel_diff(args.image1, args.image2, args.output, args.threshold)

    print(f"Dimensions: {result['dimensions']}")
    print(f"Total pixels: {result['total_pixels']}")
    print(f"Different pixels: {result['diff_pixels']}")
    print(f"Diff: {result['diff_percent']}% (threshold: {result['threshold']}%)")

    if result.get("diff_image"):
        print(f"Diff image: {result['diff_image']}")

    status = "PASS" if result["passed"] else "FAIL"
    print(f"\n{status}")
    sys.exit(0 if result["passed"] else 1)


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Pillow kur**

Run: `pip3 install Pillow`

- [ ] **Step 3: Test et (iki farklı PNG ile)**

Run: `python3 scripts/pixel_diff.py figma-screenshot.png compose-preview.png -o diff-output.png`
Expected: Diff yüzdesi ve PASS/FAIL çıktısı

- [ ] **Step 4: Commit**

```bash
git add scripts/pixel_diff.py
git commit -m "feat: add pixel diff comparison script"
```

---

## Chunk 4: Doğrulama Workflow Skill

### Task 8: Validation Skill Oluştur

**Files:**
- Create: `ai/design/skills/figma-compose-validation/SKILL.md`

- [ ] **Step 1: Skill dosyasını oluştur**

```markdown
---
name: figma-compose-validation
description: >
  Layered validation system for Figma-to-Compose implementations.
  Validates asset extraction, SVG path data, design token matching,
  and visual screenshot comparison. Use after implementing any Figma
  design or when the user asks to validate/verify a design implementation.
---

# Figma → Compose Validation

5 katmanlı doğrulama sistemi. Her katman bağımsız çalışabilir.

## Quick Run (Tüm katmanlar)

```bash
# K1+K2: Asset validation
python3 scripts/validate_svg_paths.py

# K3: Token validation
python3 scripts/validate_design_tokens.py

# K4: Screenshot comparison (manuel)
# 1. Figma'dan: get_screenshot(nodeId) → figma-ref.png
# 2. Cihazdan: ./scripts/capture_screen.sh → compose.png
# 3. Karşılaştır: python3 scripts/pixel_diff.py figma-ref.png compose.png -o diff.png
# 4. Claude: İki görseli oku ve karşılaştır
```

## Katman Detayları

### K1: Asset Sınıflandırma (DETECT)
**Ne yapar:** Figma node'larını IMAGE/VECTOR/SHAPE olarak sınıflar
**Nasıl:** figma-image skill'ini kullan (DETECT adımı)
**Doğrulama:** Fotoğraflar PNG mi? Vektörler SVG mi? Shape'ler kod mu?

### K2: Asset Dönüşüm (EXTRACT + CONVERT)
**Ne yapar:** SVG → XML Vector Drawable dönüşüm doğruluğu
**Nasıl:** `python3 scripts/validate_svg_paths.py`
**Doğrulama:** Path data geçerli mi? fillColor var mı? Bozuk veri yok mu?

### K3: Design Token Eşleştirme
**Ne yapar:** Figma renk/typography vs AppColors/AppTypography
**Nasıl:** `python3 scripts/validate_design_tokens.py`
**Doğrulama:** Renkler eşleşiyor mu? Font size/weight doğru mu? Hardcoded değer var mı?

### K4: Görsel Karşılaştırma
**Ne yapar:** Figma screenshot vs Compose preview pixel diff + Claude review
**Nasıl:**
1. `get_screenshot(nodeId)` → Figma referans PNG
2. `capture_screen.sh` → ADB ile cihazdan Compose PNG
3. `python3 scripts/pixel_diff.py figma.png compose.png -o diff.png -t 5`
4. Claude: İki görseli yan yana değerlendir

**Doğrulama:** Diff ≤ %5 mi? Claude onay veriyor mu?

## Başlangıçta Çalıştır (Figma → Kod başlamadan)
1. K1: Tüm elementleri sınıfla
2. K4-başlangıç: Figma screenshot'ı referans olarak kaydet

## Sonunda Çalıştır (Kod tamamlandıktan sonra)
1. K2: SVG path'leri doğrula
2. K3: Token'ları doğrula
3. K4-son: Pixel diff + Claude karşılaştırma
```

- [ ] **Step 2: Commit**

```bash
git add ai/design/skills/figma-compose-validation/SKILL.md
git commit -m "feat: add figma-compose-validation skill"
```

---

### Task 9: Makefile'a Validation Komutları Ekle

**Files:**
- Modify: `Makefile` — Validation targets ekle

- [ ] **Step 1: Makefile'a validation target'ları ekle**

```makefile
## Figma Validation
validate-svg:          ## Validate SVG path data in vector drawables
	python3 scripts/validate_svg_paths.py

validate-tokens:       ## Validate design tokens against Figma reference
	python3 scripts/validate_design_tokens.py

validate-all:          ## Run all validation checks
	@echo "=== SVG Path Validation ===" && python3 scripts/validate_svg_paths.py
	@echo "\n=== Design Token Validation ===" && python3 scripts/validate_design_tokens.py
	@echo "\n=== All validations complete ==="
```

- [ ] **Step 2: Test et**

Run: `make validate-svg`
Expected: SVG validation output

- [ ] **Step 3: Commit**

```bash
git add Makefile
git commit -m "feat: add validation targets to Makefile"
```

---

## Execution Notes

**Paparazzi sorun çıkarırsa:**
- KMP + `com.android.application` ile uyumsuzluk olabilir
- Alternatif: Ayrı `screenshot-test` Android library modülü oluştur
- Veya: `adb shell screencap` + crop ile fallback

**Sıralama:**
- Chunk 1 (Paparazzi) → Chunk 2 (Scriptler) → Chunk 3 (Skill)
- Chunk 1 en riskli (Paparazzi KMP uyumu), sorun çıkarırsa `adb shell screencap` fallback kullan

**Bağımlılıklar:**
- Chunk 1 bağımsız
- Chunk 2 bağımsız
- Chunk 3 hepsine bağımlı (orchestration)
