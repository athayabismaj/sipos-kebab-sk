package com.sipos.kebabsk

import android.app.Application
import com.sipos.kebabsk.common.AppSessionStore

import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.sipos.kebabsk.di.appModule

class SiposKebabApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppSessionStore.initialize(this)
        
        startKoin {
            androidContext(this@SiposKebabApplication)
            modules(appModule)
        }
    }
}
