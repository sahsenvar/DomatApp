# Prompt Templates — Figma Image Handling

## Table of Contents
1. [Claude.ai Artifact Prompts (Screenshot-Based)](#1-claudeai-artifact-prompts)
2. [Claude Code / Cursor Prompts (MCP-Based)](#2-claude-code--cursor-prompts)
3. [REST API Script Prompts](#3-rest-api-script-prompts)
4. [Platform-Specific Prompts](#4-platform-specific-prompts)
5. [SVG-First Prompts](#5-svg-first-prompts)
6. [Reusable Components](#6-reusable-components)

---

## 1. Claude.ai Artifact Prompts

### Template A: Full Mobile Screen Implementation

Use when pasting a Figma screenshot into Claude.ai chat.

```markdown
Bu Figma mobil tasarımını React artifact olarak implemente et.

## Element Analizi
Her görseli tip olarak belirtiyorum:

- [Hero banner alanı]: FOTOĞRAF → https://picsum.photos/375/200 kullan
- [Profil resmi]: FOTOĞRAF → SVG initials avatar (CSS harf + renkli daire)
- [Geri butonu]: İKON → Lucide ArrowLeft
- [Kalp ikonu]: İKON → Lucide Heart
- [Paylaş ikonu]: İKON → Lucide Share2
- [Logo]: VEKTÖR → Inline <svg> olarak benzerini çiz
- [Arka plan]: SHAPE → CSS gradient
- [Card gölge]: EFEKT → CSS box-shadow

## Image Stratejisi (ZORUNLU)
1. Fotoğraf alanları: https://picsum.photos/{w}/{h} + onError gradient fallback
2. Avatar: SVG initials-based (harf + renkli daire, network bağımsız)
3. İkonlar: SADECE lucide-react import et
4. Logo/vektör: Inline <svg> path olarak çiz
5. Dekoratif: CSS gradient + pattern
6. Her <img> için onError handler + aspect-ratio CSS
7. Skeleton loading state (animated pulse)

## Teknik Kurallar
- Tailwind core utilities only
- Dış kütüphane: sadece lucide-react, recharts izinli
- CSS variables ile renk sistemi (--primary, --secondary, vb.)
- Mobile-first responsive
- localStorage/sessionStorage KULLANMA (desteklenmiyor)
```

### Template B: Quick Component

Use for single components (button, card, nav bar, etc.)

```markdown
Bu Figma component'ini React artifact olarak yap.

[Screenshot]

Image kuralları:
- İkon varsa → Lucide React
- Fotoğraf varsa → picsum.photos + onError fallback
- Shape/gradient → CSS ile yap
- Tailwind only, dış font yok
```

### Template C: SVG-Only Output

Use when you want everything as inline SVG, no external dependencies.

```markdown
Bu tasarımı implemente et. TÜM görselleri inline SVG olarak oluştur:

1. Fotoğraf alanları → SVG gradient mesh + geometric abstract composition
2. Avatar → SVG daire + harf initials
3. İkonlar → Custom SVG path (Lucide yerine, kendi çiz)
4. Logo → SVG path
5. Dekoratif → SVG pattern + filter (feTurbulence, feGaussianBlur)
6. Card arka plan → SVG rect + gradient

Hiçbir <img> tag'i veya dış URL olmasın.
Hiçbir import olmasın (lucide-react dahil).
%100 self-contained inline SVG + CSS.
```

### Template D: Design System Component Library

```markdown
Bu Figma tasarım sisteminden component library oluştur.

[Screenshots of components page]

Her component için:
1. Tüm variant'ları implemente et (default, hover, active, disabled)
2. İkonlar → Lucide React + fallback inline SVG
3. Renk sistemi → CSS custom properties (--color-primary-500 vb.)
4. Typography → font-size/weight/line-height sistemi
5. Spacing → 4px grid sistemi
6. Her image alanında SafeImage component kullan (onError + gradient fallback)

Export: Tek React artifact dosyası, tüm component'lar bir arada.
```

---

## 2. Claude Code / Cursor Prompts

### Template E: MCP Full Implementation

Use with Figma MCP connected in Claude Code or Cursor.

```markdown
Bu Figma frame'ini kodla implemente et:
[Figma URL]

## Workflow
1. get_design_context ile node yapısını al
2. get_screenshot ile görsel referans al
3. Her element'i kategorize et (image / vector / shape)
4. Asset'leri indir:
   - Image fill'ler → PNG olarak indir, src/assets/ klasörüne koy
   - Vector'ler → SVG olarak indir, SVGO ile optimize et
   - Shape/gradient → Kod olarak oluştur
5. [React/Vue/Svelte/Compose] olarak implemente et
6. Screenshot ile karşılaştırıp fidelity kontrol et

## Asset Kuralları
- MCP'den gelen localhost URL'leri direkt kullan
- Yeni ikon paketi ekleme, Figma payload'daki asset'leri kullan
- Placeholder oluşturma, gerçek asset varsa onu kullan
```

### Template F: Icon Library Extraction

```markdown
Bu Figma dosyasından icon library oluştur:
[Figma icon page URL]

## Adımlar
1. get_design_context ile tüm ikon node'larını bul
2. Her ikonu SVG olarak export et
3. SVGO ile optimize et (metadata temizle, path'leri basitleştir)
4. [Platform] formatına çevir:
   - React: SVG React component'leri
   - Android: Vector Drawable XML
   - iOS: SVG asset catalog
   - KMP: composeResources/drawable/ SVG dosyaları
5. TypeScript type definition oluştur
6. Index file ile tree-shaking ready export
```

### Template G: Design Token Extraction

```markdown
Bu Figma tasarımından design token'ları çıkar:
[Figma URL]

## İstenen Token'lar
1. Colors → CSS custom properties + Kotlin Color values
2. Typography → Font family, size, weight, line-height scale
3. Spacing → 4px base grid token'ları
4. Border radius → Token scale
5. Shadow → Elevation token'ları
6. Image/asset referansları → Hangi node'lar image fill, hangileri vector?

## Output Format
- tokens.css (CSS custom properties)
- tokens.kt (Kotlin object)
- asset-manifest.json (her asset'in tipi ve path'i)
```

---

## 3. REST API Script Prompts

### Template H: Automated Asset Pipeline

```markdown
Figma REST API kullanarak bu dosyadan otomatik asset extraction script'i yaz:

File Key: {file_key}
Frame Node: {node_id}

## Script Gereksinimleri
1. GET /files/{key}/nodes?ids={node_id} ile tüm node'ları al
2. Her node'u classify et:
   - fills[].type === "IMAGE" → RASTER
   - node.type === "VECTOR" → SVG
   - fills[].type === "SOLID" → SKIP (kod ile yapılacak)
3. RASTER node'ları PNG olarak export et (scale=2)
4. VECTOR node'ları SVG olarak export et (svg_outline_text=true)
5. Asset manifest JSON oluştur
6. Dosyaları organize et:
   assets/
   ├── images/    (PNG/WebP)
   ├── icons/     (SVG)
   └── manifest.json

## Çıktı: Python veya Node.js script
```

---

## 4. Platform-Specific Prompts

### Template I: Figma → Compose Multiplatform

```markdown
Bu Figma mobil tasarımını Kotlin Compose Multiplatform kodu olarak yaz:
[Figma URL veya screenshot]

## Asset Pipeline
- Fotoğraflar → AsyncImage (Coil) + placeholder + error composable
- İkonlar → SVG'den ImageVector'e çevir veya Compose Resources kullan
- Shape → Modifier.background(Brush...) veya Canvas composable
- Gradient → Brush.linearGradient / radialGradient
- Shadow → Modifier.shadow(elevation)

## Mimari
- MVI pattern
- Koin dependency injection
- State: data class ile immutable state
- Preview: @Preview annotation ile

## Image Handling
@Composable
fun NetworkImage(url: String, modifier: Modifier) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(Res.drawable.placeholder),
        error = painterResource(Res.drawable.error_placeholder)
    )
}
```

### Template J: Figma → SwiftUI

```markdown
Bu Figma tasarımını SwiftUI kodu olarak yaz:
[Figma URL veya screenshot]

## Asset Handling
- Fotoğraflar → AsyncImage(url:) { phase in ... } ile loading/error state
- İkonlar → SF Symbols veya custom SVG (Asset Catalog)
- Shape → SwiftUI Shape / custom Path
- Gradient → LinearGradient / RadialGradient
```

---

## 5. SVG-First Prompts

### Template K: Pure SVG Artifact

Zero external dependency, everything is inline SVG.

```markdown
Bu tasarımı implemente et. External dependency SIFIR olacak.

## SVG Stratejisi
Tüm görselleri SVG teknikleri ile yap:

### Fotoğraf Alanları (abstract geometric composition)
<svg viewBox="0 0 {w} {h}">
  <defs>
    <linearGradient id="g1" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="{color1}"/>
      <stop offset="100%" stop-color="{color2}"/>
    </linearGradient>
  </defs>
  <rect fill="url(#g1)" width="{w}" height="{h}" rx="12"/>
  <!-- Floating geometric shapes -->
  <circle cx="..." cy="..." r="..." fill="rgba(255,255,255,0.1)"/>
  <rect ... transform="rotate(15 ...)" fill="rgba(255,255,255,0.08)"/>
</svg>

### Avatar (initials-based)
<svg width="48" height="48" viewBox="0 0 48 48">
  <circle cx="24" cy="24" r="24" fill="{hashColor}"/>
  <text x="24" y="24" text-anchor="middle" dy=".35em"
        fill="white" font-size="18" font-weight="600">
    {initials}
  </text>
</svg>

### İkonlar (custom SVG path)
Lucide import yerine, kendi SVG path'lerimi çiz.

### Dekoratif Dokular
<svg>
  <filter id="noise">
    <feTurbulence type="fractalNoise" baseFrequency="0.65" numOctaves="3"/>
    <feColorMatrix type="saturate" values="0"/>
  </filter>
  <rect filter="url(#noise)" opacity="0.05" width="100%" height="100%"/>
</svg>

### Animasyonlu Elemanlar
<svg>
  <circle r="4" fill="#3B82F6">
    <animate attributeName="opacity" values="1;0.3;1" dur="1.5s" repeatCount="indefinite"/>
  </circle>
</svg>
```

### Template L: SVG Icon System

```markdown
Tasarımdaki tüm ikonlar için custom inline SVG icon system oluştur.

Her ikon:
- 24x24 viewBox
- currentColor fill (parent'tan renk alsın)
- strokeWidth prop
- size prop (width/height)

const Icon = ({ name, size = 24, className }) => {
  const paths = {
    'arrow-left': 'M19 12H5M12 19l-7-7 7-7',
    'heart': 'M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z',
    'share': 'M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8M16 6l-4-4-4 4M12 2v13',
    // ... diğer ikonlar
  };

  return (
    <svg width={size} height={size} viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth={2}
         strokeLinecap="round" strokeLinejoin="round"
         className={className}>
      <path d={paths[name]} />
    </svg>
  );
};
```

---

## 6. Reusable Components

### SafeImage (React — for Artifacts)

```jsx
const SafeImage = ({ src, alt, className, aspectRatio, fallbackGradient }) => {
  const [status, setStatus] = useState('loading'); // loading | loaded | error
  const gradient = fallbackGradient || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';

  return (
    <div
      className={className}
      style={{
        position: 'relative',
        overflow: 'hidden',
        aspectRatio: aspectRatio || 'auto',
      }}
    >
      {/* Gradient fallback / skeleton */}
      {status !== 'loaded' && (
        <div style={{
          position: 'absolute', inset: 0,
          background: status === 'error' ? gradient : undefined,
          animation: status === 'loading' ? 'pulse 2s infinite' : undefined,
        }}>
          {status === 'loading' && (
            <div style={{
              position: 'absolute', inset: 0,
              background: `linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent)`,
              animation: 'shimmer 1.5s infinite',
            }} />
          )}
        </div>
      )}

      {status !== 'error' && (
        <img
          src={src}
          alt={alt || ''}
          loading="lazy"
          onLoad={() => setStatus('loaded')}
          onError={() => setStatus('error')}
          style={{
            width: '100%', height: '100%',
            objectFit: 'cover',
            opacity: status === 'loaded' ? 1 : 0,
            transition: 'opacity 0.3s ease',
          }}
        />
      )}
    </div>
  );
};
```

### SVG Initials Avatar (React)

```jsx
const InitialsAvatar = ({ name, size = 48 }) => {
  const initials = name
    .split(' ')
    .map(n => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

  const colors = [
    '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4',
    '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F',
    '#BB8FCE', '#85C1E9', '#F0B27A', '#82E0AA',
  ];
  const colorIndex = name.split('').reduce((acc, c) => acc + c.charCodeAt(0), 0) % colors.length;

  return (
    <svg width={size} height={size} viewBox="0 0 48 48">
      <circle cx="24" cy="24" r="24" fill={colors[colorIndex]} />
      <text
        x="24" y="24"
        textAnchor="middle"
        dominantBaseline="central"
        fill="white"
        fontSize="18"
        fontWeight="600"
        fontFamily="system-ui, sans-serif"
      >
        {initials}
      </text>
    </svg>
  );
};
```

### Geometric Hero Placeholder (React)

```jsx
const HeroPlaceholder = ({ width = 375, height = 200, colors = ['#667eea', '#764ba2'] }) => (
  <svg viewBox={`0 0 ${width} ${height}`} style={{ width: '100%', display: 'block' }}>
    <defs>
      <linearGradient id="hero-bg" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stopColor={colors[0]} />
        <stop offset="100%" stopColor={colors[1]} />
      </linearGradient>
    </defs>
    <rect fill="url(#hero-bg)" width={width} height={height} />
    <circle cx={width * 0.2} cy={height * 0.3} r={height * 0.25}
            fill="rgba(255,255,255,0.08)" />
    <circle cx={width * 0.8} cy={height * 0.7} r={height * 0.35}
            fill="rgba(255,255,255,0.05)" />
    <rect x={width * 0.5} y={height * 0.1} width={height * 0.4} height={height * 0.4}
          rx="16" fill="rgba(255,255,255,0.06)"
          transform={`rotate(15 ${width * 0.5 + height * 0.2} ${height * 0.1 + height * 0.2})`} />
  </svg>
);
```

### NetworkImage (Compose Multiplatform)

```kotlin
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    var isError by remember { mutableStateOf(false) }

    if (isError) {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                    )
                )
            )
        )
    } else {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            onError = { isError = true },
            placeholder = ColorPainter(placeholderColor),
        )
    }
}
```
