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
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) override;

private:
    std::shared_ptr<oboe::AudioStream> mStream;
    FastYin mFastYin;
    std::atomic<float> mLatestFrequency;

    std::vector<float> mSampleBuffer;
    const int WINDOW_SIZE = 2048;
};