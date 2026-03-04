package com.rosso.harptune

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

@Composable
fun HarmonicaApp(pitchDetector: PitchDetector) {
    val context = LocalContext.current
    var frequency by remember { mutableFloatStateOf(0f) }
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasPermission) {
            LaunchedEffect(Unit) {
                val sampleRate = 44100
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
                val buffer = ShortArray(bufferSize)

                audioRecord.startRecording()
                while (true) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val detected = pitchDetector.detectPitch(buffer)
                        if (detected > 0) frequency = detected
                    }
                    delay(16)
                }
            }

            // Visualización Técnica
            Text(
                text = "${frequency.toInt()} Hz",
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            val noteInfo = HarmonicaMapper.mapFrequencyToNote(frequency)

            if (noteInfo != null) {
                Spacer(modifier = Modifier.height(24.dp))

                Icon(
                    imageVector = if (noteInfo.isDraw) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(150.dp),
                    tint = if (noteInfo.isDraw) Color(0xFF2196F3) else Color(0xFFF44336) // Azul Aspirar, Rojo Soplar
                )

                Text(
                    text = "CELDA ${noteInfo.cell}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = noteInfo.noteName,
                    fontSize = 48.sp,
                    color = if (noteInfo.isDraw) Color(0xFF2196F3) else Color(0xFFF44336)
                )
            }
        } else {
            Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }) {
                Text("Activar Micrófono")
            }
        }
    }
}