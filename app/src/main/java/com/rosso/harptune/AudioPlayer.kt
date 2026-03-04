package com.rosso.harptune

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

object AudioPlayer {
    fun playAudio(audioData: ShortArray, sampleRate: Int) {
        if (audioData.isEmpty()) return
        
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(audioData.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(audioData, 0, audioData.size, AudioTrack.WRITE_BLOCKING)
        audioTrack.play()
    }
}
