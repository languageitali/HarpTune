package com.rosso.harptune.ml

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * Entidad de serialización para dataset de entrenamiento.
 * Almacena el vector de características y la etiqueta corregida.
 */
data class AudioTensorRecord(
    val timestamp: Long,
    val frequency: Float,
    val brightness: Float,
    val predictedHole: Int,
    val predictedIsBlow: Boolean,
    val correctedHole: Int,         // Etiqueta ground-truth provista en la UI
    val correctedIsBlow: Boolean
)

object TelemetryManager {

    private val recordsBuffer = mutableListOf<AudioTensorRecord>()
    private const val BUFFER_THRESHOLD = 50 // Volcado a disco cada 50 inferencias

    fun recordInference(record: AudioTensorRecord, context: Context) {
        synchronized(recordsBuffer) {
            recordsBuffer.add(record)
            if (recordsBuffer.size >= BUFFER_THRESHOLD) {
                flushToDisk(context)
            }
        }
    }

    private fun flushToDisk(context: Context) {
        val recordsCopy = synchronized(recordsBuffer) {
            val copy = recordsBuffer.toList()
            recordsBuffer.clear()
            copy
        }

        CoroutineScope(Dispatchers.IO).launch {
            // Persistencia local en caché de la aplicación (formato CSV/JSONL para TFLite)
            val file = File(context.cacheDir, "ml_dataset_pending.csv")
            recordsCopy.forEach { record ->
                val line = "${record.timestamp},${record.frequency},${record.brightness},${record.correctedHole},${record.correctedIsBlow}\n"
                file.appendText(line)
            }

            // TODO: Integración con Google Drive REST API.
            // Una vez que el archivo local alcanza > 5MB, se ejecuta un Worker (WorkManager)
            // que autentica vía GoogleSignInAccount y sube el archivo a un AppFolder oculto en Drive.
            // Este dataset será el input para el entrenamiento del modelo de red neuronal.
        }
    }
}