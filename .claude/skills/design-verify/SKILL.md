---
name: design-verify
description: >
  Verify DomatApp Compose screens against their design packages: Roborazzi preview screenshots,
  compare_design_package.py, check_design_texts.py, token grep, and a visual review of the diff sheets.
  Use after implementing or changing any screen or shared component, for "tasarıma uyuyor mu", "görsel
  doğrulama", "roborazzi", "pixel diff", or before committing UI work.
---

# Tasarım doğrulaması

Kabul ölçütleri `design/README.md` → "Doğrulama" (kural 16–18). Bu skill nasıl ölçüleceğini anlatır.

## 1. Çalıştır

```bash
./gradlew :composeApp:assembleDebug                 # uyarılar hata sayılır
./gradlew :composeApp:recordRoborazziDebug          # tüm @Preview'lar → composeApp/build/outputs/roborazzi/
python3 ai/design/scripts/check_design_texts.py <ID>
python3 ai/design/scripts/compare_design_package.py <ID>     # her durum ≤ %3,5
grep -rnE "Color\(0x|colorResource\(|RoundedCornerShape\([0-9]|\.(padding|spacedBy)\([0-9]+\.dp" <ekran paketi>
```

Bir core bileşen değiştiyse tek ekranla yetinme: **tüm paketler** için karşılaştırmayı çalıştır
(`for s in $(ls design/screens); do python3 ai/design/scripts/compare_design_package.py $s; done`).

## 2. Sonucu oku

- `raw` = düz piksel farkı (bilgi amaçlı); `diff` = 1 px komşuluk toleranslı fark (eşik bunun üzerinde).
- Kalibrasyon: tüm ekran 1 dp kayarsa ≈ %1,6, 2 dp ≈ %3,0, 4 dp ≈ %4,2. Kalan %1–3 çoğunlukla Figma/Skia
  yazı rasterleştirme farkıdır.
- **Boyut farkı** (`design px` ≠ `compose px`) → önizleme `heightDp`'si `structure.json size[1]` ile aynı değil.
- Aynı durum için birden çok render varsa betik boyutu eşleşeni seçer (Gradle önbelleği eski render'ları
  geri getirebilir, L-008).

## 3. Göz kontrolü (zorunlu) {#goz-kontrolu}

Metrik alan tabanlıdır; küçük alandaki renk/opaklık/ikon farkını kaçırır (L-019). Her ekran için en az
en karmaşık durumun fark sayfasına (`build/design-diff/<ID>/<durum>.png` = [tasarım | compose | fark]) bak:

- Fark ısısında **blok hâlinde** kırmızı (satır, bileşen) = gerçek fark. Kenar kenar ince çizgi = rasterleştirme.
- Pasif/devre dışı renkler, ikon türü, seçili durum, hata rengi — oranı düşük olsa bile karşılaştır.
- Kök neden sırası: bileşen `spec` → token → yerleşim → metin (kart). Düzeltme yönü: `existing` bileşende
  Figma koda uyar; `new` bileşende ikisi `spec`'e uyar; metinde kart kazanır.

## 4. Raporla

Tablo: ekran | durum | diff % | OK/FAIL, ve göz kontrolünde bulunanlar. `ai/design/screen-metrics.yaml`'a
ilk render (`first_pass_diff`) ve kabul (`final_diff`) değerlerini yaz.

## Ortam notları {#ortam}

- Roborazzi Robolectric ile JVM'de çalışır; emülatör gerekmez. `composeApp/build.gradle.kts`'deki iki ayar
  kritik: `application = android.app.Application` ve JDK 21 `--add-opens/--add-exports` (L-017).
- Maven Central'ı kısıtlayan ortamda: `-Probolectric.dependency.repo.url=<ayna>` (L-024).
- Popup'lar görüntüye girmez (kural 6).

## Son adım: retro (zorunlu)

Doğrulamada bulunan her gerçek farkın kök nedenini `ai/design/learnings.yaml`'a yaz. `stage_caused`
farkın **yapıldığı** aşama (figma / code / package …), `stage_detected: verify`. Aynı kök neden varsa
`occurrences`++. `python3 ai/design/scripts/learnings.py check` geçmeli.
