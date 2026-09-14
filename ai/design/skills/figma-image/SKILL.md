---
name: figma-image
description: >
  Handle images, SVGs, and visual assets when implementing Figma designs as code.
  Use this skill whenever a task involves converting Figma designs to code and the design
  contains images, photos, icons, illustrations, avatars, or any visual assets. Also trigger
  when the user mentions "implement design", "Figma to code", "design implementation",
  "export assets from Figma", "SVG from Figma", "image handling in artifacts", or asks
  about why images are broken/missing in Claude artifacts. Trigger even if the user just
  pastes a Figma URL and asks to implement it — images are almost always involved.
  This skill covers: Figma MCP image/SVG asset extraction, Figma REST API image detection,
  image-vs-vector classification, SVG export strategies, Claude artifact image workarounds,
  mobile (Compose/KMP/SwiftUI) asset pipelines, and prompt engineering for design fidelity.
---

# Figma Image Skill

Turn Figma designs into code with **pixel-perfect image and asset handling**. This skill
solves the #1 pain point in AI design implementation: images that break, disappear, or
render as blank boxes.

## Core Concept: Figma Has No Image Layers

Figma stores every photo as a **rectangle (or ellipse/frame) with an `IMAGE` type fill**.
Dragging a photo into Figma creates a `RECTANGLE` node whose `fills` array contains a
paint with `type: "IMAGE"` and an `imageRef` hash. This means:

- There is no dedicated "image node type" — you detect images by inspecting fills
- Vectors (icons, logos) are `VECTOR` or `BOOLEAN_OPERATION` nodes with SVG path data
- The same `RECTANGLE` node type can be a solid color, a gradient, OR a photo

Understanding this distinction is the foundation of correct asset handling.

---

## Decision Flow

When you receive a Figma design to implement, follow this sequence:

```
1. DETECT  → Classify every visual element (image / vector / shape / text)
2. EXTRACT → Pull assets using the right tool and format
3. CONVERT → Transform assets for the target platform
4. EMBED   → Place assets in code with proper fallbacks
```

For detailed API references, read `references/figma-api-guide.md`.
For prompt templates and examples, read `references/prompt-templates.md`.

---

## Step 1: DETECT — Classify Elements

### Using Figma MCP (preferred when available)

If a Figma MCP server is connected, use this flow:

```
1. get_design_context(figma_url)    → Returns node tree with fill types
2. get_screenshot(figma_url)         → Visual reference
3. Examine the response for asset types
```

The MCP response reveals element types through:
- **Localhost image URLs** (`http://localhost:3845/assets/abc.png`) → This is a raster photo
- **Localhost SVG URLs** (`http://localhost:3845/assets/def.svg`) → This is a vector
- **Code references** (`import { img } from "./svg-4zqz3"`) → This is a vector asset
- **CSS colors/gradients in generated code** → This is a shape, build in code

### Using Figma REST API (when MCP unavailable)

Fetch the file JSON and check each node's `fills` array:

```
Image:    fills[].type === "IMAGE"     → has imageRef
Vector:   node.type === "VECTOR"       → has SVG path data
Shape:    fills[].type === "SOLID"     → plain color
Gradient: fills[].type === "GRADIENT_LINEAR" (etc.) → gradient
```

See `references/figma-api-guide.md` Section 2 for the full REST API detection flow.

### Using Screenshots Only (Claude.ai artifacts)

When no API access is available (just a screenshot pasted into Claude.ai), you CANNOT
programmatically detect image types. Instead, ASK the user or make educated guesses:

- Large rectangular areas with photographic content → likely raster IMAGE
- Small symbolic graphics → likely VECTOR icons
- Uniform colored areas → likely SHAPE (code it, don't export)
- User avatars (circular photos) → likely raster IMAGE with clip

Always state your assumptions: "I'm treating the hero banner as a photo and the icons
as vectors. Let me know if any of these are different."

---

## Step 2: EXTRACT — Pull Assets Correctly

### Critical Rule: Photos Do NOT Become Vectors

Exporting a photo as SVG does NOT vectorize it. The SVG will contain a base64-encoded
`<image>` tag — still raster, but now 2-3x larger. Never export photos as SVG.

### Asset Extraction Table

| Element Type        | Figma MCP                              | REST API                           | No API (screenshot)        |
|---------------------|----------------------------------------|------------------------------------|----------------------------|
| **Photo/raster**    | Use localhost PNG/JPG URL directly     | GET /images → PNG/JPG              | picsum.photos placeholder  |
| **Icon (vector)**   | Use localhost SVG URL directly         | GET /images?format=svg             | Lucide React icon          |
| **Logo (vector)**   | Use localhost SVG URL directly         | GET /images?format=svg             | Inline SVG approximation   |
| **Shape**           | Don't export — code it                 | Don't export — code it             | Tailwind/CSS               |
| **Gradient**        | Don't export — code it                 | Don't export — code it             | CSS gradient               |

### MCP Asset Rules (from official Figma MCP guide)

When Figma MCP returns assets, follow these rules strictly:

1. **If MCP returns a localhost URL for image or SVG → use it directly**
2. **DO NOT import new icon packages** — all assets come from the Figma payload
3. **DO NOT create placeholders** if a localhost source is provided
4. **Download needed assets** before starting implementation

### SVG Extraction Best Practices

For clean SVG output from vectors:

- REST API: `GET /images/{key}?ids={node}&format=svg&svg_outline_text=true`
- Plugin API: `node.exportAsync({ format: 'SVG_STRING' })`
- figma-mcp-full-server: "Get the SVG code for this Figma node"

For SVG optimization before embedding:
- Remove unnecessary metadata (`<metadata>`, comments)
- Simplify paths with SVGO if the SVG is large
- Convert to target format (ImageVector for Compose, React component for web)

---

## Step 3: CONVERT — Platform-Specific Transforms

### Web (React/HTML) — Claude Artifacts

Claude artifacts run in a sandboxed iframe with strict CSP. External images often fail.

**Working image sources in artifacts:**
- `https://picsum.photos/{width}/{height}` — CORS-friendly photo placeholder
- Inline SVG (`<svg>...</svg>`) — always works, no network needed
- Lucide React icons (`import { Icon } from "lucide-react"`) — pre-bundled
- CSS gradients, patterns — no network needed
- Data URLs (`data:image/svg+xml,...`) — works but watch size limits

**Broken image sources in artifacts:**
- Direct Unsplash URLs — CORS blocked
- Random CDN image URLs — often CORS blocked
- Large base64 images — may hit size limits

**Required pattern — SafeImage component:**

Every `<img>` tag must have an error fallback:

```jsx
const SafeImage = ({ src, alt, className, fallbackGradient }) => {
  const [error, setError] = useState(false);
  const [loaded, setLoaded] = useState(false);
  const gradient = fallbackGradient || 'linear-gradient(135deg, #667eea, #764ba2)';

  if (error) {
    return <div className={className} style={{ background: gradient }} />;
  }

  return (
    <div className={className} style={{ position: 'relative' }}>
      {!loaded && <div style={{ position: 'absolute', inset: 0, background: gradient }} />}
      <img
        src={src}
        alt={alt}
        onLoad={() => setLoaded(true)}
        onError={() => setError(true)}
        style={{ opacity: loaded ? 1 : 0, transition: 'opacity 0.3s' }}
      />
    </div>
  );
};
```

### Mobile — Compose Multiplatform / KMP

| Asset Type | Compose Implementation |
|---|---|
| Photo (network) | `AsyncImage` with Coil/Kamel + placeholder + error state |
| Photo (local) | `painterResource(Res.drawable.xxx)` in composeResources |
| SVG icon | Convert to `ImageVector` or use `painterResource` for SVG files |
| Shape | `Modifier.background(brush)` or `Canvas` composable |
| Gradient | `Brush.linearGradient(...)` / `Brush.radialGradient(...)` |

SVG → Compose ImageVector conversion:
1. Android Studio: File → New → Vector Asset → SVG file
2. Or: place SVG in `commonMain/composeResources/drawable/`
3. Or: manually build `ImageVector.Builder` from path data

### iOS — SwiftUI

| Asset Type | SwiftUI Implementation |
|---|---|
| Photo (network) | `AsyncImage(url:)` with placeholder |
| Photo (local) | Asset catalog image |
| SVG icon | SF Symbols or SVG in asset catalog |
| Shape | SwiftUI Shape / Path |

---

## Step 4: EMBED — Write Code With Proper Fallbacks

### For Claude Artifacts (React/HTML)

Always generate code with this image strategy:

```
Photos     → <SafeImage src="https://picsum.photos/{w}/{h}" fallbackGradient="..." />
Avatars    → CSS initials avatar (colored circle + letter) — no network dependency
Icons      → import { IconName } from "lucide-react"
Logos      → Inline <svg> with approximate path
Decorative → CSS gradient + optional SVG pattern overlay
Empty state→ Inline SVG illustration
```

### For Code Editors (Claude Code / Cursor with MCP)

```
Photos     → Download from MCP localhost URL → save to project assets
Icons      → Download SVG from MCP → convert to platform format
Shapes     → Generate in code from design tokens
Everything → Validate against Figma screenshot for 1:1 fidelity
```

---

## Quick Reference: The 10 Rules

1. **Classify first, code second** — Know what's a photo vs vector vs shape before writing any code
2. **Photos stay raster** — Never SVG-export a photo; use PNG/JPG/WebP
3. **Vectors go SVG** — Icons and logos export as clean SVG paths
4. **Shapes become code** — Solid colors and gradients are cheaper as CSS/Compose code
5. **Every image needs a fallback** — `onError` handler + gradient placeholder, always
6. **Artifacts need special care** — Sandbox blocks most external URLs; use picsum.photos + inline SVG
7. **MCP localhost URLs are sacred** — If MCP gives you a URL, use it directly, no placeholders
8. **Specify strategy in prompts** — Explicitly tell the AI how to handle each image type
9. **Use aspect-ratio CSS** — Prevents layout shift while images load
10. **Prefer inline SVG over network SVG** — Eliminates CORS/loading issues entirely
