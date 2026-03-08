package com.domatapp.core.presentation.compose

import androidx.compose.runtime.staticCompositionLocalOf
import com.domatapp.core.navigation.Navigator

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("No Navigator provided")
}
