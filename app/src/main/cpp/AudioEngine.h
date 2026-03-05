#pragma once

#include <oboe/Oboe.h>
#include <tensorflow/lite/interpreter.h>
#include <tensorflow/lite/kernels/register.h>
#include <tensorflow/lite/model.h>
#include <atomic>
#include <vector>
#include <memory>
#include <mutex>
#include "FastYin.h"

class AudioEngine : public oboe::AudioStreamDataCallback {
public:
    AudioEngine(const char* modelPath = nullptr);
    bool start();
    void stop();
    float getLatestFrequency();
    float getLatestBrightness();
    std::vector<float> extractAudioTensor();

    struct AIPrediction {
        int hole;
        bool isBlow;
        float confidence;
    };
    AIPrediction getLatestPrediction();

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) override;

private:
    std::shared_ptr<oboe::AudioStream> mStream;
    FastYin mFastYin;
    std::atomic<float> mLatestFrequency;
    std::atomic<float> mLatestBrightness;

    std::unique_ptr<tflite::FlatBufferModel> mModel;
    std::unique_ptr<tflite::Interpreter> mInterpreter;
    std::atomic<int> mLatestHole{0};
    std::atomic<bool> mLatestIsBlow{true};
    std::atomic<float> mLatestConfidence{0.0f};

    std::vector<float> mSampleBuffer;
    const int WINDOW_SIZE = 2048;

    static const int TENSOR_BUFFER_SIZE = 24000;
    std::vector<float> mTensorBuffer;
    std::atomic<int> mTensorWriteIndex{0};
    std::mutex mTensorMutex;

    float calculateTimbreBrightness(const float* buffer, int size);
    void runInference(float f0, float brightness, const float* audioData);
};