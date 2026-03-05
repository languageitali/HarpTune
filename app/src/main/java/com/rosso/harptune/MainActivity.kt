package com.rosso.harptune

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rosso.harptune.ui.theme.DetectFreqTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                AudioEngineBridge.startEngine()
            } catch (e: UnsatisfiedLinkError) { }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAudioPermissions()
        setContent {
            DetectFreqTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HarmonicaScreen() // Se elimina pitchDetector
                }
            }
        }
    }

    private fun checkAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            try { AudioEngineBridge.startEngine() } catch (e: UnsatisfiedLinkError) { }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onStop() {
        super.onStop()
        try { AudioEngineBridge.stopEngine() } catch (e: UnsatisfiedLinkError) { }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { AudioEngineBridge.stopEngine() } catch (e: UnsatisfiedLinkError) { }
    }
}