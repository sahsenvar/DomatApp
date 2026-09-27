package com.domatapp.core.presentation.platform

import androidx.compose.runtime.Composable

actual abstract class PlatformContext

private object IosPlatformContext : PlatformContext()

@Composable
actual fun currentPlatformContext(): PlatformContext = IosPlatformContext
