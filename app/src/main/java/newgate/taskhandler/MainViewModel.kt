package newgate.taskhandler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import newgate.taskhandler.task.GetLocalTimeTask

internal class MainViewModel : ViewModel() {

    val getLocalTimeTask = GetLocalTimeTask(viewModelScope)
}