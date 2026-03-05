#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <complex>

// Estándar C++23 para procesamiento numérico
extern "C" JNIEXPORT jfloat JNICALL
Java_com_rosso_harptune_HarmonicaMapper_processFrequency(
        JNIEnv* env,
        jobject /* this */,
        jfloatArray audio_data) {

    jsize len = env->GetArrayLength(audio_data);
    jfloat* data = env->GetFloatArrayElements(audio_data, nullptr);

    // Lógica de detección de pitch optimizada (ej. Autocorrelación o FFT)
    float magnitude_sum = 0.0f;
    for (int i = 0; i < len; ++i) {
        magnitude_sum += std::abs(data[i]);
    }

    env->ReleaseFloatArrayElements(audio_data, data, JNI_ABORT);
    return magnitude_sum / static_cast<float>(len);
}