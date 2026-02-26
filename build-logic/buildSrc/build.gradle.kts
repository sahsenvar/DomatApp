plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
    // Burada projedeki TOML dosyasını okuyabilmesi veya convention plugin'lerin 
    // ortak kullanacağı dependency'leri (örn: detekt veya ktlint plugin id'leri) tutabilirsin.
}
