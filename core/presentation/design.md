# core:presentation — Komponent Dökümanı

## Genel Bakış

Tüm feature modüllerinin kullandığı ortak UI componentlerini ve MVI altyapısını barındırır.

```
core/presentation/src/
├── commonMain/
│   └── base/BaseViewModel.kt          # MVI altyapısı
└── androidMain/
    ├── compose/                        # Composition Locals
    │   ├── LocalNavigator.kt
    │   ├── LocalSnackbarHostState.kt
    │   └── LocalScrollableState.kt
    └── component/
        ├── badge/
        │   ├── DomatBadge.kt
        │   └── DomatHeroBadge.kt
        ├── bar/
        │   └── DomatBottomActionBar.kt
        ├── button/
        │   ├── DomatButton.kt
        │   └── DomatGoogleSignInButton.kt
        ├── card/
        │   ├── DomatCard.kt
        │   └── DomatLocationCard.kt
        ├── header/
        │   └── DomatScreenHeader.kt
        ├── indicator/
        │   └── DomatProgressIndicator.kt
        ├── input/
        │   ├── DomatInputDropdown.kt
        │   └── DomatTextField.kt
        ├── list/
        │   └── DomatFeatureListItem.kt
        └── tag/
            └── DomatTag.kt
```

---

## BaseViewModel

**Kaynak:** `commonMain/.../base/BaseViewModel.kt`

```kotlin
abstract class BaseViewModel<UiState : Any, Intent : Any, Effect : Any>(
    initialState: UiState
) : ViewModel()
```

### API

| Üye | Tür | Açıklama |
|-----|-----|----------|
| `state` | `StateFlow<UiState>` | UI state stream — `collectAsStateWithLifecycle()` ile screen'de observe edilir |
| `effect` | `Flow<Effect>` | Tek seferlik efektler — `Channel.BUFFERED` ile arka planda birikir |
| `onIntent(intent)` | `abstract fun` | Intent işleyici — her ViewModel override eder |
| `updateState(reducer)` | `protected fun` | State güncelleme — `_state.value = reducer(_state.value)` |
| `emitEffect(effect)` | `protected fun` | Efekt gönderme — `viewModelScope.launch` içinde çalışır |
| `currentState` | `protected val` | `state.value` kısayolu |

### Kullanım Örneği

```kotlin
class FooViewModel : BaseViewModel<FooUiState, FooIntent, FooEffect>(FooUiState()) {
    override fun onIntent(intent: FooIntent) = when (intent) {
        FooIntent.DoSomething -> {
            updateState { it.copy(isLoading = true) }
            emitEffect(FooEffect.ShowToast("Tamam"))
        }
    }
}
```

---

## Composition Locals

**Kaynak:** `androidMain/.../compose/`

| Local | Tür | Sağlayan | Hata Durumu |
|-------|-----|---------|------------|
| `LocalNavigator` | `Navigator` | `MainActivity` — `CompositionLocalProvider` | throws error |
| `LocalSnackbarHostState` | `SnackbarHostState` | `MainActivity` — `CompositionLocalProvider` | throws error |
| `LocalScrollableState` | `ScrollableState?` | İsteğe bağlı, varsayılan `null` | null döner |

```kotlin
// Erişim
val navigator = LocalNavigator.current
val snackbarHostState = LocalSnackbarHostState.current
val scrollableState = LocalScrollableState.current  // nullable
```

---

## Butonlar

### DomatPrimaryButton

**Kullanım:** Ana CTA butonu (checkout, devam et, kaydet gibi kritik aksiyonlar)

```kotlin
fun DomatPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
)
```

| Özellik | Değer |
|---------|-------|
| Yükseklik | 60dp |
| Köşe yarıçapı | 12dp |
| Arka plan | `DomatColors.primary` |
| Metin rengi | `DomatColors.textPrimary` |
| Disabled arka plan | `DomatColors.surfaceMuted` |
| Disabled metin | `DomatColors.textDisabled` |
| Tipografi | `titleLarge` |
| Leading icon | 20dp, 8dp sağ boşluk |

---

### DomatPrimaryMediumButton

**Kullanım:** Orta boy CTA (onboarding bottom bar gibi)

```kotlin
fun DomatPrimaryMediumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
```

| Özellik | Değer |
|---------|-------|
| Yükseklik | 56dp |
| Köşe yarıçapı | 12dp |
| Arka plan | `DomatColors.primary` |
| Tipografi | `titleLarge` |

---

### DomatPrimarySmallButton

**Kullanım:** Kompakt aksiyonlar (kart içi butonlar)

```kotlin
fun DomatPrimarySmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
```

| Özellik | Değer |
|---------|-------|
| Yükseklik | 36dp |
| Köşe yarıçapı | 8dp |
| Arka plan | `DomatColors.primary` |
| Tipografi | `labelLarge` |
| İç padding | horizontal 16dp, vertical 0dp |

---

### DomatSecondaryButton

**Kullanım:** İkincil aksiyonlar (iptal, geri gibi)

```kotlin
fun DomatSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
```

| Özellik | Değer |
|---------|-------|
| Yükseklik | 48dp |
| Köşe yarıçapı | 12dp |
| Arka plan | `DomatColors.surfaceDark` |
| Metin rengi | `DomatColors.textInverse` |
| Tipografi | `titleLarge` |

---

### DomatGhostButton

**Kullanım:** Transparan/ghost aksiyonlar (atla, daha sonra gibi)

```kotlin
fun DomatGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
```

| Özellik | Değer |
|---------|-------|
| Yükseklik | 56dp |
| Köşe yarıçapı | 12dp |
| Arka plan | `DomatColors.surfaceDefault` (transparan görünür) |
| Border | 0dp genişlik (görünmez) |
| Metin | `DomatColors.textPrimary` |
| Tipografi | `titleLarge` + `FontWeight.SemiBold` |

---

### DomatIconButton

**Kullanım:** İkon butonlar (geri, kapat, favori gibi)

```kotlin
enum class DomatIconButtonSize { Large, Medium }

fun DomatIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: DomatIconButtonSize = DomatIconButtonSize.Large,
)
```

| Boyut | Piksel |
|-------|--------|
| Large | 48×48dp |
| Medium | 40×40dp |

| Özellik | Değer |
|---------|-------|
| Şekil | CircleShape |
| Arka plan | `DomatColors.surfaceMuted` |
| İkon rengi | `DomatColors.textPrimary` |
| Disabled arka plan | `DomatColors.borderDefault` |

---

### DomatGoogleSignInButton

**Kullanım:** Google ile giriş yapma butonu

```kotlin
fun DomatGoogleSignInButton(
    onClick: () -> Unit,
    iconPainter: Painter,
    text: String,
    modifier: Modifier = Modifier,
)
```

| Özellik | Değer |
|---------|-------|
| Yükseklik | 56dp (fillMaxWidth) |
| Köşe yarıçapı | 12dp |
| Arka plan | `DomatColors.surfaceDefault` |
| Border | 1dp `DomatColors.borderDefault` |
| Shadow | shadowElevation 1dp |
| İkon | CenterStart, padding start 20dp, 24×24dp |
| Metin | merkez hizalı, `titleMedium`, `textPrimary` |

---

## Badge'ler

### DomatBadge

**Kullanım:** Kategori/durum etiketleri

```kotlin
enum class DomatBadgeVariant { Primary, Warning, Error, Dark, Info, Success }

fun DomatBadge(
    text: String,
    variant: DomatBadgeVariant = DomatBadgeVariant.Primary,
    modifier: Modifier = Modifier,
)
```

| Özellik | Değer |
|---------|-------|
| Şekil | CircleShape |
| Padding | horizontal 8dp, vertical 4dp |
| Tipografi | `labelSmall` |

| Variant | Arka Plan | Metin |
|---------|-----------|-------|
| Primary | `primary` | `textPrimary` |
| Warning | `warning` | `textInverse` |
| Error | `error` | `textInverse` |
| Dark | `surfaceDark` | `textInverse` |
| Info | `infoLight` | `info` |
| Success | `successLight` | `success` |

---

### DomatHeroBadge

**Kullanım:** Hero alanı için ikon + metin pill badge (onboarding, landing)

```kotlin
fun DomatHeroBadge(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
)
```

| Özellik | Değer |
|---------|-------|
| Şekil | RoundedCornerShape(9999dp) — pill |
| Arka plan | `DomatColors.primary20` |
| Border | 1dp `DomatColors.primary30` |
| Padding | horizontal 13dp, vertical 5dp |
| İkon boyutu | 13×13dp |
| Metin | `text.uppercase()`, `labelLarge`, `primary` |
| Boşluk | İkon ile metin arası 8dp |

---

## Bar

### DomatBottomActionBar

**Kullanım:** Ekran altında sabit aksiyon barı (buton, fiyat özeti gibi içerik alır)

```kotlin
fun DomatBottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
```

| Özellik | Değer |
|---------|-------|
| Genişlik | fillMaxWidth |
| Arka plan | `DomatColors.surfaceDefault` |
| Üst border | 1dp `DomatColors.borderLight` |
| Padding | start 16dp, end 16dp, top 17dp, bottom 32dp |
| Layout | Column |

---

## Kartlar

### DomatProductCard

**Kullanım:** Ürün listesi kartı

```kotlin
fun DomatProductCard(
    name: String,
    price: String,
    description: String,
    imageContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
)
```

| Özellik | Değer |
|---------|-------|
| Genişlik | 163dp (sabit) |
| Görsel alanı | fillMaxWidth × 100dp |
| Köşe yarıçapı | 12dp |
| Arka plan | `DomatColors.surfaceDefault` |
| Border | 1dp `DomatColors.borderDefault` |
| İçerik padding | 8dp |
| Ad | `bodySmall` + Bold, `textPrimary`, maxLines 2 |
| Fiyat | `titleMedium`, `textPrimary` |
| Açıklama | `labelSmall`, `textSecondary`, maxLines 1 |

---

### DomatSelectionCard

**Kullanım:** Seçilebilir kart (blok/daire seçimi)

```kotlin
fun DomatSelectionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

| Özellik | Değer |
|---------|-------|
| Boyut | 84×56dp |
| Köşe yarıçapı | 12dp |
| Arka plan | `DomatColors.surfaceDefault` |
| Border seçili | 2dp `DomatColors.primary` |
| Border seçili değil | 1dp `DomatColors.borderDefault` |
| Metin | `labelLarge`, `textPrimary`, merkez hizalı |

---

### DomatPaymentCard

**Kullanım:** Ödeme kartı listesi öğesi

```kotlin
fun DomatPaymentCard(
    cardNumber: String,
    cardInfo: String,
    logo: @Composable BoxScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
)
```

| Özellik | Değer |
|---------|-------|
| Boyut | fillMaxWidth × 72dp |
| Köşe yarıçapı | 12dp |
| Arka plan | `DomatColors.surfaceDefault` |
| Border | 1dp `DomatColors.borderDefault` |
| İçerik padding | horizontal 16dp |
| Logo alanı | 40×40dp Box |
| Kart numarası | `labelLarge`, `textPrimary` |
| Kart bilgisi | `bodySmall`, `textSecondary` |

---

### DomatLocationCard

**Kullanım:** Konum seçimi satırı (ilçe, mahalle, site — kilitli veya açık)

```kotlin
fun DomatLocationCard(
    label: String,
    value: String,
    checkmarkPainter: Painter,
    modifier: Modifier = Modifier,
    isLocked: Boolean = true,
    lockPainter: Painter? = null,
    cardAlpha: Float = 1f,
)
```

| Özellik | Değer |
|---------|-------|
| Genişlik | fillMaxWidth |
| Arka plan | `DomatColors.surfaceSubtle` |
| Köşe yarıçapı | 12dp |
| Padding | 17dp |
| Şeffaflık | `cardAlpha` (soluk görünüm için) |
| İkon kutusu | 40×40dp, radius 8dp, `primary20` bg |
| İkon boyutu | 14×14dp |
| Label | `label.uppercase()`, `labelLarge`, `textTertiary` |
| Değer | `titleLarge`, `textPrimary`, maxLines 1, ellipsis |
| Kilit ikonu | 13×17dp, sadece `isLocked && lockPainter != null` ise gösterilir |

---

### DomatLocationCardConnector

**Kullanım:** İki `DomatLocationCard` arasındaki dikey bağlantı çizgisi

```kotlin
fun DomatLocationCardConnector(modifier: Modifier = Modifier)
```

| Özellik | Değer |
|---------|-------|
| Boyut | 2dp genişlik × 24dp yükseklik |
| Padding start | 36dp (kart ikonuyla hizalı) |
| Renk | `DomatColors.borderDefault` |

---

## Header

### DomatScreenHeader

**Kullanım:** Sayfa üst başlık barı (geri butonu + başlık, opsiyonel alt içerik)

```kotlin
fun DomatScreenHeader(
    title: String,
    onBackClick: () -> Unit,
    backIconPainter: Painter,
    modifier: Modifier = Modifier,
    bottomContent: @Composable (() -> Unit)? = null,
)
```

| Özellik | Değer |
|---------|-------|
| Genişlik | fillMaxWidth |
| Arka plan | `DomatColors.surfaceDefault` |
| Border | 1dp `DomatColors.borderLight` |
| Geri butonu | 48×48dp CircleShape, ikon 16×16dp |
| Başlık | `headlineSmall`, `textPrimary`, TextAlign.Center |
| Başlık padding | `end = 48dp` (geri buton genişliğini dengeler) |
| Row padding | start/end 16dp, top 16dp, bottom 8dp |
| `bottomContent` | Opsiyonel slot — `DomatProgressSteps` gibi |

---

## Göstergeler (Indicators)

### DomatProgressDots

**Kullanım:** Pager nokta göstergesi (onboarding gibi sayfalı içerikler)

```kotlin
fun DomatProgressDots(
    totalDots: Int,
    activeIndex: Int,
    modifier: Modifier = Modifier,
)
```

| Özellik | Değer |
|---------|-------|
| Aktif nokta | 24×6dp, `DomatColors.primary`, pill |
| Pasif nokta | 6×6dp, `DomatColors.borderLight`, pill |
| Animasyon | `animateDpAsState` spring (dampingRatio 0.6, stiffness 500) |
| Renk animasyon | `animateColorAsState` tween 200ms |
| Noktalar arası | 8dp |

---

### DomatProgressSteps

**Kullanım:** Adım göstergesi (kayıt akışı, çok adımlı formlar)

```kotlin
fun DomatProgressSteps(
    totalSteps: Int,
    currentStep: Int,   // 0-indexed
    modifier: Modifier = Modifier,
)
```

| Durum | Görünüm |
|-------|---------|
| Tamamlanmış (`index < currentStep`) | 6×6dp daire, `primary` |
| Aktif (`index == currentStep`) | 32×6dp pill, `primary`, spring animasyon |
| Bekleyen (`index > currentStep`) | 6×6dp daire, `borderDefault` |

| Özellik | Değer |
|---------|-------|
| Genişlik | fillMaxWidth |
| Dikey padding | 12dp |
| Adımlar arası | 12dp |

---

## Input

### DomatInputDropdown

**Kullanım:** Açılır seçim alanı (blok/daire seçimi)

```kotlin
fun DomatInputDropdown(
    label: String,
    value: String,
    iconPainter: Painter,
    chevronPainter: Painter,
    checkmarkPainter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isOpen: Boolean = false,
    items: List<String> = emptyList(),
    selectedItem: String? = null,
    onItemSelected: (String) -> Unit = {},
)
```

**Kapalı durum:**

| Özellik | Değer |
|---------|-------|
| Köşe yarıçapı | 12dp |
| Arka plan | `DomatColors.surfaceDefault` |
| Border aktif | 2dp `DomatColors.primary` |
| Border pasif | 1dp `DomatColors.borderDefault` |
| Shadow | shadowElevation 1dp |
| İkon | 16×16dp, aktif: `primary`, pasif: `textTertiary` |
| Label | `label.uppercase()`, `labelLarge`, `textTertiary` |
| Değer | `titleLarge`, `textPrimary` |
| Chevron | 12×12dp, 180° animasyon açıkken |
| Padding | aktif 18dp, pasif 17dp |

**Açık dropdown listesi:**

| Özellik | Değer |
|---------|-------|
| Konum | `zIndex(10f)`, `paddingTop = 88dp` |
| Shadow | elevation 8dp, alpha 0.1 |
| Border | 2dp `DomatColors.primary` |
| Seçili öğe bg | `DomatColors.primary10` |
| Seçili öğe checkmark | 16×16dp, `primary` rengi |
| Öğe tipografi | `bodyLarge` |
| Öğeler arası | 1dp `borderLight` divider |

---

### DomatTextField

**Kullanım:** Serbest metin girişi

```kotlin
fun DomatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String = "",
    isError: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
)
```

| Özellik | Değer |
|---------|-------|
| Köşe yarıçapı | 10dp |
| Tipografi | `bodyMedium` |
| Placeholder | `bodyMedium`, `textMuted` |
| Border odaklanmış | `DomatColors.primary` |
| Border odaklanmamış | `DomatColors.borderDefault` |
| Border hatalı | `DomatColors.error` |
| Border disabled | `DomatColors.borderLight` |
| Cursor | `DomatColors.primary` |
| Cursor hata | `DomatColors.error` |

---

## Liste

### DomatFeatureListItem

**Kullanım:** Özellik/bilgi listesi satırı (onboarding trust sayfası gibi)

```kotlin
fun DomatFeatureListItem(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
)
```

| Özellik | Değer |
|---------|-------|
| Genişlik | fillMaxWidth |
| Şekil | RoundedCornerShape(8dp) |
| Arka plan | `DomatColors.surfaceSubtle` |
| Border | 1dp `DomatColors.borderLight` |
| Padding | 13dp |
| İkon | 17×17dp |
| Metin | `bodySmall`, `textPrimary` |
| Boşluk | İkon ile metin arası 12dp |

---

## Tag

### DomatTag

**Kullanım:** Ürün veya içerik etiketleri (Yeni, İndirim vb.)

```kotlin
enum class DomatTagVariant { New, Sale, Discount, Limited }

fun DomatTag(
    text: String,
    variant: DomatTagVariant,
    modifier: Modifier = Modifier,
)
```

| Özellik | Değer |
|---------|-------|
| Şekil | RoundedCornerShape(4dp) |
| Padding | horizontal 6dp, vertical 4dp |
| Tipografi | `labelSmall` |

| Variant | Arka Plan | Metin |
|---------|-----------|-------|
| New | `primary` | `textPrimary` |
| Sale | `warning` | `textInverse` |
| Discount | `error` | `textInverse` |
| Limited | `surfaceDark` | `textInverse` |

---

## Kurallar

- Tüm renkler `colorResource(DomatColors.*)` ile alınır — `MaterialTheme.colorScheme.*` **kullanılmaz**
- `Painter` parametreleri `MR.images.*` ile beslenir — `Res.drawable.*` **kullanılmaz**
- Variant/durum renkleri kod içinde `when(variant)` ile türetilir — hardcoded `Color(0x...)` **kullanılmaz**
- Alt composable'lar `UiModel` data class alır, ham primitifler almaz
- `UiModel` içinde `Color`, `FontWeight` gibi Compose tipleri bulunmaz — variant/enum üzerinden türetilir
