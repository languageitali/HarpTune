#pragma once
#include <oboe/Oboe.h>
#include <atomic>
#include <vector>
#include <memory>
#include "FastYin.h"

class AudioEngine : public oboe::AudioStreamDataCallback {
public:
    AudioEngine();
    bool start();
    void stop();
    float getLatestFrequency();

    // Extensión de métricas espectrales
    float getLatestBrightness();

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) override;

private:
    std::shared_ptr<oboe::AudioStream> mStream;
    FastYin mFastYin;

    // Variables atómicas para lectura concurrente lock-free desde el hilo de la JVM
    std::atomic<float> mLatestFrequency;
    std::atomic<float> mLatestBrightness;

    std::vector<float> mSampleBuffer;
    const int WINDOW_SIZE = 2048;

    // Extracción de características en el dominio del tiempo
    float calculateTimbreBrightness(const float* buffer, int size);
};
