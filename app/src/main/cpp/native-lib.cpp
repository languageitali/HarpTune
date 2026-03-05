#include <jni.h>
#include <memory>
#include "AudioEngine.h"

// Estándar de Producción: Puntero inteligente global para manejar el ciclo de vida del Engine
static std::unique_ptr<AudioEngine> audio_engine = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_rosso_harptune_AudioEngineBridge_startEngine(JNIEnv *env, jobject thiz) {
    if (!audio_engine) {
        audio_engine = std::make_unique<AudioEngine>();
    }
    return audio_engine->start() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_rosso_harptune_AudioEngineBridge_stopEngine(JNIEnv *env, jobject thiz) {
    if (audio_engine) {
        audio_engine->stop();
        // Liberar memoria cuando la app pasa a background o se destruye
        audio_engine.reset();
    }
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_rosso_harptune_AudioEngineBridge_getLatestFrequency(JNIEnv *env, jobject thiz) {
    if (audio_engine) {
        return audio_engine->getLatestFrequency();
    }
    return -1.0f; // Retorna -1 si el motor no está inicializado
}