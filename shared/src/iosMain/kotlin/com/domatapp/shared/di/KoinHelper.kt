package com.domatapp.shared.di

import org.koin.core.component.KoinComponent

fun doInitKoin() = initKoin {}

/**
 * Helper class for accessing Koin dependencies from iOS/Swift.
 * Swift cannot call Koin's reified inline functions directly,
 * so this class provides typed accessors.
 *
 * Usage from Swift:
 * ```swift
 * let helper = KoinHelper()
 * let authViewModel = helper.authViewModel()
 * ```
 */
class KoinHelper : KoinComponent {
    fun loginViewModel() =
        getKoin().get<com.domatapp.feature.auth.presentation.login.LoginViewModel>()
}
