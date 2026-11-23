package com.jacky.wanandroid.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacky.wanandroid.domain.model.ThemeMode
import com.jacky.wanandroid.domain.usecase.ObserveThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
/** 将设备级主题 Flow 提升为应用根节点可直接收集的 StateFlow。 */
class ThemeViewModel @Inject constructor(
    observeThemeMode: ObserveThemeModeUseCase
) : ViewModel() {
    /** Eagerly 保证 Activity 首次组合时立即获得 DataStore 当前值。 */
    val themeMode = observeThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
}
