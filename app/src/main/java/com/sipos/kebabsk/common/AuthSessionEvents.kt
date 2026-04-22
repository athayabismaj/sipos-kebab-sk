package com.sipos.kebabsk.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object AuthSessionEvents {
    private val _forceLogout = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceLogout: SharedFlow<Unit> = _forceLogout

    fun notifyForceLogout() {
        _forceLogout.tryEmit(Unit)
    }
}

