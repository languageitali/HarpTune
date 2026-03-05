package com.rosso.harptune

object AudioEngineBridge {
    init {
        System.loadLibrary("harptune-native")
    }

    external fun startEngine(): Boolean
    external fun stopEngine()
    external fun getLatestFrequency(): Float
    external fun getLatestBrightness(): Float
    external fun extractAudioTensor(): FloatArray
    
    /**
     * Retorna la predicción del modelo TFLite.
     * @return IntArray [Hole (1-10), IsBlow (1=Soplar, 0=Aspirar), Confidence (0-100)]
     */
    external fun getAIPrediction(): IntArray
}
