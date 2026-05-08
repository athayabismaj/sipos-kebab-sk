package com.sipos.kebabsk.common.perf

import android.util.Log
import android.view.Window
import androidx.metrics.performance.JankStats

object JankMonitor {
    private const val TAG = "JankMonitor"

    fun attach(window: Window): JankStats? {
        return runCatching {
            JankStats.createAndTrack(window) { frameData ->
                if (!frameData.isJank) return@createAndTrack
                val frameMs = frameData.frameDurationUiNanos / 1_000_000.0
                Log.w(TAG, "Jank frame=${"%.2f".format(frameMs)}ms states=${frameData.states}")
            }
        }.getOrElse {
            Log.w(TAG, "Jank monitor disabled: ${it.message}")
            null
        }
    }
}
