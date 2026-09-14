#!/usr/bin/env python3
"""Validates Figma design tokens against project source files."""

import os
import re
import sys

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# ── helpers ──────────────────────────────────────────────────────────────────

def read_file(path: str) -> str:
    with open(path, encoding="utf-8") as f:
        return f.read()

def section(title: str) -> None:
    print()
    print("=" * 60)
    print(f"  {title}")
    print("=" * 60)

def ok(msg: str)   -> None: print(f"  [OK]   {msg}")
def fail(msg: str) -> None: print(f"  [FAIL] {msg}")
def warn(msg: str) -> None: print(f"  [WARN] {msg}")

# ── 1. Colors ─────────────────────────────────────────────────────────────────

FIGMA_COLORS = {
    "Green500":   "0xFF13EC49",
    "Slate900":   "0xFF0F172A",
    "Slate800":   "0xFF1E293B",
    "Slate700":   "0xFF334155",
    "Slate600":   "0xFF475569",
    "Slate500":   "0xFF64748B",
    "Slate400":   "0xFF94A3B8",
    "Slate200":   "0xFFE2E8F0",
    "Slate100":   "0xFFF1F5F9",
    "Slate50":    "0xFFF8FAFC",
    "Background": "0xFFF6F8F6",
}

def validate_colors() -> tuple[int, int]:
    section("1. COLORS  —  AppColors.kt")
    path = os.path.join(
        PROJECT_ROOT,
        "composeApp/src/commonMain/kotlin/org/example/project/theme/AppColors.kt",
    )
    passed = failed = 0
    try:
        content = read_file(path)
    except FileNotFoundError:
        fail(f"File not found: {path}")
        return 0, len(FIGMA_COLORS)

    for name, hex_val in FIGMA_COLORS.items():
        # Match:  {Name} = Color({hex})
        pattern = rf"\b{re.escape(name)}\s*=\s*Color\s*\(\s*{re.escape(hex_val)}\s*\)"
        if re.search(pattern, content):
            ok(f"{name} = Color({hex_val})")
            passed += 1
        else:
            # Check whether name exists at all (wrong value)
            name_pattern = rf"\b{re.escape(name)}\s*=\s*Color\s*\(\s*(0x[0-9A-Fa-f]+)\s*\)"
            m = re.search(name_pattern, content)
            if m:
                fail(f"{name}: expected Color({hex_val}), found Color({m.group(1)})")
            else:
                fail(f"{name}: not found in file")
            failed += 1

    return passed, failed

# ── 2. Typography ─────────────────────────────────────────────────────────────

FIGMA_TYPOGRAPHY = {
    "displayLarge":  {"fontSize": "36.sp", "fontWeight": "FontWeight.ExtraBold"},
    "displayMedium": {"fontSize": "30.sp", "fontWeight": "FontWeight.Bold"},
    "headlineLarge": {"fontSize": "24.sp", "fontWeight": "FontWeight.Bold"},
    "headlineSmall": {"fontSize": "18.sp", "fontWeight": "FontWeight.Bold"},
    "titleLarge":    {"fontSize": "18.sp", "fontWeight": "FontWeight.Bold"},
    "titleMedium":   {"fontSize": "16.sp", "fontWeight": "FontWeight.Bold"},
    "bodyLarge":     {"fontSize": "18.sp", "fontWeight": "FontWeight.Medium"},
    "bodyMedium":    {"fontSize": "14.sp", "fontWeight": "FontWeight.Normal"},
    "labelLarge":    {"fontSize": "12.sp", "fontWeight": "FontWeight.SemiBold"},
}

def _extract_slot(content: str, slot: str) -> str | None:
    """Return the TextStyle(...) block for a given Typography slot name."""
    # Find   slotName = TextStyle(
    start_pattern = rf"\b{re.escape(slot)}\s*=\s*TextStyle\s*\("
    m = re.search(start_pattern, content)
    if not m:
        return None
    # Walk forward counting parens to find matching close
    depth = 0
    i = m.end() - 1  # position of opening '('
    for j in range(i, len(content)):
        if content[j] == "(":
            depth += 1
        elif content[j] == ")":
            depth -= 1
            if depth == 0:
                return content[i : j + 1]
    return None

def validate_typography() -> tuple[int, int]:
    section("2. TYPOGRAPHY  —  AppTypography.kt")
    path = os.path.join(
        PROJECT_ROOT,
        "composeApp/src/commonMain/kotlin/org/example/project/theme/AppTypography.kt",
    )
    passed = failed = 0
    try:
        content = read_file(path)
    except FileNotFoundError:
        fail(f"File not found: {path}")
        return 0, len(FIGMA_TYPOGRAPHY)

    for slot, expected in FIGMA_TYPOGRAPHY.items():
        block = _extract_slot(content, slot)
        if block is None:
            fail(f"{slot}: slot not found in file")
            failed += 1
            continue

        exp_size   = expected["fontSize"]
        exp_weight = expected["fontWeight"]

        # Check fontSize
        size_m = re.search(r"fontSize\s*=\s*([0-9.]+\.sp)", block)
        actual_size = size_m.group(1) if size_m else "<missing>"

        # Check fontWeight
        weight_m = re.search(r"fontWeight\s*=\s*(FontWeight\.\w+)", block)
        actual_weight = weight_m.group(1) if weight_m else "<missing>"

        size_ok   = actual_size   == exp_size
        weight_ok = actual_weight == exp_weight

        if size_ok and weight_ok:
            ok(f"{slot}: {exp_size} {exp_weight}")
            passed += 1
        else:
            parts = []
            if not size_ok:
                parts.append(f"fontSize expected={exp_size} got={actual_size}")
            if not weight_ok:
                parts.append(f"fontWeight expected={exp_weight} got={actual_weight}")
            fail(f"{slot}: {', '.join(parts)}")
            failed += 1

    return passed, failed

# ── 3. Hardcoded Color scan ───────────────────────────────────────────────────

SCAN_DIRS = [
    "composeApp/src/commonMain/kotlin/org/example/project/components",
    "composeApp/src/commonMain/kotlin/org/example/project/onboarding",
]

# Matches Color(0x...) — literal hex literal, not a token reference
HARDCODED_COLOR_RE = re.compile(r"Color\s*\(\s*0x[0-9A-Fa-f]+\s*\)")

def validate_hardcoded_colors() -> tuple[int, int]:
    section("3. HARDCODED COLORS SCAN")
    clean = flagged = 0

    for rel_dir in SCAN_DIRS:
        abs_dir = os.path.join(PROJECT_ROOT, rel_dir)
        print(f"\n  Scanning: {rel_dir}")
        if not os.path.isdir(abs_dir):
            warn(f"Directory not found: {abs_dir}")
            continue

        kt_files = sorted(
            f for f in os.listdir(abs_dir) if f.endswith(".kt")
        )
        if not kt_files:
            warn("No .kt files found.")
            continue

        for fname in kt_files:
            fpath = os.path.join(abs_dir, fname)
            content = read_file(fpath)
            matches = HARDCODED_COLOR_RE.findall(content)
            if matches:
                fail(f"{fname}: {len(matches)} hardcoded color(s) → {', '.join(matches)}")
                flagged += 1
            else:
                ok(f"{fname}: clean")
                clean += 1

    return clean, flagged

# ── main ──────────────────────────────────────────────────────────────────────

def main() -> None:
    print()
    print("╔══════════════════════════════════════════════════════════╗")
    print("║        Figma Design Token Validator                      ║")
    print("╚══════════════════════════════════════════════════════════╝")

    c_pass, c_fail = validate_colors()
    t_pass, t_fail = validate_typography()
    s_clean, s_flag = validate_hardcoded_colors()

    # ── Summary ───────────────────────────────────────────────────────────────
    section("SUMMARY")
    total_pass = c_pass + t_pass
    total_fail = c_fail + t_fail

    print(f"  Colors     : {c_pass} passed, {c_fail} failed")
    print(f"  Typography : {t_pass} passed, {t_fail} failed")
    print(f"  Hardcoded  : {s_clean} clean files, {s_flag} flagged files")
    print()
    print(f"  Design tokens  : {total_pass}/{total_pass + total_fail} OK")
    print(f"  Hardcoded scan : {'CLEAN' if s_flag == 0 else f'{s_flag} FILE(S) NEED ATTENTION'}")
    print()

    if total_fail > 0 or s_flag > 0:
        print("  Result: VALIDATION FAILED")
        sys.exit(1)
    else:
        print("  Result: ALL CHECKS PASSED")
        sys.exit(0)

if __name__ == "__main__":
    main()
