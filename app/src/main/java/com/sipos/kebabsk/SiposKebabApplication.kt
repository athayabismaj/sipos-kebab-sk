package com.sipos.kebabsk

import android.app.Application
import com.sipos.kebabsk.common.AppSessionStore
import com.sipos.kebabsk.data.network.NetworkModule

class SiposKebabApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppSessionStore.initialize(this)
        NetworkModule.initialize(this)
    }
}
