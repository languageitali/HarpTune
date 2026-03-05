#include "AudioEngine.h"
#include <android/log.h>
#include <cmath>

AudioEngine::AudioEngine() : mFastYin(48000, WINDOW_SIZE), mLatestFrequency(-1.0f) {
    mSampleBuffer.reserve(WINDOW_SIZE * 2);
}

bool AudioEngine::start() {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Input)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setFormat(oboe::AudioFormat::Float)
            ->setChannelCount(oboe::ChannelCount::Mono)
            ->setSampleRate(48000)
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

    mSampleBuffer.insert(mSampleBuffer.end(), floatData, floatData + numFrames);

    if (mSampleBuffer.size() >= WINDOW_SIZE) {
        float rms = 0.0f;
        for (int i = 0; i < WINDOW_SIZE; i++) {
            rms += mSampleBuffer[i] * mSampleBuffer[i];
        }
        rms = std::sqrt(rms / WINDOW_SIZE);

        if (rms > 0.01f) {
            float freq = mFastYin.process(mSampleBuffer.data(), WINDOW_SIZE);
            mLatestFrequency.store(freq, std::memory_order_relaxed);
        } else {
            mLatestFrequency.store(-1.0f, std::memory_order_relaxed);
        }

        mSampleBuffer.erase(mSampleBuffer.begin(), mSampleBuffer.begin() + numFrames);
    }

    return oboe::DataCallbackResult::Continue;
}