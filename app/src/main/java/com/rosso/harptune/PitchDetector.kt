package com.rosso.harptune

import kotlin.math.abs

object PitchDetector {
    
    fun detectPitch(buffer: ShortArray, sampleRate: Int): Double {
        val size = buffer.size
        if (size < 1024) return -1.0

        // 1. Remove DC Offset (Subtract Mean)
        var mean = 0.0
        for (i in 0 until size) mean += buffer[i]
        mean /= size
        
        val cleanBuffer = DoubleArray(size)
        for (i in 0 until size) cleanBuffer[i] = buffer[i] - mean

        val minFreq = 80.0 
        val maxFreq = 2200.0 
        val minPeriod = (sampleRate / maxFreq).toInt()
        val maxPeriod = (sampleRate / minFreq).toInt().coerceAtMost(size / 2)
        
        val diff = DoubleArray(maxPeriod + 1)
        
        // 2. Difference function with normalization for signal energy
        for (tau in 1..maxPeriod) {
            var sum = 0.0
            val limit = size - tau
            for (i in 0 until limit) {
                val d = cleanBuffer[i] - cleanBuffer[i + tau]
                sum += d * d
            }
            diff[tau] = sum / limit
        }

        // 3. Cumulative Mean Normalized Difference Function (CMNDF)
        val cmndf = DoubleArray(maxPeriod + 1)
        cmndf[0] = 1.0
        var cumulativeSum = 0.0
        for (tau in 1..maxPeriod) {
            cumulativeSum += diff[tau]
            if (cumulativeSum != 0.0) {
                cmndf[tau] = diff[tau] / (cumulativeSum / tau)
            } else {
                cmndf[tau] = 1.0
            }
        }

        // 4. Absolute thresholding with a very strict value for "professional" precision
        // We look for the first dip that is significantly low
        val threshold = 0.10 
        var bestTau = -1
        
        for (tau in minPeriod..maxPeriod) {
            if (cmndf[tau] < threshold) {
                bestTau = tau
                // Refine: Find the exact local minimum in this valley
                var currentTau = tau
                while (currentTau + 1 < maxPeriod && cmndf[currentTau + 1] < cmndf[currentTau]) {
                    currentTau++
                }
                bestTau = currentTau
                break 
            }
        }

        // Fallback to global minimum if nothing is below threshold
        if (bestTau == -1) {
            var minVal = Double.MAX_VALUE
            for (tau in minPeriod..maxPeriod) {
                if (cmndf[tau] < minVal) {
                    minVal = cmndf[tau]
                    bestTau = tau
                }
            }
            // If the best dip isn't deep enough, it's probably noise or not a periodic sound
            if (minVal > 0.35) return -1.0
        }

        // 5. Parabolic Interpolation for sub-sample accuracy (essential for high frequencies)
        if (bestTau > 0 && bestTau < maxPeriod) {
            val y0 = cmndf[bestTau - 1]
            val y1 = cmndf[bestTau]
            val y2 = cmndf[bestTau + 1]
            
            val denom = y0 - 2 * y1 + y2
            val preciseTau = if (abs(denom) > 1e-9) {
                bestTau.toDouble() + (y0 - y2) / (2 * denom)
            } else {
                bestTau.toDouble()
            }
            return sampleRate.toDouble() / preciseTau
        }
        
        return sampleRate.toDouble() / bestTau
    }
}
