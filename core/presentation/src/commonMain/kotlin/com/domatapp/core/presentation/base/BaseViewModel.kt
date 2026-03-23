package com.domatapp.core.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI pattern.
 * Extends Lifecycle ViewModel for KMP support with automatic lifecycle management.
 *
 * @param UiState The state type that represents UI state
 * @param Intent The intent type that represents user actions
 * @param Effect The effect type that represents one-time events/side effects
 */
abstract class BaseViewModel<UiState : Any, Intent : Any, Effect : Any>(
    initialState: UiState
) : ViewModel() {
    // UI State (StateFlow for state management)
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<UiState> = _state.asStateFlow()

    // One-time effects (Channel for side effects like navigation, show toast)
    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /**
     * Handle user intents.
     * Override this in concrete ViewModels to implement intent handling.
     */
    abstract fun onIntent(intent: Intent)

    /**
     * Update the UI state.
     * Call this from concrete ViewModels to update state.
     */
    protected fun updateState(reducer: UiState.() -> UiState) {
        _state.value = reducer(_state.value)
    }

    /**
     * Emit a one-time effect/side effect.
     * Call this from concrete ViewModels to trigger side effects.
     */
    protected fun emitEffect(effect: Effect) = viewModelScope.launch {
        _effect.send(effect)
    }

    /**
     * Get current state value.
     */
    protected val currentState: UiState
        get() = _state.value
}
