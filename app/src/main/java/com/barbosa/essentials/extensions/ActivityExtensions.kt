package com.barbosa.essentials.extensions

import android.app.Activity
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

/**
 * Provides a lazy delegate to initialize a ViewBinding instance for an Activity.
 *
 * @param T The type of the ViewBinding.
 * @param bindingInflater A lambda function that takes a LayoutInflater and returns an instance of the ViewBinding.
 * @return A lazy delegate to initialize the ViewBinding.
 */
inline fun <reified T : ViewBinding> Activity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) {
    bindingInflater.invoke(layoutInflater)
}