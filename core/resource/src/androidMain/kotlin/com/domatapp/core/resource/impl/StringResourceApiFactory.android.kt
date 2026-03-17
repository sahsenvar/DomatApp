package com.domatapp.core.resource.impl

import com.domatapp.core.resource.api.StringResourceApi

actual fun createStringResourceApi(): StringResourceApi = AndroidStringResourceApi()
