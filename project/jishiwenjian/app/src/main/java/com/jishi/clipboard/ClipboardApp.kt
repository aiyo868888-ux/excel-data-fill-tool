package com.jishi.clipboard

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class ClipboardApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 初始化日志
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("即时剪贴板应用启动")
    }
}
