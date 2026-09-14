# Compose Resources Bundling Fix — KMP Library Modules

## Problem

`com.android.kotlin.multiplatform.library` (KMP native Android library plugin) kullanılan feature
modüllerinde, CMP 1.10.x'in `CopyResourcesToAndroidAssetsTask` task'ı `outputDirectory`'yi otomatik
konfigüre edemiyor.

Bunun sonucunda feature modüllerindeki compose resource dosyaları (PNG, XML) APK'ya **bundlelanmıyor**
ve runtime'da `MissingResourceException` crash'i oluşuyor:

```
MissingResourceException: Missing resource with path:
composeResources/domatapp.feature.onboarding.presentation.generated.resources/drawable/img_welcome_neighborhood.png
```

---

## Root Cause

Standard `com.android.library` plugin ile çalışan CMP resources entegrasyonu, KMP native plugin
olan `com.android.kotlin.multiplatform.library` ile çalışmıyor.

CMP plugin, library variant'larını `LibraryExtension.libraryVariants` üzerinden bulmaya çalışıyor;
ancak KMP native plugin `KotlinMultiplatformAndroidLibraryExtension` kullanıyor. Bu API
uyumsuzluğu nedeniyle `copyAndroidMainComposeResourcesToAndroidAssets` task'ının `outputDirectory`
property'si hiçbir zaman set edilmiyor.

```
A problem was found with the configuration of task
':feature:onboarding:presentation:copyAndroidMainComposeResourcesToAndroidAssets'.
Type 'CopyResourcesToAndroidAssetsTask' property 'outputDirectory' doesn't have a configured value.
```

---

## Çözüm

Feature modüllerde kod üretimi (`compose.resources {}` bloğu) çalışmaya devam ediyor; sorun sadece
APK'ya kopyalama aşamasında. Bu yüzden **composeApp** modülünde, her feature modülünün hazırladığı
kaynakları doğru path yapısıyla APK'ya kopyalayan custom Gradle task'ları oluşturuldu.

### composeApp/build.gradle.kts

```kotlin
// Feature modüllerinin compose kaynaklarını bundle et
// (com.android.kotlin.multiplatform.library, CopyResourcesToAndroidAssetsTask'ı
//  otomatik konfigüre edemediği için manuel yapıyoruz)

val copyOnboardingResources = tasks.register<Copy>("copyOnboardingComposeResources") {
    dependsOn(":feature:onboarding:presentation:prepareComposeResourcesTaskForCommonMain")
    from(project(":feature:onboarding:presentation").layout.buildDirectory.dir(
        "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
    ))
    into(layout.buildDirectory.dir(
        "compose-feature-assets/composeResources/domatapp.feature.onboarding.presentation.generated.resources"
    ))
}

val copyAuthResources = tasks.register<Copy>("copyAuthComposeResources") {
    dependsOn(":feature:auth:presentation:prepareComposeResourcesTaskForCommonMain")
    from(project(":feature:auth:presentation").layout.buildDirectory.dir(
        "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
    ))
    into(layout.buildDirectory.dir(
        "compose-feature-assets/composeResources/domatapp.feature.auth.presentation.generated.resources"
    ))
}

android {
    // ...
    sourceSets {
        named("main") {
            assets.srcDirs("${layout.buildDirectory.get().asFile}/compose-feature-assets")
        }
    }
}

afterEvaluate {
    listOf("Debug", "Release").forEach { variant ->
        tasks.findByName("merge${variant}Assets")
            ?.dependsOn(copyOnboardingResources, copyAuthResources)
    }
}
```

---

## Yeni Feature Modülü Eklenince Yapılacaklar

Compose resource kullanan yeni bir feature modülü eklendiğinde şu adımları uygula:

### 1. Feature modülünün `build.gradle.kts`'ine ekle

```kotlin
// commonMain dependencies içine:
implementation(libs.compose.components.resources)

// Dosyanın sonuna:
compose.resources {
    publicResClass = false
    packageOfResClass = "domatapp.feature.<name>.presentation.generated.resources"
    generateResClass = always
}
```

### 2. Kaynak dosyaları ekle

```
feature/<name>/presentation/src/commonMain/composeResources/drawable/
```

### 3. `composeApp/build.gradle.kts`'e yeni copy task ekle

```kotlin
val copy<Name>Resources = tasks.register<Copy>("copy<Name>ComposeResources") {
    dependsOn(":<module>:prepareComposeResourcesTaskForCommonMain")
    from(project(":<module>").layout.buildDirectory.dir(
        "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
    ))
    into(layout.buildDirectory.dir(
        "compose-feature-assets/composeResources/domatapp.feature.<name>.presentation.generated.resources"
    ))
}
```

Ve `afterEvaluate` bloğuna ekle:
```kotlin
tasks.findByName("merge${variant}Assets")
    ?.dependsOn(copyOnboardingResources, copyAuthResources, copy<Name>Resources)
```

---

## packageOfResClass Neden Önemli?

`packageOfResClass`, hem üretilen `Res` sınıfının paketini hem de runtime kaynak yolunu belirliyor:

```kotlin
// Drawable0.commonMain.kt içinde üretilen:
private const val MD = "composeResources/<packageOfResClass>/"
```

Bu yol, APK'daki asset yoluyla eşleşmeli. Eğer screen dosyalarında şu importlar varsa:

```kotlin
import domatapp.feature.onboarding.presentation.generated.resources.Res
```

O zaman `packageOfResClass = "domatapp.feature.onboarding.presentation.generated.resources"` olmalı.

---

## Önemli Notlar

- `compose.resources {}` bloğu **kod üretimi** için gerekli; bundling için yeterli değil.
- `copyAndroidMainComposeResourcesToAndroidAssets` task'ı var ama KMP native plugin ile çalışmıyor.
- `prepareComposeResourcesTaskForCommonMain` task'ı kaynakları build dizinine hazırlıyor;
  bu hazır kaynakları biz manuel olarak APK'ya kopyalıyoruz.
- Feature modülünün `androidMain/res/drawable/` dizini bu süreçte kullanılmıyor; KMP native plugin
  `generateAndroidMainEmptyResourceFiles` ile boş R.txt üretiyor, R sınıfı generate etmiyor.
