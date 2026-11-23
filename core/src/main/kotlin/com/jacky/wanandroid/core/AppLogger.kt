package com.jacky.wanandroid.core

import android.util.Log

object AppLogger {
    private const val TAG = "AppLogger"

    fun d(tag: String = TAG, message: String) = Log.d(tag, message)
    fun i(tag: String = TAG, message: String) = Log.i(tag, message)
    fun w(tag: String = TAG, message: String, throwable: Throwable? = null) =
        Log.w(tag, message, throwable)
    fun e(tag: String = TAG, message: String, throwable: Throwable? = null) =
        Log.e(tag, message, throwable)
}
