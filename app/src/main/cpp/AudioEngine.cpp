#include "AudioEngine.h"
#include <android/log.h>
#include <cmath>
#include <algorithm>

#define TAG "AudioEngine"

AudioEngine::AudioEngine(const char* modelPath) : mFastYin(48000, 2048), mLatestFrequency(-1.0f), mLatestBrightness(0.0f) {
    mSampleBuffer.reserve(WINDOW_SIZE * 2);
    mTensorBuffer.resize(TENSOR_BUFFER_SIZE, 0.0f);

    if (modelPath) {
        mModel = tflite::FlatBufferModel::BuildFromFile(modelPath);
        if (mModel) {
            tflite::ops::builtin::BuiltinOpResolver resolver;
            tflite::InterpreterBuilder(*mModel, resolver)(&mInterpreter);
            if (mInterpreter) {
                mInterpreter->AllocateTensors();
            }
        }
    }
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

float AudioEngine::getLatestBrightness() {
    return mLatestBrightness.load(std::memory_order_relaxed);
}

std::vector<float> AudioEngine::extractAudioTensor() {
    std::lock_guard<std::mutex> lock(mTensorMutex);
    std::vector<float> result(TENSOR_BUFFER_SIZE);
    int currentIdx = mTensorWriteIndex.load();
    
    // Devolver buffer rotado para que sea cronológico
    for (int i = 0; i < TENSOR_BUFFER_SIZE; ++i) {
        result[i] = mTensorBuffer[(currentIdx + i) % TENSOR_BUFFER_SIZE];
    }
    return result;
}

AudioEngine::AIPrediction AudioEngine::getLatestPrediction() {
    return { mLatestHole.load(), mLatestIsBlow.load(), mLatestConfidence.load() };
}

oboe::DataCallbackResult AudioEngine::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
    float *floatData = static_cast<float *>(audioData);

    // Actualizar Tensor Buffer Circular para Telemetría
    {
        std::lock_guard<std::mutex> lock(mTensorMutex);
        int idx = mTensorWriteIndex.load();
        for (int i = 0; i < numFrames; ++i) {
            mTensorBuffer[idx] = floatData[i];
            idx = (idx + 1) % TENSOR_BUFFER_SIZE;
        }
        mTensorWriteIndex.store(idx);
    }

    mSampleBuffer.insert(mSampleBuffer.end(), floatData, floatData + numFrames);

    if (mSampleBuffer.size() >= WINDOW_SIZE) {
        float brightness = calculateTimbreBrightness(mSampleBuffer.data(), WINDOW_SIZE);
        mLatestBrightness.store(brightness, std::memory_order_relaxed);

        float freq = mFastYin.process(mSampleBuffer.data(), WINDOW_SIZE);
        mLatestFrequency.store(freq, std::memory_order_relaxed);

        if (freq > 70.0f && mInterpreter) {
            runInference(freq, brightness, mSampleBuffer.data());
        }

        mSampleBuffer.erase(mSampleBuffer.begin(), mSampleBuffer.begin() + numFrames);
    }

    return oboe::DataCallbackResult::Continue;
}

float AudioEngine::calculateTimbreBrightness(const float* buffer, int size) {
    float sum_abs = 0.0f;
    float zero_crossings = 0.0f;
    for (int i = 1; i < size; ++i) {
        sum_abs += std::abs(buffer[i]);
        if ((buffer[i] > 0 && buffer[i-1] < 0) || (buffer[i] < 0 && buffer[i-1] > 0)) {
            zero_crossings += 1.0f;
        }
    }
    return (zero_crossings / (float)size) * (sum_abs / (float)size) * 1000.0f;
}

void AudioEngine::runInference(float f0, float brightness, const float* audioData) {
    float* input = mInterpreter->typed_input_tensor<float>(0);
    input[0] = f0;
    input[1] = brightness;
    // MFCC simplification for edge (using ZCR/Energy as spectral features in this version)
    for (int i = 2; i < 15; ++i) input[i] = audioData[i * 10]; 

    if (mInterpreter->Invoke() == kTfLiteOk) {
        float* output = mInterpreter->typed_output_tensor<float>(0);
        int maxClass = std::max_element(output, output + 20) - output;
        
        mLatestHole.store((maxClass / 2) + 1);
        mLatestIsBlow.store(maxClass % 2 == 0);
        mLatestConfidence.store(output[maxClass]);
    }
}
