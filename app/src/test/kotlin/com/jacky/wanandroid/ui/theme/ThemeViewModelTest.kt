package com.jacky.wanandroid.ui.theme

import com.jacky.wanandroid.core.util.MainDispatcherRule
import com.jacky.wanandroid.domain.model.ThemeMode
import com.jacky.wanandroid.domain.repository.SettingsRepository
import com.jacky.wanandroid.domain.usecase.ObserveThemeModeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
/** 验证应用根主题能够实时响应仓库中的外部偏好变化。 */
class ThemeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `root theme follows repository changes`() = runTest {
        // 准备可主动发射主题变化的内存仓库。
        val repository = FakeSettingsRepository()
        val viewModel = ThemeViewModel(ObserveThemeModeUseCase(repository))

        // 验证根状态依次跟随系统、深色和浅色偏好。
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
        repository.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.themeMode.value)
        repository.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, viewModel.themeMode.value)
    }
}

private class FakeSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(ThemeMode.SYSTEM)
    override val themeMode = state
    override suspend fun setThemeMode(mode: ThemeMode) {
        state.value = mode
    }
}
