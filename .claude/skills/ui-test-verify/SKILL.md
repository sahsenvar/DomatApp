---
name: ui-test-verify
description: >
  Verify DomatApp Compose screens against their design packages and generate their tests: Roborazzi
  preview screenshots, compare_design_package.py, check_design_texts.py, token grep, a visual review of the
  diff sheets, Maestro flows from the card's acceptance items, and ViewModel unit tests from its actions and
  navigation. Ends by opening the PR and setting the Handoff note to "incelemede · PR #n". Use after
  implementing or changing any screen or shared component, for "tasarıma uyuyor mu", "görsel doğrulama",
  "roborazzi", "pixel diff", "test yaz", "maestro", or before committing UI work.
---

# UI doğrulama ve test üretimi

Kabul ölçütleri `design/README.md` → "Doğrulama" (kural 16–18). Bu skill nasıl ölçüleceğini ve kartın
hangi testlere dönüşeceğini anlatır. Sıra: 1–4 görsel doğrulama → 5 testler → 6 PR ve devir kapanışı.

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

## 5. Testleri üret {#testler}

Kaynak her zaman kart (`design/cards/<ID>.yaml`): `acceptance`, `actions`, `navigation`, `states`. Kartta
olmayan davranışı teste yazma; kartta eksik ya da çelişkili bir şey görürsen Handoff notunun "Sorular"
bölümüne yaz ve sor (kartın sahibi Cowork).

**Maestro** — `maestro/domatapp/<ID>.yaml`, `acceptance` içinde `test: maestro` olan her madde için.
- Her madde bir akış adımı (`tapOn`, `inputText`) ya da doğrulama (`assertVisible`) olur; adımın üstüne
  `# AC-n: <text>` yorumu yazılır ki madde ↔ adım izlenebilsin.
- Metinler kartın `strings` / `sampleData` değerleriyle birebir (Maestro görünen metni eşler).
- Örnek biçim: `maestro/domatapp/Onbaording.yaml` (`appId: com.domatapp`).

**Birim testleri** — ekranın `feature:<ad>:presentation` modülünde `src/commonTest/kotlin/…/<Ekran>ViewModelTest.kt`.
Bağımlılıklar (`kotlin-test`, `kotlinx-coroutines-test`) ve Android host testi (`withHostTest`) her
`feature:*:presentation` modülünde açık; başka modüle test ekleme.
- Kartın her `actions` maddesi → intent gönder, beklenen `state` ve/veya `effect`'i doğrula.
- Her `navigation` kenarı → ViewModel doğru `XxxEffect`'i yayar. `@Effects` sağlayıcısının doğru navigator
  metodunu çağırdığını test etmek için önce üretilen `<X>Navigator`'a bak
  (`build/generated/ksp/metadata/commonMain/`): sahtelenebiliyorsa (interface) sahte navigator ile test et,
  sahtelenemiyorsa yalnızca effect'i test et ve raporda belirt.
- `acceptance` içinde `test: unit` olan maddeler bu dosyaya girer; test adı madde kimliğini taşır
  (`` `AC-3 geçersiz numarada hata metni` ``).
- `BaseViewModel` `viewModelScope` kullanır: `Dispatchers.setMain(UnconfinedTestDispatcher())` /
  `resetMain()`; effect'ler `Channel` üzerinden gelir, `runTest` içinde `viewModel.effect.first()` ile oku.
- Use case'ler ve repository sahte (fake) sınıflarla verilir; Koin başlatılmaz.
- Çalıştır: `./gradlew :feature:<ad>:presentation:testAndroidHostTest` (iOS hedefleri yalnızca macOS'ta).

**Roborazzi** — kartın her `states` maddesi için `@Preview(name = "<ID>@<durum>")` (kural 15). Ayrı test
dosyası yazılmaz; `:composeApp` önizlemelerden üretir. `test: roborazzi` maddeleri böyle karşılanır.

`test: manual` maddeleri PR açıklamasında elle kontrol listesi olarak yer alır.

## 6. PR ve devir kapanışı

1. Tek PR aç. Açıklamaya Handoff notunun node ID'sini, testleri ve (varsa) `manual` kontrol listesini yaz.
2. Figma › Handoff notunun DURUM etiketini `incelemede · PR #<no>` yap.

## Ortam notları {#ortam}

- Roborazzi Robolectric ile JVM'de çalışır; emülatör gerekmez. `composeApp/build.gradle.kts`'deki iki ayar
  kritik: `application = android.app.Application` ve JDK 21 `--add-opens/--add-exports` (L-017).
- Maven Central'ı kısıtlayan ortamda: `-Probolectric.dependency.repo.url=<ayna>` (L-024).
- Popup'lar görüntüye girmez (kural 6).

## Son adım: retro (zorunlu)

Kayıt, düzeltilecek skill'in yanında durur, hatayı fark eden tarafın değil (`design/README.md` → Hata kaydı).
Doğrulamada bulunan her gerçek farkın kök nedenini yaz: repo skill'i / script / kod / kart şeması kaynaklıysa
`ai/design/learnings.yaml`'a; kart içeriği ya da Figma çizimi kaynaklıysa Handoff notuna (Cowork kendi
kaydına işler). `stage_caused` farkın **yapıldığı** aşama (figma / code / package …), `stage_detected: verify`.
Aynı kök neden varsa `occurrences`++. `python3 ai/design/scripts/learnings.py check` geçmeli.
