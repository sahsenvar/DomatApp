# MCP Server Konfigürasyonları

Bu projede kullanılan MCP sunucuları.

## Figma Desktop MCP

Figma tasarımlarından design context, screenshot ve asset çıkarmak için kullanılır.

### Kurulum
```bash
claude mcp add --transport http figma-desktop http://127.0.0.1:3845/mcp
```

### Gereksinim
- Figma Desktop uygulaması açık olmalı
- Figma Dev Mode MCP Server plugin yüklü olmalı
- Çalışmak istediğin Figma dosyası aktif tab'da olmalı

### Araçlar
| Tool | Ne Yapar |
|------|----------|
| `get_design_context` | Node yapısı + kod + asset URL'leri |
| `get_screenshot` | Node'un PNG screenshot'ı |
| `get_metadata` | Hafif node ağacı (XML) |
| `get_variable_defs` | Design token/variable tanımları |
| `get_figjam` | FigJam board içeriği |

### Screenshot'ı Diske Kaydetme
MCP `get_screenshot` inline image döner, dosyaya kaydetmez.
Diske kaydetmek için JSON-RPC session kullan:
```python
# Detaylı script: ai/design/skills/figma-to-compose/SKILL.md → AŞAMA 0
```

### Diğer MCP Seçenekleri
- **Figma Remote MCP:** `claude mcp add --scope user --transport http figma https://mcp.figma.com/mcp`
- **figma-mcp-full-server:** `npm install -g figma-mcp-full-server` (enhanced capabilities)
