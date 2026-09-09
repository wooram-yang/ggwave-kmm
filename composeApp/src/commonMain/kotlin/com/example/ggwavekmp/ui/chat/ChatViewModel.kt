package com.example.ggwavekmp.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ggwavekmp.BaseCoreManager
import com.example.ggwavekmp.CaptureSoundListener
import com.example.ggwavekmp.CoreManagerFactory
import com.example.ggwavekmp.PlaySoundListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val coreManager: BaseCoreManager = CoreManagerFactory.createInstance(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var nextMessageId = 0L

    init {
        coreManager.playSoundListener = PlaySoundListener { onPlayEnded() }
        coreManager.captureSoundListener = CaptureSoundListener { message ->
            onMessageReceived(message)
        }
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(inputText = value) }
    }

    fun onSendClick() {
        val state = _uiState.value
        val message = state.inputText.trim()
        if (message.isEmpty() || state.isSending) return

        if (state.isCapturing) {
            stopCapture()
        }

        val outgoing = ChatMessage(
            id = nextMessageId++,
            text = message,
            sender = "You",
            isMine = true,
        )
        _uiState.update {
            it.copy(
                messages = it.messages + outgoing,
                inputText = "",
                isSending = true,
            )
        }

        coreManager.messageWillBeSent = message
        coreManager.startPlayback()
    }

    fun onToggleCapture() {
        if (_uiState.value.isCapturing) {
            stopCapture()
        } else {
            startCapture()
        }
    }

    override fun onCleared() {
        if (_uiState.value.isCapturing) {
            coreManager.stopCapturing()
        }
        if (_uiState.value.isSending) {
            coreManager.stopPlayback()
        }
    }

    private fun startCapture() {
        _uiState.update { it.copy(isCapturing = true) }
        coreManager.startCapturing()
    }

    private fun stopCapture() {
        _uiState.update { it.copy(isCapturing = false) }
        coreManager.stopCapturing()
    }

    private fun onPlayEnded() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = false) }
        }
    }

    private fun onMessageReceived(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val incoming = ChatMessage(
                id = nextMessageId++,
                text = message,
                sender = "Someone",
                isMine = false,
            )
            _uiState.update { it.copy(messages = it.messages + incoming) }
        }
    }
}
