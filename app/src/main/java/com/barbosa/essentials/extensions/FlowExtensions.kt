package com.barbosa.essentials.extensions

import com.barbosa.essentials.core.MapperExceptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.Response

/**
 * Simplifies the creation of a flow that emits the result of a suspend function.
 *
 * @param block The suspend function whose result will be emitted by the flow.
 * @return A Flow that emits the result of the suspend function.
 */
fun <T> flowEmit(block: suspend () -> T): Flow<T> = flow {
    emit(block())
}

/**
 * Creates a Flow that emits Unit if the provided suspending block returns a successful Response.
 * Throws an HttpException if the Response is not successful.
 *
 * @param block A suspending block that returns a Response.
 * @return A Flow that emits Unit on a successful Response or throws an HttpException on failure.
 * @throws HttpException If the Response is not successful.
 */
fun flowEmptyResponse(block: suspend () -> Response<*>): Flow<Unit> = flow {
    val result = block()
    if (result.isSuccessful) {
        emit(Unit)
    } else {
        throw HttpException(result)
    }
}

/**
 * Extension to map various types of errors to user-defined domain-specific exceptions using a DataExceptionMapper.
 *
 * This function catches any exceptions thrown in the flow and uses the provided
 * DataExceptionMapper to map the exception to a domain-specific exception.
 *
 * @param mapper The DataExceptionMapper to use for mapping exceptions.
 * @return A Flow that maps exceptions using the provided DataExceptionMapper.
 */
fun <T> Flow<T>.parseDataExceptions(mapper: MapperExceptions): Flow<T> = catch { throwable ->
    throw mapper.map(throwable)
}