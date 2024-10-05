package newgate.taskhandler.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

sealed interface Task<out P, out R> {

    @JvmInline
    value class Progress<out P>(val value: P) : Task<P, Nothing> {

        companion object {

            operator fun invoke(): Progress<Unit> {
                return Progress(Unit)
            }
        }
    }

    @JvmInline
    value class Result<out R>(val value: R) : Task<Nothing, R> {

        companion object {

            operator fun invoke(): Result<Unit> {
                return Result(Unit)
            }
        }
    }
}

inline val Task<*, *>?.isProgress: Boolean
    get() = this is Task.Progress

inline val Task<*, *>?.isResult: Boolean
    get() = this is Task.Result

fun <P> Task<P, *>.getProgressOrNull(): P? {
    return if (this is Task.Progress) value
    else null
}

fun <R> Task<*, R>.getResultOrNull(): R? {
    return if (this is Task.Result) value
    else null
}

fun <P> Task<P, *>.getProgressOrDefault(defaultValue: P): P {
    return if (this is Task.Progress) value
    else defaultValue
}

fun <R> Task<*, R>.getResultOrDefault(defaultValue: R): R {
    return if (this is Task.Result) value
    else defaultValue
}

inline fun <P, R> Task<P, R>.getProgressOrElse(onResult: (value: R) -> P): P {
    return when (this) {
        is Task.Progress -> value

        is Task.Result -> {
            onResult(value)
        }
    }
}

inline fun <P, R> Task<P, R>.getResultOrElse(onProgress: (value: P) -> R): R {
    return when (this) {
        is Task.Progress -> {
            onProgress(value)
        }

        is Task.Result -> value
    }
}

inline fun <P, R> Task<P, R>.onProgress(action: (value: P) -> Unit): Task<P, R> {
    if (this is Task.Progress) {
        action(value)
    }
    return this
}

inline fun <P, R> Task<P, R>.onResult(action: (value: R) -> Unit): Task<P, R> {
    if (this is Task.Result) {
        action(value)
    }
    return this
}

inline fun <P, R, T> Task<P, R>.fold(
    onProgress: (value: P) -> T,
    onResult: (value: R) -> T,
): T {
    return when (this) {
        is Task.Progress -> {
            onProgress(value)
        }

        is Task.Result -> {
            onResult(value)
        }
    }
}

inline fun <P, R, P1> Task<P, R>.mapProgress(transform: (value: P) -> P1): Task<P1, R> {
    return when (this) {
        is Task.Progress -> {
            Task.Progress(transform(value))
        }

        is Task.Result -> this
    }
}

inline fun <P, R, R1> Task<P, R>.mapResult(transform: (value: R) -> R1): Task<P, R1> {
    return when (this) {
        is Task.Progress -> this

        is Task.Result -> {
            Task.Result(transform(value))
        }
    }
}

inline fun <P, R, P1, R1> Task<P, R>.map(
    transformProgress: (value: P) -> P1,
    transformResult: (value: R) -> R1,
): Task<P1, R1> {
    return when (this) {
        is Task.Progress -> {
            Task.Progress(transformProgress(value))
        }

        is Task.Result -> {
            Task.Result(transformResult(value))
        }
    }
}

suspend fun Flow<Task<*, *>?>.awaitNull() {
    filter { task ->
        task == null
    }.first()
}

suspend fun <P> Flow<Task<P, *>?>.awaitProgress(): P {
    return filterIsInstance<Task.Progress<P>>().first().value
}

suspend inline fun <P> Flow<Task<P, *>?>.awaitProgress(crossinline predicate: suspend (value: P) -> Boolean): P {
    return filterIsInstance<Task.Progress<P>>().filter { progress ->
        predicate(progress.value)
    }.first().value
}

suspend fun <R> Flow<Task<*, R>?>.awaitResult(): R {
    return filterIsInstance<Task.Result<R>>().first().value
}

suspend inline fun <R> Flow<Task<*, R>?>.awaitResult(crossinline predicate: suspend (value: R) -> Boolean): R {
    return filterIsInstance<Task.Result<R>>().filter { result ->
        predicate(result.value)
    }.first().value
}