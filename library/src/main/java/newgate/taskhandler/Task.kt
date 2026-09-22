package newgate.taskhandler

import newgate.taskhandler.Task.Completed
import newgate.taskhandler.Task.Progress

/**
 * Represents the state of a task, which can be either [Progress] or [Completed].
 *
 * @param P type of the progress value.
 * @param C type of the completed value.
 */
public sealed interface Task<out P, out C> {

    /**
     * Holds an intermediate progress value of a running task.
     */
    @JvmInline
    public value class Progress<out P>(public val value: P) : Task<P, Nothing>

    /**
     * Holds the final value of a completed task.
     */
    @JvmInline
    public value class Completed<out C>(public val value: C) : Task<Nothing, C>

    public companion object {

        /**
         * Convenience instance for a [Progress] with [Unit] value.
         */
        public inline val PROGRESS: Progress<Unit>
            get() = Progress(Unit)

        /**
         * Convenience instance for a [Completed] with [Unit] value.
         */
        public inline val COMPLETED: Completed<Unit>
            get() = Completed(Unit)
    }
}

/**
 * Returns `true` if this task is [Progress].
 */
public inline val Task<*, *>.isProgress: Boolean
    get() = this is Progress

/**
 * Returns `true` if this task is [Completed].
 */
public inline val Task<*, *>.isCompleted: Boolean
    get() = this is Completed

/**
 * Returns the progress value if this is [Progress], or `null` otherwise.
 */
public fun <P> Task<P, *>.getProgressOrNull(): P? {
    return if (this is Progress) value else null
}

/**
 * Returns the completed value if this is [Completed], or `null` otherwise.
 */
public fun <C> Task<*, C>.getCompletedOrNull(): C? {
    return if (this is Completed) value else null
}

/**
 * Returns the progress value if this is [Progress], or [defaultValue] otherwise.
 */
public fun <P> Task<P, *>.getProgressOrDefault(defaultValue: P): P {
    return if (this is Progress) value else defaultValue
}

/**
 * Returns the completed value if this is [Completed], or [defaultValue] otherwise.
 */
public fun <C> Task<*, C>.getCompletedOrDefault(defaultValue: C): C {
    return if (this is Completed) value else defaultValue
}

/**
 * Returns the progress value if this is [Progress], or the result of [onCompleted] otherwise.
 */
public inline fun <P, C> Task<P, C>.getProgressOrElse(onCompleted: (value: C) -> P): P {
    return when (this) {
        is Progress -> value
        is Completed -> onCompleted(value)
    }
}

/**
 * Returns the completed value if this is [Completed], or the result of [onProgress] otherwise.
 */
public inline fun <P, C> Task<P, C>.getCompletedOrElse(onProgress: (value: P) -> C): C {
    return when (this) {
        is Progress -> onProgress(value)
        is Completed -> value
    }
}

/**
 * Performs [action] on the progress value if this is [Progress].
 */
public inline fun <P, C> Task<P, C>.onProgress(action: (value: P) -> Unit): Task<P, C> {
    if (this is Progress) {
        action(value)
    }
    return this
}

/**
 * Performs [action] on the completed value if this is [Completed].
 */
public inline fun <P, C> Task<P, C>.onCompleted(action: (value: C) -> Unit): Task<P, C> {
    if (this is Completed) {
        action(value)
    }
    return this
}

/**
 * Applies [onProgress] if this is [Progress], or [onCompleted] if this is [Completed], and returns the result.
 */
public inline fun <P, C, R> Task<P, C>.fold(
    onProgress: (value: P) -> R,
    onCompleted: (value: C) -> R,
): R {
    return when (this) {
        is Progress -> onProgress(value)
        is Completed -> onCompleted(value)
    }
}

/**
 * Returns a new [Task] with transformed progress value if this is [Progress], or keeps [Completed] unchanged.
 */
public inline fun <P, C, R> Task<P, C>.mapProgress(transform: (value: P) -> R): Task<R, C> {
    return when (this) {
        is Progress -> Progress(transform(value))
        is Completed -> this
    }
}

/**
 * Returns a new [Task] with transformed completed value if this is [Completed], or keeps [Progress] unchanged.
 */
public inline fun <P, C, R> Task<P, C>.mapCompleted(transform: (value: C) -> R): Task<P, R> {
    return when (this) {
        is Progress -> this
        is Completed -> Completed(transform(value))
    }
}

/**
 * Transforms progress value with [transform] returning a new [Task] if this is [Progress], or keeps [Completed] unchanged.
 */
public inline fun <P, C, R> Task<P, C>.flatMapProgress(transform: (value: P) -> Task<R, C>): Task<R, C> {
    return when (this) {
        is Progress -> transform(value)
        is Completed -> this
    }
}

/**
 * Transforms completed value with [transform] returning a new [Task] if this is [Completed], or keeps [Progress] unchanged.
 */
public inline fun <P, C, R> Task<P, C>.flatMapCompleted(transform: (value: C) -> Task<P, R>): Task<P, R> {
    return when (this) {
        is Progress -> this
        is Completed -> transform(value)
    }
}