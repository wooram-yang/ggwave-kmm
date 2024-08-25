import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.PreComposeApp

val viewModel = AppViewModel()

@Composable
fun App() {
    PreComposeApp {
        MaterialTheme {
            var playSoundToggle by remember { mutableStateOf(false) }
            var recordToggle by remember { mutableStateOf(false) }

            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(viewModel.firstStatus)
                Text(viewModel.messageWillBeSent)
                Button(onClick = {
                    playSoundToggle = !playSoundToggle

                    togglePlaySound(playSoundToggle)
                }) {
                    Text(viewModel.firstButtonString)
                }
                Text(viewModel.secondStatus)
                Button(onClick = {
                    recordToggle = !recordToggle

                    toggleRecordSound(recordToggle)
                }) {
                    Text(viewModel.secondButtonString)
                }
            }
        }
    }
}

fun togglePlaySound(playSoundToggle: Boolean) {
    if (playSoundToggle) {
        viewModel.startPlaySound()
    } else {
        viewModel.stopPlaySound()
    }
}

fun toggleRecordSound(recordToggle: Boolean) {
    if (recordToggle) {
        viewModel.startCaptureSound()
    } else {
        viewModel.stopCaptureSound()
    }
}
