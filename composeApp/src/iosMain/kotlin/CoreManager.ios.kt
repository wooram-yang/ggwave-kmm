package com.example.ggwave_multiplatform

import CaptureSoundListener
import PlaySoundListener
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object IOSCoreManager: BaseCoreManager {
    override var ggWave: GGWave = GGWaveFactory.createInstance()
    override lateinit var playSoundListener: PlaySoundListener
    override lateinit var captureSoundListener: CaptureSoundListener

    override var messageWillBeSent: String = ""

    init {
        ggWave.delegate = this
        ggWave.initNative()

        initAudioTrack()
        initAudioRecord()
    }

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private const val sampleHz = 48000
    private var encodedDataArray: ShortArray? = null

    private const val recordingBufferSize = 4 * 1024
    private var willStopRecording = false
    private var decodedDataArray: ShortArray = ShortArray(recordingBufferSize / 2)


    @OptIn(ExperimentalForeignApi::class)
    override fun startCapturing() {
        capture()
    }

    override fun stopCapturing() {
        willStopRecording = true
    }

    override fun capture() {
        scope.launch {
            while (true) {
                if(willStopRecording) {
                    break
                }
            }

            willStopRecording = false
        }
    }

    override fun startPlayback() {
        ggWave.sendMessage(messageWillBeSent)
    }

    override fun stopPlayback() {}

    override fun play() {
        scope.launch {
            encodedDataArray?.let {
            }
        }
    }

    override fun playing() {
    }

    override fun onNativeReceivedMessage(data: ByteArray) {
//        val resultString = String(data)

        captureSoundListener.onReceivedMessage(data.toString())
    }

    override fun onNativeMessageEncoded(data: ShortArray) {
        encodedDataArray = data
        play()
    }

    private fun initAudioTrack() {
    }

    private fun initAudioRecord() {
    }
}

actual object CoreManagerFactory {
    actual fun createInstance(): BaseCoreManager = IOSCoreManager
}