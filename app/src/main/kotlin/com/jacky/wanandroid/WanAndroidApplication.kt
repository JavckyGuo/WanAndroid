package com.jacky.wanandroid

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WanAndroidApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Add conditional init here for Firebase, etc. (see conditional templates section)
    }
}
