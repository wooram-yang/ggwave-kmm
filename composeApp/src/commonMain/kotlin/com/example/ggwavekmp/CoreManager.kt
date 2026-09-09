package com.example.ggwavekmp

fun interface PlaySoundListener {
    fun onPlayEnded()
}

fun interface CaptureSoundListener {
    fun onReceivedMessage(value: String)
}

interface BaseCoreManager {
    var ggWave: GGWave
    var playSoundListener: PlaySoundListener
    var captureSoundListener: CaptureSoundListener
    var messageWillBeSent: String

    fun startCapturing()
    fun stopCapturing()
    fun capture()

    fun startPlayback()
    fun stopPlayback()
    fun play()
    fun playing()

    fun onNativeReceivedMessage(data: ByteArray)
    fun onNativeMessageEncoded(data: ShortArray)
}

expect object CoreManagerFactory {
    fun createInstance(): BaseCoreManager
}
