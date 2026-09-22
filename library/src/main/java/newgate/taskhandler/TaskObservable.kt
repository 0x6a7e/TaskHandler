package newgate.taskhandler

import kotlinx.coroutines.flow.StateFlow

/**
 * An interface for observing the state of a task via [stateFlow].
 *
 * Provides a read-only stream of [Task] states: `null` (Idle), [Task.Progress], or [Task.Completed].
 *
 * @param P type of the progress value.
 * @param C type of the completed value.
 */
public interface TaskObservable<out P, out C> {

    /**
     * The observable state of the task as a [StateFlow].
     */
    public val stateFlow: StateFlow<Task<P, C>?>
}

/**
 * Convenience property to access the current value of [TaskObservable.stateFlow].
 */
public inline val <P, C> TaskObservable<P, C>.value: Task<P, C>?
    get() = stateFlow.value