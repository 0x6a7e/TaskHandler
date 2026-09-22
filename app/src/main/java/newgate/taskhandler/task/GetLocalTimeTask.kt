package newgate.taskhandler.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import newgate.taskhandler.TaskHandler
import newgate.taskhandler.TaskResettable
import java.time.LocalTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class GetLocalTimeTask(
    scope: CoroutineScope,
    private val handler: TaskHandler<Float?, LocalTime> = TaskHandler(scope),
) : TaskResettable<Float?, LocalTime> by handler {

    operator fun invoke(): Boolean {

        // launch the task and set the initial value [Task.Progress]
        // return true if the task was launched successfully
        // return false if the task has already been launched and requires reset
        return handler.handle(initialValue = null) {
            delay(2.seconds)

            // if necessary, update value [Task.Progress]
            for (i in 0..100) {
                emit(i / 100F)
                delay(20.milliseconds)
            }

            // return value [Task.Result]
            LocalTime.now()
        }
    }
}