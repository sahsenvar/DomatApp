package com.domatapp.core.remote.rest

import cn.ktorfitx.multiplatform.core.config.KtorfitxConfig
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin

internal actual fun KtorfitxConfig.platformHttpClient(configure: HttpClientConfig<*>.() -> Unit) {
    httpClient(Darwin) { configure() }
}
