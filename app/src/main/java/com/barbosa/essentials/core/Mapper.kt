package com.barbosa.essentials.core

/**
 * Interface for mapping objects of type I to objects of type O.
 *
 * Implement this interface to provide custom mapping logic between two types.
 */
interface Mapper<I, O> {
    /**
     * Maps a single object of type I to an object of type O.
     *
     * @param input The object to map.
     * @return The mapped object.
     */
    fun map(input: I): O

    /**
     * Maps a list of objects of type I to a list of objects of type O.
     *
     * @param inputList The list of objects to map.
     * @return The list of mapped objects.
     */
    fun mapList(inputList: List<I>): List<O> {
        return inputList.map { map(it) }
    }
}