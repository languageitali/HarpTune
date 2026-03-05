package com.rosso.harptune

object AudioEngineBridge {
    init {
        System.loadLibrary("harptune-native")
    }

    external fun startEngine(): Boolean
    external fun stopEngine()
    external fun getLatestFrequency(): Float
}
