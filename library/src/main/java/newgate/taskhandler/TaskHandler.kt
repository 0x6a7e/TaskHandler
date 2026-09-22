package newgate.taskhandler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A thread-safe FSM for an asynchronous task with observable state.
 *
 * Implements [TaskResettable] to expose [stateFlow] and allow resetting the task back to Idle (`null`).
 *
 * ## State Machine
 *
 * The lifecycle of a task transitions through [Task] states as follows:
 * ```
 * null (Idle) -> Task.Progress -> (optional) Task.Completed -> null (Idle)
 * ```
 * - `null` (Idle): No task is running.
 * - [Task.Progress]: The task is currently running. The initial progress value is set immediately upon start.
 *   Intermediate progress updates emitted via [FlowCollector.emit] are set to [stateFlow] (conflated by [StateFlow]).
 *   If the task is reset while in [Task.Progress], it transitions directly back to `null` (Idle) without reaching [Task.Completed].
 * - [Task.Completed] *(optional)*: The task has finished execution. The completed value is retained until explicitly cleared via [reset].
 *
 * At most one task can run at a time.
 * The provided [handlerScope] must remain active while the [TaskHandler] is in use.
 *
 * @param P type of the progress value.
 * @param C type of the completed value.
 * @param handlerScope the [CoroutineScope] in which task jobs are launched.
 */
public class TaskHandler<P, C>(private val handlerScope: CoroutineScope) : TaskResettable<P, C> {

    /**
     * The observable state of the task as a [StateFlow]: `null` (Idle), [Task.Progress], or [Task.Completed].
     */
    override val stateFlow: StateFlow<Task<P, C>?>
        field = MutableStateFlow(null)

    private val lock = ReentrantLock()

    private var taskJob: Job? = null

    /**
     * Starts a new task if the [TaskHandler] is currently Idle (`null`).
     *
     * Sets [initialValue] as [Task.Progress] immediately before the task body starts executing.
     * The task body [block] executes within [handlerScope] (combined with [context]) and can emit intermediate
     * progress values using [emit][FlowCollector.emit].
     *
     * @param initialValue the initial value set as [Task.Progress] when the task starts.
     * @param context additional [CoroutineContext] combined with [handlerScope].
     * @param block suspendable task body that emits progress via [FlowCollector.emit] and returns the completed value.
     * @return `true` if the task was successfully started, `false` otherwise.
     */
    public fun handle(
        initialValue: P,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend FlowCollector<P>.() -> C,
    ): Boolean {
        return lock.withLock {
            if (stateFlow.value != null) return false

            val job = handlerScope.launch(
                context = context,
                start = CoroutineStart.LAZY,
            ) {

                fun set(task: Task<P, C>) {
                    lock.withLock {
                        ensureActive()
                        stateFlow.value = task
                    }
                }

                set(Task.Completed(block { value ->
                    set(Task.Progress(value))
                }))
            }
            taskJob = job
            stateFlow.value = Task.Progress(initialValue)
            job
        }.start()
    }

    /**
     * Cancels the currently running task (if any), resets [stateFlow] to `null` (Idle), and returns the reset state.
     *
     * @return the reset task state or `null` if already Idle.
     */
    override fun reset(): Task<P, C>? {
        return lock.withLock {
            taskJob?.cancel()
            taskJob = null

            val resetTask = stateFlow.value
            stateFlow.value = null
            resetTask
        }
    }
}

/**
 * Convenience [handle] overload for tasks with [Unit] progress.
 *
 * @param context additional [CoroutineContext] combined with [handlerScope].
 * @param block suspendable task body returning the completed value.
 * @return `true` if the task was successfully started, `false` otherwise.
 */
public inline fun <C> TaskHandler<Unit, C>.handle(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend () -> C,
): Boolean {
    return handle(
        initialValue = Unit,
        context = context,
    ) {
        block()
    }
}