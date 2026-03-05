#include "FastYin.h"
#include <cmath>

FastYin::FastYin(int32_t sampleRate, int32_t bufferSize)
        : mSampleRate(sampleRate), mBufferSize(bufferSize) {
    mYinBuffer.assign(bufferSize / 2, 0.0f);
}

float FastYin::process(const float* audioBuffer, int32_t numFrames, float threshold) {
    int32_t yinBufferSize = mYinBuffer.size();
    if (numFrames < yinBufferSize * 2) return -1.0f;

    for (int32_t tau = 0; tau < yinBufferSize; tau++) {
        float sum = 0.0f;
        for (int32_t i = 0; i < yinBufferSize; i++) {
            float delta = audioBuffer[i] - audioBuffer[i + tau];
            sum += delta * delta;
        }
        mYinBuffer[tau] = sum;
    }

    mYinBuffer[0] = 1.0f;
    float runningSum = 0.0f;
    int32_t bestTau = -1;

    for (int32_t tau = 1; tau < yinBufferSize; tau++) {
        runningSum += mYinBuffer[tau];
        mYinBuffer[tau] *= tau / runningSum;

        if (mYinBuffer[tau] < threshold) {
            while (tau + 1 < yinBufferSize && mYinBuffer[tau + 1] < mYinBuffer[tau]) {
                tau++;
            }
            bestTau = tau;
            break;
        }
    }

    if (bestTau == -1) return -1.0f;

    float preciseTau = parabolicInterpolation(bestTau);
    return mSampleRate / preciseTau;
}

float FastYin::parabolicInterpolation(int32_t tauEstimate) {
    if (tauEstimate < 1 || tauEstimate >= mYinBuffer.size() - 1) return (float)tauEstimate;

    float s0 = mYinBuffer[tauEstimate - 1];
    float s1 = mYinBuffer[tauEstimate];
    float s2 = mYinBuffer[tauEstimate + 1];

    float adjustment = (s2 - s0) / (2.0f * (2.0f * s1 - s2 - s0));
    return tauEstimate + adjustment;
}