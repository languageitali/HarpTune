package com.rosso.harptune

object AudioEngineBridge {
    init {
        System.loadLibrary("harptune-native")
    }

    external fun startEngine(): Boolean
    external fun stopEngine()

    // Extracción de F0
    external fun getLatestFrequency(): Float

    // Extracción de Centroide Espectral
    external fun getLatestBrightness(): Float

    // Extracción del buffer circular (Tensor de Audio de 500ms)
    external fun extractAudioTensor(): FloatArray
}