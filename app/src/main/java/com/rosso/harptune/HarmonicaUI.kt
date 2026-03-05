package com.rosso.harptune

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.rosso.harptune.ml.TelemetryManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun HarmonicaScreen() {
    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (hasAudioPermission) {
        PitchDetectionComponent()
    } else {
        PermissionDeniedView(onGrant = { launcher.launch(Manifest.permission.RECORD_AUDIO) })
    }
}

@Composable
fun PitchDetectionComponent() {
    var detectedFrequency by remember { mutableStateOf(0.0) }
    var detectedBrightness by remember { mutableStateOf(0.0f) }
    var harmonicaAction by remember { mutableStateOf<HarmonicaAction?>(null) }
    var lastValidAction by remember { mutableStateOf<HarmonicaAction?>(null) }

    var isRecording by remember { mutableStateOf(false) }
    val recordedActions = remember { mutableStateListOf<HarmonicaAction>() }

    LaunchedEffect(Unit) {
        var lastRecordedAction: HarmonicaAction? = null

        while (isActive) {
            val freq = AudioEngineBridge.getLatestFrequency()
            val brightness = AudioEngineBridge.getLatestBrightness()

            if (freq > 70.0f) {
                detectedFrequency = freq.toDouble()
                detectedBrightness = brightness
                val action = HarmonicaMapper.getHarmonicaAction(detectedFrequency, brightness)

                if (action != null) {
                    harmonicaAction = action
                    lastValidAction = action

                    if (isRecording && (lastRecordedAction == null || action.hole != lastRecordedAction.hole || action.isBlow != lastRecordedAction.isBlow)) {
                        recordedActions.add(action)
                        lastRecordedAction = action
                    }
                }
            } else {
                harmonicaAction = null
            }

            delay(16)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Afinador de Armónica (C)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = lastValidAction?.noteName ?: "---",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "%.2f Hz".format(detectedFrequency), style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val currentAction = lastValidAction
            if (currentAction != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ActionDisplay(currentAction, isActive = harmonicaAction != null)
                    Spacer(modifier = Modifier.height(16.dp))
                    ValidationOverlay(
                        detectedAction = currentAction,
                        f0 = detectedFrequency.toFloat(),
                        brightness = detectedBrightness,
                        onDismiss = {}
                    )
                }
            } else {
                Text("Esperando sonido...", color = Color.Gray, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HarmonicaGrid(
            activeHole = lastValidAction?.hole,
            isBlow = lastValidAction?.isBlow ?: true,
            isCurrentlyActive = harmonicaAction != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { if (!isRecording) { recordedActions.clear() }; isRecording = !isRecording },
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary)
            ) { Text(if (isRecording) "DETENER" else "GRABAR NOTAS") }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = { recordedActions.clear(); lastValidAction = null; detectedFrequency = 0.0 },
                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            ) { Icon(Icons.Default.Refresh, contentDescription = "Clear") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Secuencia:", style = MaterialTheme.typography.labelLarge)
        LazyRow(
            modifier = Modifier.fillMaxWidth().height(64.dp).background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recordedActions) { action -> RecordedActionItem(action) }
        }
    }
}

@Composable
fun ValidationOverlay(
    detectedAction: HarmonicaAction,
    f0: Float,
    brightness: Float,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showCorrectionDialog by remember { mutableStateOf(false) }

    if (showCorrectionDialog) {
        CorrectionDialog(
            initialHole = detectedAction.hole,
            onCorrect = { hole, isBlow ->
                coroutineScope.launch {
                    val tensor = AudioEngineBridge.extractAudioTensor()
                    TelemetryManager.saveValidatedTensor(context, tensor, f0, brightness, hole, isBlow)
                    showCorrectionDialog = false
                    onDismiss()
                }
            },
            onCancel = { showCorrectionDialog = false }
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showCorrectionDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Corregir Fallo") }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val tensor = AudioEngineBridge.extractAudioTensor()
                        TelemetryManager.saveValidatedTensor(
                            context, tensor, f0, brightness, detectedAction.hole, detectedAction.isBlow
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Text("Confirmar Bien") }
        }
    }
}

@Composable
fun CorrectionDialog(initialHole: Int, onCorrect: (Int, Boolean) -> Unit, onCancel: () -> Unit) {
    var selectedHole by remember { mutableStateOf(initialHole) }
    var selectedBlow by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Corrección de Etiqueta") },
        text = {
            Column {
                Slider(
                    value = selectedHole.toFloat(),
                    onValueChange = { selectedHole = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8
                )
                Text("Celda seleccionada: $selectedHole", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedBlow, onClick = { selectedBlow = true })
                    Text("Soplar")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = !selectedBlow, onClick = { selectedBlow = false })
                    Text("Aspirar")
                }
            }
        },
        confirmButton = { Button(onClick = { onCorrect(selectedHole, selectedBlow) }) { Text("Guardar Muestra") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

@Composable
fun ActionDisplay(action: HarmonicaAction, isActive: Boolean) {
    val arrow = if (action.isBlow) "↑" else "↓"
    val actionText = if (action.isBlow) "SOPLAR" else "ASPIRAR"
    val baseColor = if (action.isBlow) Color(0xFF4CAF50) else Color(0xFF2196F3)
    val color = if (isActive) baseColor else baseColor.copy(alpha = 0.4f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = action.hole.toString(), fontSize = 120.sp, fontWeight = FontWeight.Black, color = if (isActive) Color.Black else Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = arrow, fontSize = 120.sp, fontWeight = FontWeight.Black, color = color)
        }
        Surface(color = color, shape = RoundedCornerShape(12.dp)) {
            Text(text = actionText, color = Color.White, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
}

@Composable
fun RecordedActionItem(action: HarmonicaAction) {
    val arrow = if (action.isBlow) "↑" else "↓"
    val color = if (action.isBlow) Color(0xFF4CAF50) else Color(0xFF2196F3)
    Box(modifier = Modifier.background(color, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = action.hole.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = arrow, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun HarmonicaGrid(activeHole: Int?, isBlow: Boolean, isCurrentlyActive: Boolean) {
    val holes = (1..10).toList()
    LazyVerticalGrid(columns = GridCells.Fixed(5), modifier = Modifier.height(100.dp), userScrollEnabled = false) {
        items(holes) { hole ->
            val isTarget = hole == activeHole
            val color = when {
                isTarget && isBlow -> if (isCurrentlyActive) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.2f)
                isTarget && !isBlow -> if (isCurrentlyActive) Color(0xFF2196F3) else Color(0xFF2196F3).copy(alpha = 0.2f)
                else -> Color.LightGray.copy(alpha = 0.4f)
            }
            Box(modifier = Modifier.padding(2.dp).height(44.dp).background(color, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                Text(text = hole.toString(), color = if (isTarget && isCurrentlyActive) Color.White else Color.Black, fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Normal, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PermissionDeniedView(onGrant: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Se requiere permiso de micrófono para funcionar.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onGrant) {
                Text("Conceder Permiso")
            }
        }
    }
}