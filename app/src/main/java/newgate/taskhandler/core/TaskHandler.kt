package newgate.taskhandler.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class TaskHandler<P, R>(private val handlerScope: CoroutineScope) {

    private val taskMutableStateFlow = MutableStateFlow<Task<P, R>?>(null)
    val taskStateFlow = taskMutableStateFlow.asStateFlow()

    private var taskJob: Job? = null

    @Synchronized
    fun handle(
        initialValue: P,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        collect: suspend FlowCollector<P>.() -> R,
    ): Boolean {
        return if (taskMutableStateFlow.value == null) {
            taskMutableStateFlow.value = Task.Progress(initialValue)
            taskJob = handlerScope.launch(context = coroutineContext) {
                val emit = { task: Task<P, R> ->
                    synchronized(this@TaskHandler) {
                        ensureActive()
                        taskMutableStateFlow.value = task
                    }
                }
                emit(Task.Result(collect { value ->
                    emit(Task.Progress(value))
                }))
            }
            true
        } else false
    }

    @Synchronized
    fun reset() {
        taskJob?.cancel()
        taskJob = null
        taskMutableStateFlow.value = null
    }

    companion object {

        operator fun <R> invoke(handlerScope: CoroutineScope): TaskHandler<Unit, R> {
            return TaskHandler(handlerScope)
        }
    }
}

inline fun <R> TaskHandler<Unit, R>.handle(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline collect: suspend () -> R,
): Boolean {
    return handle(
        initialValue = Unit,
        coroutineContext = coroutineContext,
    ) {
        collect()
    }
}