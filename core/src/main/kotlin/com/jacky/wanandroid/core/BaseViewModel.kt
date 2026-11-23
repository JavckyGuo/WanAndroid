package com.jacky.wanandroid.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Feature ViewModel 的公共基类。
 *
 * 所有协程均使用注入的 [DispatcherProvider]，让生产环境和测试环境可以替换调度器，
 * 避免 ViewModel 直接依赖全局 [kotlinx.coroutines.Dispatchers]。
 */
abstract class BaseViewModel(
    protected val dispatchers: DispatcherProvider
) : ViewModel() {

    protected fun launchOnIO(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(dispatchers.io, block = block)

    protected fun launchOnMain(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(dispatchers.main, block = block)

    protected fun launchOnDefault(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(dispatchers.default, block = block)
}
