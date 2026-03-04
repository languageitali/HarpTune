#include <jni.h>
#include <vector>
#include <cmath>
#include <numeric>

extern "C" JNIEXPORT jfloat JNICALL
Java_com_rosso_harptune_PitchDetector_detectPitch(JNIEnv* env, jobject thiz, jshortArray audio_data) {
    jsize len = env->GetArrayLength(audio_data);
    jshort* buffer = env->GetShortArrayElements(audio_data, nullptr);

    // Algoritmo de Autocorrelación Normalizada (Baja Latencia)
    int sampleRate = 44100;
    int minFreq = 50;
    int maxFreq = 1100;
    int minPeriod = sampleRate / maxFreq;
    int maxPeriod = sampleRate / minFreq;

    float maxCorr = -1.0f;
    int bestPeriod = -1;

    for (int period = minPeriod; period <= maxPeriod; period++) {
        float corr = 0;
        float norm = 0;
        for (int i = 0; i < len - period; i++) {
            corr += (float)buffer[i] * (float)buffer[i + period];
        }
        if (corr > maxCorr) {
            maxCorr = corr;
            bestPeriod = period;
        }
    }

    env->ReleaseShortArrayElements(audio_data, buffer, JNI_ABORT);
    return (bestPeriod > 0) ? (float)sampleRate / (float)bestPeriod : 0.0f;
}