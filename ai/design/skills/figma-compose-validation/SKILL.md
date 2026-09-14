---
name: figma-compose-validation
description: >
  Layered validation system for Figma-to-Compose implementations.
  Validates asset extraction, SVG path data, design token matching,
  and visual screenshot comparison. Use after implementing any Figma
  design or when the user asks to validate/verify a design implementation.
---

# Figma → Compose Validation

4 katmanlı doğrulama sistemi. Her katman bağımsız çalışabilir.

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
2. K5-başlangıç: Figma screenshot'ı referans olarak kaydet

## Sonunda Çalıştır (Kod tamamlandıktan sonra)
1. K2: SVG path'leri doğrula
2. K3: Token'ları doğrula
3. K4-son: Pixel diff + Claude karşılaştırma
