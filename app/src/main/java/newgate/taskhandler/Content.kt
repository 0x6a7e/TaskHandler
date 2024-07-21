package newgate.taskhandler

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import newgate.taskhandler.core.isProgress
import newgate.taskhandler.core.onProgress
import newgate.taskhandler.core.onResult

@Composable
fun Content(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val getLocalTimeTask by mainViewModel.getLocalTimeTaskStateFlow.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp)
        ) {
            Row {
                FilledIconButton(
                    onClick = {
                        mainViewModel.resetToGetLocalTime()
                    },
                    enabled = getLocalTimeTask.isProgress,
                ) {
                    Icon(
                        Icons.Default.Close,
                        "Cancel",
                    )
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {

                        // check the success of the task launch
                        // because otherwise the button is disabled
                        check(mainViewModel.getLocalTime())
                    },
                    enabled = getLocalTimeTask == null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Get local time")
                }
            }

            getLocalTimeTask?.onProgress { progress ->
                val progressModifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                if (progress.isNaN()) {
                    CircularProgressIndicator(modifier = progressModifier)
                } else {
                    CircularProgressIndicator(
                        progress = {
                            progress
                        },
                        modifier = progressModifier,
                    )
                }
            }?.onResult { localTime ->
                LaunchedEffect(localTime) {

                    // suspend fun (show the Snackbar indefinitely)
                    snackbarHostState.showSnackbar(
                        localTime.toString(),
                        actionLabel = "Close",
                    )

                    // reset the task when the Snackbar is closed
                    mainViewModel.resetToGetLocalTime()
                }
            }
        }
    }
}