package com.jacky.wanandroid.core.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import com.jacky.wanandroid.core.DispatcherProvider

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * 测试专用调度器提供者，让 IO、Main、Default 共用同一可控测试调度器。
 */
class FakeDispatcherProvider : DispatcherProvider {
    private val dispatcher = UnconfinedTestDispatcher()
    override val io = dispatcher
    override val main = dispatcher
    override val default = dispatcher
}
