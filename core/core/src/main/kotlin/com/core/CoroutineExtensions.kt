package com.core

import kotlinx.coroutines.CancellationException

/**
 * Executes the given suspend block and wraps the result into Result<T>.
 * Any [CancellationException] will not be caught here (i.e., if the coroutine is cancelled, it will be rethrown),
 * while all other exceptions will be caught and returned as [Result.failure].
 *
 * @param block The suspend block whose result we want to obtain.
 * @return [Result.success] with the block’s return value, or [Result.failure] with the caught exception.
 */
suspend inline fun <T> runSuspendCatching(
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        // Directly invoke the suspend block and wrap its result
        Result.success(block())
    } catch (e: Throwable) {
        // If this is a coroutine cancellation exception, rethrow it
        // so that the parent coroutine can handle cancellation properly.
        if (e is CancellationException) {
            throw e
        }
        // Otherwise, return Result.failure wrapping the exception
        Result.failure(e)
    }
}
