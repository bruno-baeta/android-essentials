package com.barbosa.essentials.core

/**
 * Composite validator that combines multiple validators for type T.
 *
 * @param validators The list of validators to combine.
 */
class CompositeValidator<T>(
    private val validators: List<Validator<T>>
) : Validator<T> {

    /**
     * Validates a single object using all combined validators.
     *
     * @param value The object to validate.
     * @return True if all validators return true, false otherwise.
     */
    override fun validate(value: T): Boolean {
        return validators.all { it.validate(value) }
    }
}