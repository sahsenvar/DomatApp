package com.domatapp.core.presentation.compose

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalScrollableState = staticCompositionLocalOf<ScrollableState?> {
    null
}