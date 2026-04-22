package com.sipos.kebabsk

import android.app.Application
import com.sipos.kebabsk.common.AppSessionStore

class SiposKebabApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppSessionStore.initialize(this)
    }
}

