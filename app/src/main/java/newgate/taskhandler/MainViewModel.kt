package newgate.taskhandler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import newgate.taskhandler.core.TaskHandler
import java.time.LocalTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MainViewModel : ViewModel() {

    private val getLocalTimeHandler = TaskHandler<Float, LocalTime>(viewModelScope)
    val getLocalTimeTaskStateFlow = getLocalTimeHandler.taskStateFlow

    fun getLocalTime(): Boolean {

        // launch the task and set an optional initial value [Task.Progress]
        // return true, if the task was launched successfully
        // return false, if the task has already launched and requires to reset
        return getLocalTimeHandler.handle(Float.NaN) {
            delay(2.seconds)

            // if necessary, update value [Task.Progress]
            var progress = 0F
            while (progress < 1F) {
                delay(20.milliseconds)
                progress += 0.01F
                emit(progress)
            }

            // return value [Task.Result]
            LocalTime.now()
        }
    }

    fun resetToGetLocalTime() {

        // reset (and cancel) the task to null
        getLocalTimeHandler.reset()
    }
}