# Figma Plugin API tuzakları

use_figma (Figma Plugin API) çağıran **her** iş bu tabloya uyar: repo'da `design-system-change` ve
`figma-to-package`, Cowork'te `card-to-figma`. Tek kopya burasıdır; skill'ler buraya referans verir, tabloyu
kendi içine kopyalamaz.

Yeni bir tuzak bulunca: kaydı sahibine göre aç (`design/README.md` → Hata kaydı), satırı buraya ekle.
Cowork repo'ya yazmaz; Cowork'ün bulduğu satır Handoff notuyla gelir, Claude Code ekler.

| Kayıt | Tuzak | Ne yap |
|---|---|---|
| L-018 | Hata veren use_figma çağrısı **tamamen geri alınır**. | Adımları tekrar çalıştırılabilir yaz (önce var mı kontrol et). |
| L-009 | Varyant `clone()` edilince `componentPropertyReferences` kopyalanmaz. | Klondan sonra tüm referansları yeniden bağla ve metinleri kontrol et. |
| L-010 | `resize()` auto-layout boyutlandırmasını FIXED yapar. | Sonra `*SizingMode='AUTO'` ya da `layoutSizing*` ile geri al. |
| L-011 | Değişkene bağlı dolgunun `opacity`'si örneklere geçmez. | Opaklığı **düğüm** üzerinde ver. |
| L-012 | `resetOverrides()` katman adını da sıfırlar. | Önce adı sakla, sonra geri yaz. Mümkünse hiç kullanma. |
| L-013 | use_figma çıktısı 20 KB. | Dışa aktarımı ekran başına böl. |
| L-014 | Düğümlere özel alan eklenemez (`node._x`). | `Map` kullan. |
| L-020 | `opacity: 0` ile gizlenen metin "görünür" sayılır, pakete sızar. | Gizlemek için `visible = false`. |
| — | Yazı tipi yüklenmeden metin düzenlenemez. | Her çağrının başında kullanılan fontları `loadFontAsync` ile yükle. |
