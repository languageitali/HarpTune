#pragma once
#include <vector>
#include <cstdint>

class FastYin {
public:
    // Constructor corregido: mBufferSize eliminado
    FastYin(int32_t sampleRate);
    float process(const float* audioBuffer, int32_t numFrames, float threshold = 0.15f);

private:
    int32_t mSampleRate;
    // int32_t mBufferSize; // CAMBIO: Eliminado para evitar warning
    std::vector<float> mYinBuffer;

    float parabolicInterpolation(int32_t tauEstimate);
};