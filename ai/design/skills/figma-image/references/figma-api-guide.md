# Figma API Guide — Image Detection & SVG Export

## Table of Contents
1. [Figma MCP Server Tools](#1-figma-mcp-server-tools)
2. [Figma REST API Detection](#2-figma-rest-api-detection)
3. [Figma Plugin API Detection](#3-figma-plugin-api-detection)
4. [figma-mcp-full-server (Enhanced)](#4-figma-mcp-full-server)
5. [SVG Export API Details](#5-svg-export-api-details)
6. [Image Fill Paint Structure](#6-image-fill-paint-structure)

---

## 1. Figma MCP Server Tools

### Official Remote MCP Server

**Endpoint:** `https://mcp.figma.com/mcp`

**Setup (Claude Code):**
```bash
claude mcp add --scope user --transport http figma https://mcp.figma.com/mcp
```

**Setup (Cursor):**
Install the Figma Plugin for Cursor, or manually add in MCP settings.

**Core Tools:**

| Tool | Purpose | Rate Limited |
|---|---|---|
| `get_design_context` | Structured node tree + generated code + asset URLs | Yes |
| `get_screenshot` | Visual PNG screenshot of a node | Yes |
| `get_metadata` | High-level node map (use when context is too large) | Yes |
| `generate_figma_design` | Capture live UI → Figma (Claude Code & Codex only) | No |

**Asset Delivery Modes:**

The MCP server delivers assets in different ways depending on configuration:

1. **Local image server** (default for Desktop MCP):
   ```
   http://localhost:3845/assets/89f254d1a998c9a6d1d324d43c73539c3993b16e.png
   http://localhost:3845/assets/abc123def456.svg
   ```

2. **Import references** (Remote MCP / get_design_context):
   ```javascript
   import { img } from "./svg-4zqz3";    // SVG asset reference
   const heroImg = "http://localhost:3845/assets/abc.png";  // Raster image
   ```

3. **Download mode**: Saves images directly to disk.

**How to tell image from vector in MCP response:**
- `.png` / `.jpg` URL → Raster photo (IMAGE fill)
- `.svg` URL or SVG import → Vector graphic (VECTOR node)
- No asset URL, just CSS → Shape/gradient, code it directly

### Required Agent Rules (CLAUDE.md / .cursorrules)

```markdown
## Figma MCP server rules
- The Figma MCP server provides an assets endpoint which can serve image and SVG assets
- IMPORTANT: If the Figma MCP server returns a localhost source for an image or an SVG,
  use that image or SVG source directly
- IMPORTANT: DO NOT import/add new icon packages, all the assets should be in the Figma payload
- IMPORTANT: do NOT use or create placeholders if a localhost source is provided
```

### Recommended Implementation Flow

```markdown
## Figma MCP Integration Rules

### Required flow (do not skip)
1. Run get_design_context first for the exact node(s).
2. If response is too large/truncated, run get_metadata, then re-fetch specific nodes.
3. Run get_screenshot for visual reference.
4. Only after both get_design_context and get_screenshot, download assets and implement.
5. Translate output into project conventions. Reuse existing tokens and components.
6. Validate against Figma screenshot for 1:1 fidelity.

### Asset handling
- Image fill nodes (photos, avatars) → Download as PNG/JPG, use as project resource
- Vector nodes (icons, logos) → Download as SVG, convert to platform format
- Shape nodes (solid, gradient fills) → Build programmatically, don't export as asset
```

### Rate Limits

- Starter plan / View-only seats: **6 tool calls per month**
- Dev or Full seat (Professional+): Per-minute limits matching Figma REST API Tier 1
- `generate_figma_design` is exempt from rate limits

---

## 2. Figma REST API Detection

### Authentication

```bash
# Personal Access Token
curl -H 'X-FIGMA-TOKEN: figd_xxxxx' https://api.figma.com/v1/files/{file_key}

# Or OAuth2 Bearer Token
curl -H 'Authorization: Bearer xxxxx' https://api.figma.com/v1/files/{file_key}
```

### Get File Nodes (detect image fills)

```
GET https://api.figma.com/v1/files/{file_key}/nodes?ids={node_id}
```

Response structure for a node with an image fill:
```json
{
  "id": "1:23",
  "name": "Hero Banner",
  "type": "RECTANGLE",
  "fills": [
    {
      "type": "IMAGE",
      "scaleMode": "FILL",
      "imageRef": "abc123def456789",
      "visible": true,
      "opacity": 1
    }
  ],
  "absoluteBoundingBox": {
    "x": 0, "y": 100,
    "width": 375, "height": 200
  }
}
```

Response structure for a vector node:
```json
{
  "id": "1:45",
  "name": "back-arrow",
  "type": "VECTOR",
  "fills": [
    {
      "type": "SOLID",
      "color": { "r": 1, "g": 1, "b": 1, "a": 1 }
    }
  ],
  "absoluteBoundingBox": {
    "x": 16, "y": 56,
    "width": 24, "height": 24
  }
}
```

### Classification Logic

```python
def classify_node(node):
    """Classify a Figma node into asset categories."""
    fills = node.get("fills", [])
    node_type = node.get("type", "")
    bbox = node.get("absoluteBoundingBox", {})
    w, h = bbox.get("width", 0), bbox.get("height", 0)

    # Check for image fills (photos, avatars, banners)
    has_image_fill = any(f.get("type") == "IMAGE" for f in fills)
    if has_image_fill:
        return "RASTER_IMAGE"

    # Vector nodes (icons, logos, illustrations)
    if node_type in ("VECTOR", "BOOLEAN_OPERATION", "LINE", "STAR", "POLYGON"):
        return "VECTOR_SVG"

    # Text
    if node_type == "TEXT":
        return "TEXT"

    # Small frames with only vector children → likely icon component
    if node_type in ("COMPONENT", "INSTANCE", "FRAME", "GROUP"):
        if w <= 64 and h <= 64:
            children = node.get("children", [])
            if all(classify_node(c) == "VECTOR_SVG" for c in children):
                return "VECTOR_SVG"

    # Containers
    if node_type in ("FRAME", "GROUP", "SECTION", "COMPONENT", "INSTANCE"):
        return "CONTAINER"

    # Shapes with solid/gradient fills
    has_solid = any(f.get("type") == "SOLID" for f in fills)
    has_gradient = any("GRADIENT" in f.get("type", "") for f in fills)
    if has_solid or has_gradient:
        return "SHAPE"

    return "UNKNOWN"
```

### Get All Image Fills (download URLs)

```
GET https://api.figma.com/v1/files/{file_key}/images
```

Returns a mapping from `imageRef` → download URL:
```json
{
  "meta": {
    "images": {
      "abc123def456789": "https://figma-alpha-api.s3.us-west-2.amazonaws.com/images/...",
      "xyz789ghi012345": "https://figma-alpha-api.s3.us-west-2.amazonaws.com/images/..."
    }
  }
}
```

These URLs expire after **14 days**.

### Export Node as Image/SVG

```
GET https://api.figma.com/v1/images/{file_key}
  ?ids={node_id}
  &format=svg          # or png, jpg, pdf
  &svg_outline_text=true
  &svg_include_id=true
  &svg_include_node_id=true
  &scale=2             # for raster formats only
```

**SVG-specific parameters:**
- `svg_outline_text` (boolean): Convert text to paths (removes font dependency)
- `svg_include_id` (boolean): Add layer names as SVG `id` attributes
- `svg_include_node_id` (boolean): Add Figma node IDs as `data-node-id`
- `svg_simplify_stroke` (boolean): Simplify strokes for cleaner output

**Warning:** If the node has IMAGE fills, the SVG export will embed raster data as
base64 `<image>` tags. The resulting SVG is NOT vector and will be very large.

---

## 3. Figma Plugin API Detection

For plugin development or when using tools built on the Plugin API:

```typescript
// Check if a node has image fills
function hasImageFill(node: SceneNode): boolean {
  if (!('fills' in node)) return false;
  const fills = node.fills as readonly Paint[];
  return fills.some(fill => fill.type === 'IMAGE');
}

// Get image hash from a node
function getImageHash(node: SceneNode): string | null {
  if (!('fills' in node)) return null;
  const fills = node.fills as readonly Paint[];
  const imageFill = fills.find(f => f.type === 'IMAGE') as ImagePaint | undefined;
  return imageFill?.imageHash ?? null;
}

// Export node as SVG string
async function exportSVG(node: SceneNode): Promise<string> {
  return await node.exportAsync({ format: 'SVG_STRING' });
}

// Export node as PNG bytes
async function exportPNG(node: SceneNode, scale = 2): Promise<Uint8Array> {
  return await node.exportAsync({
    format: 'PNG',
    constraint: { type: 'SCALE', value: scale }
  });
}

// Built-in asset detection heuristic
// Available on FrameNode and similar:
// node.isAsset → boolean
// "At a high level an icon is a small vector graphic
//  and an image is a node with an image fill."
```

### Paint Types Reference

```typescript
type Paint =
  | SolidPaint          // type: "SOLID" — flat color
  | GradientPaint       // type: "GRADIENT_LINEAR" | "GRADIENT_RADIAL" | "GRADIENT_ANGULAR" | "GRADIENT_DIAMOND"
  | ImagePaint          // type: "IMAGE" — raster photo/texture
  | VideoPaint          // type: "VIDEO" — video fill

interface ImagePaint {
  type: "IMAGE";
  scaleMode: "FILL" | "FIT" | "CROP" | "TILE";
  imageHash: string;      // Reference to the image data
  imageTransform?: Transform;
  scalingFactor?: number;
  rotation?: number;
  filters?: ImageFilters;
  visible?: boolean;
  opacity?: number;
  blendMode?: BlendMode;
}
```

---

## 4. figma-mcp-full-server (Enhanced)

An enhanced community MCP server with additional capabilities beyond the official server.

### Installation

```bash
npm install -g figma-mcp-full-server
figma-mcp-full-server figd_YOUR_TOKEN_HERE
```

### Claude Desktop / Cursor Config

```json
{
  "mcpServers": {
    "figma-mcp": {
      "command": "node",
      "args": ["/path/to/figma-mcp/build/index.js"],
      "env": {
        "FIGMA_TOKEN": "figd_your_token"
      }
    }
  }
}
```

### Extra Capabilities

| Capability | What It Does |
|---|---|
| **Design element analysis** | Deep analysis: images, vectors, components identified |
| **SVG extraction** | Get SVG code for vector graphics directly (path data) |
| **Image export** | Export nodes as PNG, JPG, SVG, PDF |
| **Batch export** | Multiple nodes in one operation |
| **Image asset recognition** | Find ALL images including embedded and external |
| **Vector extraction** | Extract SVG paths, shapes, icons separately |
| **Component analysis** | Identify components vs instances |

### Example Prompts

```
"Analyze what design elements this Figma node contains"
→ Returns categorized list: images, vectors, text, frames

"Extract all image assets under this Figma node"
→ Downloads all raster images from the node tree

"Get the SVG code for this Figma node"
→ Returns raw SVG markup with path data

"Fetch images from this Figma URL"
→ Exports node images in specified format

"Get style data from this design and generate CSS"
→ Extracts colors, typography, spacing as CSS
```

---

## 5. SVG Export API Details

### What Comes Out Clean as SVG

| Figma Element | SVG Output | Quality |
|---|---|---|
| Rectangle (solid fill) | `<rect>` | Perfect |
| Ellipse (solid fill) | `<ellipse>` | Perfect |
| Vector path | `<path d="...">` | Perfect |
| Boolean operation | Combined `<path>` | Good |
| Text (outlined) | `<path>` per glyph | Good but large |
| Text (as text) | `<text>` element | Font-dependent |
| Linear gradient | `<linearGradient>` + `<rect>` | Perfect |
| Radial gradient | `<radialGradient>` + `<rect>` | Perfect |
| Drop shadow | `<filter>` + `<feDropShadow>` | Some apps struggle |
| Blur | `<filter>` + `<feGaussianBlur>` | Some apps struggle |
| **Image fill** | **`<image href="data:image/png;base64,..."/>`** | **NOT vector, very large** |

### SVG Optimization Pipeline

```
Figma export (raw SVG)
  → SVGO optimization (remove metadata, simplify paths)
  → Platform conversion:
     Web:     React SVG component or inline <svg>
     Android: Android Vector Drawable XML (via vd-tool or AS import)
     iOS:     SVG asset in Xcode asset catalog
     KMP:     composeResources/drawable/ (SVG supported)
```

### SVGO Config for Figma SVGs

```javascript
// svgo.config.js
module.exports = {
  plugins: [
    'removeDoctype',
    'removeXMLProcInst',
    'removeComments',
    'removeMetadata',
    'removeEditorsNSData',
    'cleanupAttrs',
    'mergeStyles',
    'inlineStyles',
    'minifyStyles',
    'cleanupIds',
    'removeUselessDefs',
    'cleanupNumericValues',
    'convertColors',
    'removeUnknownsAndDefaults',
    'removeNonInheritableGroupAttrs',
    'removeUselessStrokeAndFill',
    'cleanupEnableBackground',
    'removeHiddenElems',
    'removeEmptyText',
    'convertShapeToPath',
    'convertEllipseToCircle',
    'moveElemsAttrsToGroup',
    'collapseGroups',
    'convertPathData',
    'convertTransform',
    'removeEmptyAttrs',
    'removeEmptyContainers',
    'removeUnusedNS',
    'sortDefsChildren',
    'removeTitle',
    'removeDesc',
  ]
};
```

---

## 6. Image Fill Paint Structure

### REST API Paint Object (in GET files response)

```json
{
  "blendMode": "NORMAL",
  "type": "IMAGE",
  "scaleMode": "FILL",
  "imageRef": "0:1",
  "imageTransform": [[1, 0, 0], [0, 1, 0]],
  "scalingFactor": 0.5,
  "rotation": 0,
  "filters": {
    "exposure": 0,
    "contrast": 0,
    "saturation": 0,
    "temperature": 0,
    "tint": 0,
    "highlights": 0,
    "shadows": 0
  }
}
```

### Getting the Actual Image Data

1. Find `imageRef` values in the file JSON (under `fills` of any node)
2. Call `GET /v1/files/{key}/images` to get ref → URL mapping
3. Download from the URL (expires in 14 days)

Or export the node directly:
```
GET /v1/images/{key}?ids={node_id}&format=png&scale=2
```
