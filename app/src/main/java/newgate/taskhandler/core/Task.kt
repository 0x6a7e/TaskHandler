@file:Suppress("NOTHING_TO_INLINE")

package newgate.taskhandler.core

sealed interface Task<out P, out R> {

    @JvmInline
    value class Progress<out P>(val value: P) : Task<P, Nothing>

    @JvmInline
    value class Result<out R>(val value: R) : Task<Nothing, R>

    companion object {

        val PROGRESS = Progress(Unit)

        val RESULT = Result(Unit)
    }
}

inline val Task<*, *>.isProgress: Boolean
    get() = this is Task.Progress

inline val Task<*, *>.isResult: Boolean
    get() = this is Task.Result

inline fun <P> Task<P, *>.getProgressOrNull(): P? {
    return if (this is Task.Progress) value
    else null
}

inline fun <R> Task<*, R>.getResultOrNull(): R? {
    return if (this is Task.Result) value
    else null
}

inline fun <P> Task<P, *>.getProgressOrDefault(defaultValue: P): P {
    return if (this is Task.Progress) value
    else defaultValue
}

inline fun <R> Task<*, R>.getResultOrDefault(defaultValue: R): R {
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

inline fun <P, R, T> Task<P, R>.mapProgress(transform: (value: P) -> T): Task<T, R> {
    return when (this) {
        is Task.Progress -> {
            Task.Progress(transform(value))
        }

        is Task.Result -> this
    }
}

inline fun <P, R, T> Task<P, R>.mapResult(transform: (value: R) -> T): Task<P, T> {
    return when (this) {
        is Task.Progress -> this

        is Task.Result -> {
            Task.Result(transform(value))
        }
    }
}

inline fun <P, R, T> Task<P, R>.flatMapProgress(transform: (value: P) -> Task<T, R>): Task<T, R> {
    return when (this) {
        is Task.Progress -> {
            transform(value)
        }

        is Task.Result -> this
    }
}

inline fun <P, R, T> Task<P, R>.flatMapResult(transform: (value: R) -> Task<P, T>): Task<P, T> {
    return when (this) {
        is Task.Progress -> this

        is Task.Result -> {
            transform(value)
        }
    }
}