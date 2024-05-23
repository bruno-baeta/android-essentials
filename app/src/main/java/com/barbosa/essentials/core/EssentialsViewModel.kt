package com.barbosa.essentials.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * A base ViewModel class that provides state and action management.
 *
 * @param Action The type of actions that can be sent by this ViewModel.
 * @param State The type of state managed by this ViewModel.
 * @param initialState The initial state of the ViewModel.
 */
open class EssentialsViewModel<Action, State>(initialState: State) : ViewModel() {

    private val _action = MutableSharedFlow<Action>()

    /**
     * A [SharedFlow] of actions that can be observed by the UI.
     */
    val action: SharedFlow<Action> = _action.asSharedFlow()

    private val _state = MutableStateFlow(initialState)

    /**
     * A [StateFlow] of the current state that can be observed by the UI.
     */
    val state: StateFlow<State> = _state

    /**
     * Sends an action to be observed by the UI.
     *
     * This method emits the given action to the [_action] [MutableSharedFlow],
     * which can be collected by the UI to react to actions.
     *
     * @param action The action to send.
     */
    protected fun sendAction(action: Action) {
        viewModelScope.launch {
            _action.emit(action)
        }
    }

    /**
     * Updates the state managed by the ViewModel.
     *
     * This method updates the current state using the provided update function.
     * The new state is then emitted to the [_state] [MutableStateFlow], which can be
     * collected by the UI to react to state changes.
     *
     * @param update A function that takes the current state and returns the updated state.
     */
    protected fun updateState(update: (State) -> State) {
        _state.value = update(_state.value)
    }
}