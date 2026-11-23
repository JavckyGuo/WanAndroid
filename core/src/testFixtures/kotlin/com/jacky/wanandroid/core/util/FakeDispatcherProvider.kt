package com.jacky.wanandroid.core.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import com.jacky.wanandroid.core.DispatcherProvider

@OptIn(ExperimentalCoroutinesApi::class)
class FakeDispatcherProvider : DispatcherProvider {
    private val dispatcher = UnconfinedTestDispatcher()
    override val io = dispatcher
    override val main = dispatcher
    override val default = dispatcher
}
