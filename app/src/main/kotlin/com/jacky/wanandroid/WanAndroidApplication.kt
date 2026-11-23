package com.jacky.wanandroid

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
/** Hilt 应用入口，负责创建应用级依赖容器。 */
class WanAndroidApplication : Application()
