# Stitch Validation Pipeline Design

**Tarih:** 2026-03-17
**Durum:** Onaylandı

## Problem

Tasarımlar Stitch'te oluşturuluyor, ardından Figma'ya aktarılıyor. Bu aktarım sırasında bozulmalar
oluyor (renk kaymaları, spacing farkları vb.). Mevcut Figma-to-Compose pipeline bu bozulmuş
tasarımı referans alarak Compose kodu üretiyor — dolayısıyla hatalar Compose'a da taşınıyor.

## Cozum

Stitch'teki orijinal tasarımı **ground truth** olarak kullanıp, Compose implementasyonunu buna karşı
doğrula. Fark bulunursa:

1. **Figma aktarım raporu** üret — aktarımda neyin bozulduğunu raporla (kullanıcı Figma'yı manuel düzeltir)
2. **Compose kodunu Claude ile düzelt** — Stitch orijinaline göre

## Mimari

```
Stitch (orijinal tasarım - ground truth)
         │
         ├──→ Figma'ya aktarım (bozulmalar olabiliyor)
         │         │
         │         ▼
         │    Mevcut Figma Pipeline (AŞAMA 1-5)
         │    Figma MCP → Asset → SVG→XML → Token → Compose implementasyon
         │                                                │
         ▼                                                ▼
  Stitch MCP tool call                             Compose ekranı hazır
  fetch_screen_image                                      │
  (orijinal screenshot)                                   ▼
         │                                          ADB Screenshot
         │                                          (capture_screen.sh)
         │                                                │
         ▼                                                ▼
  ┌──────────────────────────────────────────────────────────┐
  │                    KARŞILAŞTIRMA                          │
  │  1. Normalize (aynı boyuta getir)                        │
  │  2. pixel_diff.py --threshold 20                         │
  │  3. Claude görsel review (iki PNG + diff PNG)            │
  │  4. Birleşik rapor                                       │
  └────────────────────────┬─────────────────────────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
     Figma Aktarım Raporu        Compose Fix (Claude)
     "Bu renk yanlış,            Claude kodu Stitch
      bu spacing bozuk"          orijinaline göre düzeltir
     (kullanıcı manuel            Edit tool ile
      Figma'yı düzeltir)                │
                                        ▼
                                  Yeni ADB screenshot
                                  → Tekrar karşılaştır
                                  (max 3 iterasyon)
```

## Stitch MCP Entegrasyonu

### Server

`davideast/stitch-mcp` — proxy modu ile Claude Code'a bağlanır.

### Onemli: Stitch MCP Stdio Proxy

Stitch MCP bir HTTP server değil, **stdio proxy**'dir. Python script'ten doğrudan bağlanılamaz
(Figma MCP'den farklı olarak). Bu nedenle:

- `fetch_screen_image` çağrısı **Claude'un MCP tool calling'i** ile yapılır
- Ayrı bir Python script (`fetch_stitch_screenshot.py`) YAZILMAZ
- Claude, tool çağrısından dönen base64 PNG'yi Bash tool ile diske kaydeder:
  `python3 -c "import base64; open('screenshots/stitch/screen.png','wb').write(base64.b64decode('...'))"`
- Tüm Stitch MCP etkileşimi SKILL.md içinde Claude tool çağrısı olarak tanımlanır

### Kurulum

```json
{
  "mcpServers": {
    "stitch": {
      "command": "npx",
      "args": ["@_davideast/stitch-mcp", "proxy"]
    }
  }
}
```

### Kullanilacak Tool'lar

| Tool | Amaç |
|------|-------|
| `list_projects` | Stitch projelerini listele |
| `list_screens` | Projedeki ekranları listele |
| `fetch_screen_image` | Orijinal tasarımın PNG screenshot'ı (base64) |
| `get_screen_code` | HTML/CSS kodu — renk/font değerlerini doğrudan parse etmek için (Katman 2'de Claude'un daha kesin fix üretmesini sağlar) |

### Stitch Screen ID Akisi

Kullanıcı Stitch proje URL'sini veya screen adını verir. Claude:
1. `list_projects` ile projeleri listeler
2. `list_screens` ile eşleşen ekranı bulur
3. `fetch_screen_image` ile screenshot alır

## Compose Screenshot Kaynagi

**ADB Screenshot** — mevcut `./ai/design/scripts/capture_screen.sh` kullanılır.

- Gerçek cihaz veya emülatörden screenshot alır
- Uygulama çalışıyor ve doğru ekranda olmalı
- Çıktı: `screenshots/compose/{screen_name}.png`

## Karsilastirma ve Rapor Motoru

Ana orkestratör: `compare_stitch.py`

### Girdiler

- Stitch screenshot: `screenshots/stitch/{screen_name}.png`
- ADB screenshot: `screenshots/compose/{screen_name}.png`

### Adim 0 — Boyut Normalizasyonu

Stitch screenshot (ör. 390x844 web frame) ile ADB screenshot (ör. 1080x2340 Android cihaz) farklı
çözünürlükte olacak. Karşılaştırma öncesi:

1. Her iki görsel küçük olan boyuta resize edilir (interpolasyon artifaktını minimize eder)
2. ADB screenshot'ından status bar ve navigation bar bölgeleri crop edilir
3. Crop değerleri CLI argümanları ile ayarlanabilir:
   - `--status-bar-height` (default: 50px)
   - `--nav-bar-height` (default: 100px)
   - `--skip-crop` flag'i ile crop atlanabilir
4. Normalize edilmiş görseller `screenshots/normalized/` altına kaydedilir

Bu adım `compare_stitch.py` içinde Pillow ile yapılır.

### Katman 1 — Pixel Diff

- Mevcut `pixel_diff.py` kullanılır
- `pixel_diff.py`'da `--threshold` / `-t` parametresi **zaten mevcut** (default: 5.0)
- `compare_stitch.py` bunu `python3 ai/design/scripts/pixel_diff.py --threshold 20` ile çağırır
- Diff görseli: `screenshots/diff/{screen_name}_diff.png`
- Çıktı: yüzdelik fark skoru

### Katman 2 — Claude Gorsel Review

- Claude'a verilecekler: Stitch screenshot, ADB screenshot, diff görseli (3 PNG)
- Claude iki kategoride fark raporu üretir:
  - **Figma aktarım raporu:** "Stitch orijinalinde bu renk X ama Compose'da Y — muhtemelen Figma
    aktarımında bozulmuş"
  - **Compose fix önerileri:** Hangi dosyada, hangi satırda, ne değişmeli
- Bu adım script değil, **SKILL.md içinde Claude'un kendisi** yapar (Read tool ile görselleri okur)

### Katman 3 — Birlesik Rapor

`compare_stitch.py` pixel diff sonucunu, Claude review raporu ise SKILL.md akışı içinde Claude
tarafından üretilir. Final rapor formatı:

```
Stitch Dogrulama Raporu — {ScreenName}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Pixel Diff: %12.4 (tolerans: %20 OK)

Figma Aktarim Raporu:
  - Primary renk #1B5E20 olmali, Figma'da #1A5D1F gelmis
  - Header padding 20dp olmali, Figma'da 16dp

Compose Duzeltmeleri:
  - AppColors.kt:14 -> Primary = Color(0xFF1B5E20)
  - HomeScreen.kt:45 -> padding(top = 20.dp)
```

### Otomatik Fix Dongusu

Fix'i **Claude** yapar (Edit tool ile Kotlin dosyalarını düzenler). Akış:

1. Claude rapordaki Compose düzeltmelerini Edit tool ile uygular
2. `./gradlew :composeApp:assembleDebug` ile build doğrulanır — başarısızsa Claude hatayı düzeltir
3. Kullanıcı uygulamayı cihazda/emülatörde yeniden açar
4. `capture_screen.sh` ile yeni ADB screenshot alınır
5. `compare_stitch.py` tekrar çalıştırılır
6. Max 3 iterasyon veya tolerans altına düşene kadar tekrarlanır
7. 3 iterasyon sonrası hâlâ tolerans aşılıyorsa: rapor kullanıcıya sunulur, karar kullanıcıda

## Dosya Yapisi

### Yeni Dosyalar

```
ai/design/
├── scripts/
│   └── compare_stitch.py            # Normalize + pixel diff orkestratoru
├── skills/
│   └── stitch-validation/
│       ├── SKILL.md                  # Claude skill tanimi (tam akis)
│       └── references/
│           └── stitch-mcp-guide.md   # Stitch MCP kurulum ve tool referansi
```

**Not:** `fetch_stitch_screenshot.py` YAZILMAZ — Stitch MCP stdio proxy olduğu için screenshot alma
işlemi SKILL.md içinde Claude tool çağrısı olarak yapılır.

### Degisecek Dosyalar

- `ai/design/skills/figma-to-compose/SKILL.md` — ASAMA 6'ya Stitch dogrulama adimi eklenir

**Not:** `pixel_diff.py`'da değişiklik gerekmez — `--threshold` parametresi zaten mevcut.

## Skill Akisi

```
1. Kullanıcıdan Stitch proje URL'si veya screen adı al
2. Claude: list_projects → list_screens → fetch_screen_image (MCP tool calls)
3. Claude: base64 PNG'yi screenshots/stitch/{screen_name}.png olarak kaydet
4. ./ai/design/scripts/capture_screen.sh → ADB'den screenshots/compose/{screen_name}.png al
5. compare_stitch.py calistir:
   a. Boyut normalizasyonu (Pillow ile)
   b. pixel_diff.py --threshold 20
   c. Sonuc: diff skoru + diff gorseli → screenshots/diff/{screen_name}_diff.png
6. Claude: Stitch PNG + ADB PNG + diff PNG'yi oku (Read tool)
7. Claude: Figma aktarim raporu + Compose fix onerileri uret
   → Rapor screenshots/reports/{screen_name}_report.md olarak kaydedilir
8. Claude: Compose fix'leri Edit tool ile uygula
9. Build dogrula (assembleDebug)
10. Kullanici uygulamayi yeniden acar
11. Yeni ADB screenshot → tekrar karsilastir (adim 4-10)
12. Max 3 iterasyon — sonrasinda rapor kullaniciya sunulur
```

## Hata Senaryolari

| Senaryo | Davranis |
|---------|----------|
| Stitch MCP erişilemez (proxy başlatılmamış) | Claude kullanıcıyı uyarır, MCP kurulum rehberine yönlendirir |
| ADB cihaz bağlı değil | `capture_screen.sh` hata verirse Claude uyarır, cihaz bağlanmasını bekler |
| Stitch'te screen bulunamıyor | `list_screens` sonucu boşsa Claude kullanıcıya sorar |
| 3 iterasyon sonrası tolerans hâlâ aşılıyor | Final rapor sunulur, karar kullanıcıda |
| Build kırılırsa (fix sonrası) | Claude build hatasını okur ve düzeltir, düzeltemezse revert + rapor |

## Figma Pipeline ile Entegrasyon

Mevcut `figma-to-compose/SKILL.md`'deki ASAMA 6 (validate) kismina ek adim:

> "Kullanıcı Stitch screen adı veya proje URL'si verdiyse, ASAMA 6 sonunda `stitch-validation`
> skill'ini çalıştır. Stitch bilgisi verilmediyse bu adım atlanır."

Bu, mevcut Figma dogrulamasinin YANINA eklenir — mevcut K1-K5 katmanlari aynen korunur.

## Kapsam Disi

- Stitch'te tasarım üretme (`generate_screen_from_text` kullanılmayacak)
- Compose Preview / Paparazzi / Roborazzi screenshot kaynakları
- Figma tarafında otomatik düzeltme (sadece rapor verilir, kullanıcı manuel düzeltir)
- `fetch_stitch_screenshot.py` gibi standalone Python script (Stitch MCP stdio proxy olduğu için)
