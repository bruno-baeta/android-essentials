package com.barbosa.essentials.core

/**
 * Interface for validating objects of type T.
 *
 * Implement this interface to provide custom validation logic for objects of type T.
 */
interface Validator<T> {
    /**
     * Validates a single object.
     *
     * @param value The object to validate.
     * @return True if the object is valid, false otherwise.
     */
    fun validate(value: T): Boolean

    /**
     * Validates a list of objects.
     *
     * @param values The list of objects to validate.
     * @return True if all objects in the list are valid, false otherwise.
     */
    fun validateList(values: List<T>): Boolean {
        return values.all { validate(it) }
    }
}