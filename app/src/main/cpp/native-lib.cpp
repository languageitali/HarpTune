#include <jni.h>
#include <memory>
#include <string>
#include "AudioEngine.h"

static std::unique_ptr<AudioEngine> audio_engine = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_rosso_harptune_AudioEngineBridge_startEngine(JNIEnv *env, jobject thiz) {
    if (!audio_engine) {
        // En producción, aquí se pasaría la ruta al archivo .tflite extraído de assets
        audio_engine = std::make_unique<AudioEngine>();
    }
    return audio_engine->start() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_rosso_harptune_AudioEngineBridge_stopEngine(JNIEnv *env, jobject thiz) {
    if (audio_engine) {
        audio_engine->stop();
        audio_engine.reset();
    }
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_rosso_harptune_AudioEngineBridge_getLatestFrequency(JNIEnv *env, jobject thiz) {
    if (audio_engine) {
        return audio_engine->getLatestFrequency();
    }
    return -1.0f;
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_rosso_harptune_AudioEngineBridge_getLatestBrightness(JNIEnv *env, jobject thiz) {
    if (audio_engine) {
        return audio_engine->getLatestBrightness();
    }
    return 0.0f;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_rosso_harptune_AudioEngineBridge_extractAudioTensor(JNIEnv *env, jobject thiz) {
    if (audio_engine) {
        std::vector<float> tensor = audio_engine->extractAudioTensor();
        jfloatArray result = env->NewFloatArray(tensor.size());
        env->SetFloatArrayRegion(result, 0, tensor.size(), tensor.data());
        return result;
    }
    return env->NewFloatArray(0);
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_rosso_harptune_AudioEngineBridge_getAIPrediction(JNIEnv *env, jobject thiz) {
    if (audio_engine) {
        auto pred = audio_engine->getLatestPrediction();
        jintArray result = env->NewIntArray(3);
        jint fill[3] = { pred.hole, pred.isBlow ? 1 : 0, (jint)(pred.confidence * 100) };
        env->SetIntArrayRegion(result, 0, 3, fill);
        return result;
    }
    jintArray empty = env->NewIntArray(3);
    jint zeros[3] = {0, 1, 0};
    env->SetIntArrayRegion(empty, 0, 3, zeros);
    return empty;
}
