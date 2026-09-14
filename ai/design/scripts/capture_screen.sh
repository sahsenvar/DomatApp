#!/bin/bash
# Capture a screenshot from the connected Android device
# Usage: ./scripts/capture_screen.sh [output_filename]
#
# Example:
#   ./scripts/capture_screen.sh screenshots/welcome.png

OUTPUT="${1:-screenshot_$(date +%Y%m%d_%H%M%S).png}"
OUTPUT_DIR=$(dirname "$OUTPUT")

# Create output directory if needed
mkdir -p "$OUTPUT_DIR"

# Capture screenshot from device
adb shell screencap -p /sdcard/screenshot_tmp.png
adb pull /sdcard/screenshot_tmp.png "$OUTPUT" > /dev/null 2>&1
adb shell rm /sdcard/screenshot_tmp.png

echo "Screenshot saved: $OUTPUT"
