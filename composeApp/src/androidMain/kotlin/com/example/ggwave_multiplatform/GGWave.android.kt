package com.example.ggwave_multiplatform

actual object GGWaveFactory {
    init {
        System.loadLibrary("ggwave")
    }

    actual fun createInstance(): GGWave = JVMGGWave
}