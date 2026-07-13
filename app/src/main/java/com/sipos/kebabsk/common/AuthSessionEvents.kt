package com.sipos.kebabsk.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.atomic.AtomicBoolean

object AuthSessionEvents {
    private val _forceLogout = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceLogout: SharedFlow<Unit> = _forceLogout
    private val hasNotifiedLogout = AtomicBoolean(false)

    fun notifyForceLogout() {
        if (hasNotifiedLogout.compareAndSet(false, true)) {
            _forceLogout.tryEmit(Unit)
        }
    }
    
    fun resetLogoutState() {
        hasNotifiedLogout.set(false)
    }
}

