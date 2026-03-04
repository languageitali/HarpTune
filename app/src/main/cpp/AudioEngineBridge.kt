package com.rosso.harptune

object AudioEngineBridge {
    init {
        System.loadLibrary("harptune_engine")
    }

    external fun startEngine(): Boolean
    external fun stopEngine()
    external fun getLatestFrequency(): Float
}