package com.jacky.wanandroid.core

import android.util.Log

/**
 * 应用统一日志入口，集中约束日志标签与异常输出方式。
 *
 * 业务代码通过该对象记录诊断信息，避免直接散落调用 Android [Log]。
 */
object AppLogger {
    private const val TAG = "AppLogger"

    fun d(tag: String = TAG, message: String) = Log.d(tag, message)
    fun i(tag: String = TAG, message: String) = Log.i(tag, message)
    fun w(tag: String = TAG, message: String, throwable: Throwable? = null) =
        Log.w(tag, message, throwable)
    fun e(tag: String = TAG, message: String, throwable: Throwable? = null) =
        Log.e(tag, message, throwable)
}
