package com.example.ggwave_multiplatform

interface GGWave {
    var delegate: BaseCoreManager?

    fun initNative()
    fun processCaptureData(data: ShortArray)
    fun sendMessage(message: String)

    fun onNativeReceivedMessage(data: ByteArray)
    fun onNativeMessageEncoded(data: ShortArray)
}

expect object GGWaveFactory {
    fun createInstance(): GGWave
}