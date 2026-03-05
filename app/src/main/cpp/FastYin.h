#pragma once
#include <vector>
#include <cstdint>

class FastYin {
public:
    FastYin(int32_t sampleRate, int32_t bufferSize);
    float process(const float* audioBuffer, int32_t numFrames, float threshold = 0.15f);

private:
    int32_t mSampleRate;
    int32_t mBufferSize;
    std::vector<float> mYinBuffer;

    float parabolicInterpolation(int32_t tauEstimate);
};