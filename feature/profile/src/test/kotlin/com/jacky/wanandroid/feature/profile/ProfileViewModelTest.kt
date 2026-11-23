package com.jacky.wanandroid.feature.profile

import com.jacky.wanandroid.core.util.FakeDispatcherProvider
import com.jacky.wanandroid.core.util.MainDispatcherRule
import com.jacky.wanandroid.domain.model.ThemeMode
import com.jacky.wanandroid.domain.model.UserDashboard
import com.jacky.wanandroid.domain.model.UserProfile
import com.jacky.wanandroid.domain.model.CoinInfo
import com.jacky.wanandroid.domain.repository.AuthRepository
import com.jacky.wanandroid.domain.repository.SettingsRepository
import com.jacky.wanandroid.domain.repository.UserRepository
import com.jacky.wanandroid.domain.usecase.LoadDashboardUseCase
import com.jacky.wanandroid.domain.usecase.ObserveThemeModeUseCase
import com.jacky.wanandroid.domain.usecase.SetThemeModeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
/** 验证未登录场景下主题设置仍可观察并立即更新。 */
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `theme remains available while logged out and updates immediately`() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = ProfileViewModel(
            auth = FakeAuthRepository(),
            loadDashboard = LoadDashboardUseCase(FakeUserRepository()),
            observeThemeMode = ObserveThemeModeUseCase(settings),
            setThemeMode = SetThemeModeUseCase(settings),
            dispatchers = FakeDispatcherProvider(),
        )

        assertEquals(false, viewModel.state.value.loggedIn)
        assertEquals(ThemeMode.SYSTEM, viewModel.state.value.themeMode)
        viewModel.selectTheme(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.state.value.themeMode)
    }
}

private class FakeSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(ThemeMode.SYSTEM)
    override val themeMode = state
    override suspend fun setThemeMode(mode: ThemeMode) {
        state.value = mode
    }
}

private class FakeAuthRepository : AuthRepository {
    override val isLoggedIn = MutableStateFlow(false)
    override suspend fun login(username: String, password: String) = UserProfile(1, username)
    override suspend fun register(username: String, password: String, repeatedPassword: String) = UserProfile(1, username)
    override suspend fun logout() {
        isLoggedIn.value = false
    }
}

private class FakeUserRepository : UserRepository {
    override suspend fun dashboard() = UserDashboard(UserProfile(1, "tester"), CoinInfo())
}
