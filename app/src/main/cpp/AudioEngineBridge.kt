package com.rosso.harptune

object AudioEngineBridge {
    init {
        System.loadLibrary("harptune-native")
    }

    external fun startEngine(): Boolean
    external fun stopEngine()
    external fun getLatestFrequency(): Float

    // Binding de la nueva función de extracción espectral
    external fun getLatestBrightness(): Float
}