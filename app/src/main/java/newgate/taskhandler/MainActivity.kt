package newgate.taskhandler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

internal class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                val getLocalTimeTask = mainViewModel.getLocalTimeTask
                val getLocalTimeState by getLocalTimeTask.stateFlow.collectAsStateWithLifecycle()

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                ) { contentPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                            .padding(16.dp),
                    ) {
                        Row {
                            FilledIconButton(
                                onClick = {

                                    // reset (and cancel) the task
                                    getLocalTimeTask.reset()
                                },
                                enabled = getLocalTimeState?.isProgress == true,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.close_24),
                                    contentDescription = "Cancel",
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {

                                    // start the task
                                    getLocalTimeTask()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = getLocalTimeState == null,
                            ) {
                                Text(text = "Get local time")
                            }
                        }

                        getLocalTimeState?.onProgress { progress ->
                            val progressModifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp)

                            if (progress == null) {
                                CircularProgressIndicator(modifier = progressModifier)
                            } else {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = progressModifier,
                                )
                            }
                        }?.onCompleted { localTime ->
                            LaunchedEffect(localTime) {

                                // show the Snackbar indefinitely
                                snackbarHostState.showSnackbar(
                                    message = localTime.toString(),
                                    actionLabel = "Close",
                                )

                                // reset the task when the Snackbar is closed
                                getLocalTimeTask.reset()
                            }
                        }
                    }
                }
            }
        }
    }
}