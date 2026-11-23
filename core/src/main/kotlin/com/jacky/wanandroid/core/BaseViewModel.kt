package com.jacky.wanandroid.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
