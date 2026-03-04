#include "AudioEngine.h"
#include <android/log.h>

AudioEngine::AudioEngine() : mFastYin(48000, 2048), mLatestFrequency(-1.0f) {}

bool AudioEngine::start() {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Input)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setFormat(oboe::AudioFormat::Float)
            ->setChannelCount(oboe::ChannelCount::Mono)
            ->setSampleRate(48000) // Estándar de hardware para baja latencia
            ->setDataCallback(this);

    oboe::Result result = builder.openStream(mStream);
    if (result != oboe::Result::OK) return false;

    result = mStream->requestStart();
    return result == oboe::Result::OK;
}

void AudioEngine::stop() {
    if (mStream) {
        mStream->requestStop();
        mStream->close();
        mStream.reset();
    }
}

float AudioEngine::getLatestFrequency() {
    return mLatestFrequency.load(std::memory_order_relaxed);
}

oboe::DataCallbackResult AudioEngine::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
    float *floatData = static_cast<float *>(audioData);

    // Calcular energía RMS para descartar silencio absoluto
    float rms = 0.0f;
    for (int i = 0; i < numFrames; i++) rms += floatData[i] * floatData[i];
    rms = sqrt(rms / numFrames);

    if (rms > 0.01f) {
        float freq = mFastYin.process(floatData, numFrames);
        mLatestFrequency.store(freq, std::memory_order_relaxed);
    } else {
        mLatestFrequency.store(-1.0f, std::memory_order_relaxed);
    }

    return oboe::DataCallbackResult::Continue;
}