import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.ggwave_multiplatform.CoreManagerFactory

interface PlaySoundListener {
    fun onPlayEnded()
}

interface CaptureSoundListener {
    fun onReceivedMessage(value: String)
}

class AppViewModel: PlaySoundListener, CaptureSoundListener {
    private val coreManager = CoreManagerFactory.createInstance()

    var firstStatus by mutableStateOf("Idle")
    var firstButtonString by mutableStateOf("Send Message!")
    var secondStatus by mutableStateOf("Idle")
    var secondButtonString by mutableStateOf("Get Message!")
    var messageWillBeSent by mutableStateOf("Hello KMM")

    init {
        coreManager.playSoundListener = this
        coreManager.captureSoundListener = this
        coreManager.messageWillBeSent = messageWillBeSent
    }

    fun startCaptureSound() {
        secondStatus = "Recording"
        coreManager.startCapturing()
    }

    fun stopCaptureSound() {
        secondStatus = "Idle"
        coreManager.stopCapturing()
    }

    fun startPlaySound() {
        firstStatus = "Playing"
        coreManager.startPlayback()
    }

    fun stopPlaySound() {
        firstStatus = "Idle"
        coreManager.stopPlayback()
    }

    override fun onPlayEnded() {
        firstStatus = "Idle"
    }

    override fun onReceivedMessage(value: String) {
        secondStatus = value
    }
}