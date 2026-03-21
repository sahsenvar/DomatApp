#!/usr/bin/env python3
"""
Validates Android Vector Drawable (ic_*.xml) files for common SVG conversion errors.
"""

import re
import sys
from pathlib import Path

DRAWABLE_DIR = Path(__file__).parent.parent / "composeApp/src/commonMain/composeResources/drawable"

# SVG element names that sometimes end up as pathData due to bad conversion
INVALID_PATH_NAMES = {"Icon", "Vector", "Margin", "Container", "SVG", "icon", "vector", "svg"}

# Valid SVG path command characters (e/E are for scientific notation in numbers, not commands)
VALID_COMMANDS = set("MmLlHhVvCcSsQqTtAaZz")

# Regex to extract pathData attribute values
RE_PATH_DATA = re.compile(r'android:pathData\s*=\s*"([^"]*)"')

# Regex to find <path ...> elements (including multi-line)
RE_PATH_ELEMENT = re.compile(r'<path\b([^/]*(?:/(?!>)[^/]*)*)/>', re.DOTALL)

# Regex to extract fillColor or strokeColor from a path element
RE_FILL_COLOR = re.compile(r'android:fillColor\s*=\s*"[^"]*"')
RE_STROKE_COLOR = re.compile(r'android:strokeColor\s*=\s*"[^"]*"')


def validate_path_data(path_data: str) -> list[str]:
    """Returns a list of error strings for the given pathData value."""
    errors = []

    # Check for captured SVG element names
    stripped = path_data.strip()
    if stripped in INVALID_PATH_NAMES:
        errors.append(f"pathData looks like an SVG element name: '{stripped}'")
        return errors  # No point checking further

    # Check minimum length
    if len(stripped) < 3:
        errors.append(f"pathData is too short ({len(stripped)} chars): '{stripped}'")
        return errors

    # Check for invalid command characters
    # Strip out: digits, spaces, commas, dots, +, -, e, E (scientific notation)
    # Remaining chars should all be valid SVG commands
    remaining = re.sub(r'[\d\s,.\+\-eE]', '', stripped)
    invalid_chars = set(remaining) - VALID_COMMANDS
    if invalid_chars:
        errors.append(
            f"pathData contains invalid command characters {sorted(invalid_chars)}: '{stripped[:60]}...'"
            if len(stripped) > 60
            else f"pathData contains invalid command characters {sorted(invalid_chars)}: '{stripped}'"
        )

    return errors


def validate_file(xml_path: Path) -> tuple[bool, list[str]]:
    """
    Validates a single XML Vector Drawable file.
    Returns (passed: bool, messages: list[str]).
    """
    content = xml_path.read_text(encoding="utf-8")
    messages = []
    passed = True

    # --- Validate each pathData value ---
    for match in RE_PATH_DATA.finditer(content):
        path_data = match.group(1)
        errs = validate_path_data(path_data)
        for err in errs:
            messages.append(f"  [pathData] {err}")
            passed = False

    # --- Validate each <path> has fillColor or strokeColor ---
    for i, path_match in enumerate(RE_PATH_ELEMENT.finditer(content), start=1):
        element_body = path_match.group(0)
        has_fill = bool(RE_FILL_COLOR.search(element_body))
        has_stroke = bool(RE_STROKE_COLOR.search(element_body))
        if not has_fill and not has_stroke:
            # Extract pathData snippet for context
            pd_match = RE_PATH_DATA.search(element_body)
            snippet = pd_match.group(1)[:40] + "..." if pd_match and len(pd_match.group(1)) > 40 else (pd_match.group(1) if pd_match else "<no pathData>")
            messages.append(f"  [color] <path> #{i} has neither fillColor nor strokeColor (pathData: '{snippet}')")
            passed = False

    return passed, messages


def main() -> int:
    files = sorted(DRAWABLE_DIR.glob("ic_*.xml"))

    if not files:
        print(f"No ic_*.xml files found in: {DRAWABLE_DIR}")
        return 1

    total = len(files)
    failures = 0

    for xml_path in files:
        passed, messages = validate_file(xml_path)
        status = "OK  " if passed else "FAIL"
        print(f"[{status}] {xml_path.name}")
        for msg in messages:
            print(msg)
        if not passed:
            failures += 1

    print()
    print(f"Summary: {total - failures}/{total} files passed, {failures} failed.")

    return 0 if failures == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
