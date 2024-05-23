package com.barbosa.essentials.core

/**
 * Interface for defining error mappings.
 *
 * Implement this interface to provide custom mapping from various types of errors
 * to user-defined domain-specific exceptions.
 */
interface MapperExceptions {
    /**
     * Maps a throwable to a user-defined domain-specific exception.
     *
     * @param throwable The original throwable to map.
     * @return The mapped throwable.
     */
    fun map(throwable: Throwable): Throwable
}