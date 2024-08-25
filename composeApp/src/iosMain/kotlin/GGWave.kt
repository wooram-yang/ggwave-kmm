package com.example.ggwave_multiplatform

import kotlinx.cinterop.*

object IOSGGWave: GGWave {
    override var delegate: BaseCoreManager? = null

    @OptIn(ExperimentalForeignApi::class)
    override fun initNative() {
        memScoped {

        }
    }

    override fun processCaptureData(data: ShortArray) {}
    override fun sendMessage(message: String) {}

    override fun onNativeReceivedMessage(data: ByteArray) {
        delegate?.onNativeReceivedMessage(data)
    }

    override fun onNativeMessageEncoded(data: ShortArray) {
        delegate?.onNativeMessageEncoded(data)
    }
}

actual object GGWaveFactory {
    actual fun createInstance(): GGWave = IOSGGWave
}