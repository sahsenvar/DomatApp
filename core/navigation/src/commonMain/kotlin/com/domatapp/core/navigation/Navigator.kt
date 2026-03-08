package com.domatapp.core.navigation

interface Navigator {
    fun navigate(route: Route)
    fun popBack()
    fun popBackTo(route: Route, inclusive: Boolean = false)
    fun replaceAll(route: Route)
}
