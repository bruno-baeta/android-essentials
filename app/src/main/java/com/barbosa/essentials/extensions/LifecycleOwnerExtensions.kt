package com.barbosa.essentials.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Extension function to observe StateFlow from a LifecycleOwner.
 *
 * This function launches a coroutine in the lifecycle scope of the LifecycleOwner.
 * It collects the state from the provided StateFlow and triggers the
 * observer callback with the new state whenever it changes.
 *
 * @param stateFlow The StateFlow to observe.
 * @param observer The observer callback to trigger with the new state.
 */

fun <State> LifecycleOwner.observeState(
    stateFlow: StateFlow<State>,
    observer: (State) -> Unit
) {
    lifecycleScope.launch {
        stateFlow.collect { state ->
            observer(state)
        }
    }
}

/**
 * Extension function to observe SharedFlow from a LifecycleOwner.
 *
 * This function launches a coroutine in the lifecycle scope of the LifecycleOwner.
 * It collects the action from the provided SharedFlow and triggers the
 * observer callback with the new action whenever it is emitted.
 *
 * @param actionFlow The SharedFlow to observe.
 * @param observer The observer callback to trigger with the new action.
 */
fun <Action> LifecycleOwner.observeActions(
    actionFlow: SharedFlow<Action>,
    observer: (Action) -> Unit
) {
    lifecycleScope.launch {
        actionFlow.collect { action ->
            observer(action)
        }
    }
}