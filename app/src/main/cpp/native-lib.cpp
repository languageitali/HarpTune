#include <jni.h>
#include "AudioEngine.h"

// Instancia estática del motor (Singleton)
static AudioEngine engine;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_rosso_harptune_AudioEngineBridge_startEngine(JNIEnv *env, jobject thiz) {
    return engine.start() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_rosso_harptune_AudioEngineBridge_stopEngine(JNIEnv *env, jobject thiz) {
engine.stop();
}

extern "C" JNIEXPORT jfloat JNICALL
        Java_com_rosso_harptune_AudioEngineBridge_getLatestFrequency(JNIEnv *env, jobject thiz) {
return engine.getLatestFrequency();
}