package com.domatapp.app

import com.domatapp.core.navigation.Route

sealed interface MainIntent {
    data class Navigate(val route: Route) : MainIntent
    data object PopBack : MainIntent
    data class PopBackTo(val route: Route, val inclusive: Boolean = false) : MainIntent
    data class ReplaceAll(val route: Route) : MainIntent
}