package com.barbosa.essentials.core

/**
 * Interface for filtering objects of type T.
 *
 * Implement this interface to provide custom filtering logic for objects of type T.
 */
interface Filter<T> {
    /**
     * Filters a list of objects of type T.
     *
     * @param items The list of objects to filter.
     * @return The filtered list of objects.
     */
    fun filter(items: List<T>): List<T>
}
