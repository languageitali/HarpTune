package com.rosso.harptune.ml

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

object TelemetryManager {
    suspend fun saveValidatedTensor(
        context: Context,
        audioData: FloatArray,
        f0: Float,
        brightness: Float,
        correctHole: Int,
        correctIsBlow: Boolean
    ) = withContext(Dispatchers.IO) {
        if (audioData.isEmpty()) return@withContext

        val timestamp = System.currentTimeMillis()
        val dir = File(context.filesDir, "ml_tensors").apply { mkdirs() }
        val filename = "tensor_${timestamp}_${correctHole}_${if(correctIsBlow) "B" else "D"}.bin"
        val file = File(dir, filename)

        val buffer = ByteBuffer.allocate(4 + 4 + 4 + 1 + (audioData.size * 4)).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(audioData.size)
        buffer.putFloat(f0)
        buffer.putFloat(brightness)
        buffer.put((if(correctIsBlow) 1 else 0).toByte())
        audioData.forEach { buffer.putFloat(it) }

        file.writeBytes(buffer.array())
    }
}