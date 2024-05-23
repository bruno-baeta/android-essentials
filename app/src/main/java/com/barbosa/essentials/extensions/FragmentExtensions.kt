package com.barbosa.essentials.extensions

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Extension function to observe StateFlow from a Fragment.
 *
 * This function launches a coroutine in the lifecycle scope of the viewLifecycleOwner
 * of the Fragment. It collects the state from the provided StateFlow and triggers the
 * observer callback with the new state whenever it changes.
 *
 * @param stateFlow The StateFlow to observe.
 * @param observer The observer callback to trigger with the new state.
 */
fun <State> Fragment.observeState(
    stateFlow: StateFlow<State>,
    observer: (State) -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        stateFlow.collect { state ->
            observer(state)
        }
    }
}

/**
 * Extension function to observe SharedFlow from a Fragment.
 *
 * This function launches a coroutine in the lifecycle scope of the viewLifecycleOwner
 * of the Fragment. It collects the action from the provided SharedFlow and triggers the
 * observer callback with the new action whenever it is emitted.
 *
 * @param actionFlow The SharedFlow to observe.
 * @param observer The observer callback to trigger with the new action.
 */
fun <Action> Fragment.observeActions(
    actionFlow: SharedFlow<Action>,
    observer: (Action) -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        actionFlow.collect { action ->
            observer(action)
        }
    }
}


/**
 * Provides a lazy delegate to initialize a ViewBinding instance for a Fragment.
 *
 * @param T The type of the ViewBinding.
 * @param bindingInflater A lambda function that takes a LayoutInflater and returns an instance of the ViewBinding.
 * @return A lazy delegate to initialize the ViewBinding.
 */
inline fun <reified T : ViewBinding> Fragment.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) {
    bindingInflater.invoke(layoutInflater)
}