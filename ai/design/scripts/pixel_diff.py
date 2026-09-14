#!/usr/bin/env python3
"""
pixel_diff.py — Compare two screenshots using pixel diff.

Usage:
    python3 pixel_diff.py <image1> <image2> [-o output.png] [-t threshold]

Arguments:
    image1       Path to first image (e.g. Figma screenshot)
    image2       Path to second image (e.g. Compose preview)
    -o, --output Path to save the diff image (optional)
    -t, --threshold  Max allowed diff percentage (default: 5.0)
"""

import sys
import argparse

# Check Pillow availability before importing
try:
    from PIL import Image
except ImportError:
    print("Error: Pillow is not installed.")
    print("Install it with: pip3 install Pillow")
    sys.exit(1)

CHANNEL_THRESHOLD = 30  # Per-channel threshold (out of 255)


def load_image(path: str) -> Image.Image:
    try:
        return Image.open(path).convert("RGB")
    except FileNotFoundError:
        print(f"Error: File not found: {path}")
        sys.exit(1)
    except Exception as e:
        print(f"Error loading image '{path}': {e}")
        sys.exit(1)


def resize_to_smaller(img1: Image.Image, img2: Image.Image):
    """Resize both images to the smaller of the two dimensions."""
    w = min(img1.width, img2.width)
    h = min(img1.height, img2.height)
    if img1.size != (w, h):
        img1 = img1.resize((w, h), Image.LANCZOS)
    if img2.size != (w, h):
        img2 = img2.resize((w, h), Image.LANCZOS)
    return img1, img2


def pixel_diff(img1: Image.Image, img2: Image.Image, output_path: str = None):
    """
    Compare two images pixel by pixel.

    A pixel differs if ANY channel difference exceeds CHANNEL_THRESHOLD.
    Returns (diff_pixels, total_pixels, diff_image_or_None).
    """
    pixels1 = img1.load()
    pixels2 = img2.load()
    width, height = img1.size
    total_pixels = width * height

    diff_image = Image.new("RGB", (width, height), (0, 0, 0)) if output_path else None
    diff_pixels_out = diff_image.load() if diff_image else None

    diff_count = 0

    for y in range(height):
        for x in range(width):
            r1, g1, b1 = pixels1[x, y]
            r2, g2, b2 = pixels2[x, y]

            channel_diff = (
                abs(r1 - r2) > CHANNEL_THRESHOLD
                or abs(g1 - g2) > CHANNEL_THRESHOLD
                or abs(b1 - b2) > CHANNEL_THRESHOLD
            )

            if channel_diff:
                diff_count += 1
                if diff_pixels_out:
                    diff_pixels_out[x, y] = (255, 0, 0)  # Red for differing pixels
            else:
                if diff_pixels_out:
                    # Dim the matching pixel for context
                    avg = (r1 + r2) // 4, (g1 + g2) // 4, (b1 + b2) // 4
                    diff_pixels_out[x, y] = avg

    return diff_count, total_pixels, diff_image


def main():
    parser = argparse.ArgumentParser(
        description="Compare two screenshots using pixel diff.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument("image1", help="Path to first image (Figma screenshot)")
    parser.add_argument("image2", help="Path to second image (Compose preview)")
    parser.add_argument(
        "-o", "--output",
        help="Path to save the diff image",
        default=None,
    )
    parser.add_argument(
        "-t", "--threshold",
        type=float,
        default=5.0,
        help="Max allowed diff percentage (default: 5.0)",
    )
    args = parser.parse_args()

    if args.threshold < 0 or args.threshold > 100:
        print("Error: --threshold must be between 0 and 100.")
        sys.exit(1)

    print(f"Image 1 : {args.image1}")
    print(f"Image 2 : {args.image2}")

    img1 = load_image(args.image1)
    img2 = load_image(args.image2)

    orig1 = img1.size
    orig2 = img2.size

    img1, img2 = resize_to_smaller(img1, img2)
    final_size = img1.size

    print()
    print(f"Original dimensions  : {orig1[0]}x{orig1[1]}  vs  {orig2[0]}x{orig2[1]}")
    print(f"Comparison dimensions: {final_size[0]}x{final_size[1]}")

    diff_count, total_pixels, diff_image = pixel_diff(img1, img2, args.output)

    diff_pct = (diff_count / total_pixels) * 100 if total_pixels > 0 else 0.0

    print()
    print(f"Total pixels  : {total_pixels:,}")
    print(f"Diff pixels   : {diff_count:,}")
    print(f"Diff %        : {diff_pct:.2f}%")
    print(f"Threshold     : {args.threshold:.2f}%")
    print(f"Channel thresh: {CHANNEL_THRESHOLD}/255 per channel")

    if diff_image and args.output:
        try:
            diff_image.save(args.output)
            print(f"Diff image    : {args.output}")
        except Exception as e:
            print(f"Warning: Could not save diff image: {e}")

    print()
    if diff_pct <= args.threshold:
        print("Result: PASS")
        sys.exit(0)
    else:
        print("Result: FAIL")
        sys.exit(1)


if __name__ == "__main__":
    main()
