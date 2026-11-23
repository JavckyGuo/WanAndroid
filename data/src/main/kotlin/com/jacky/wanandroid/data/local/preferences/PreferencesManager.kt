package com.jacky.wanandroid.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.jacky.wanandroid.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
/**
 * 管理设备级轻量偏好，不保存明文密码或完整用户资料。
 *
 * 登录状态只是本地 UI 摘要，真实会话仍以加密 Cookie 和服务端响应为准。
 */
class PreferencesManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private val THEME_KEY = stringPreferencesKey("theme")
    private val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
    private val LOGGED_IN_KEY = booleanPreferencesKey("logged_in")
    private val SEARCH_HISTORY_KEY = stringSetPreferencesKey("search_history")

    val themeMode: Flow<ThemeMode> = dataStore.data.map {
        themeModeFromStorage(it[THEME_KEY])
    }
    val isOnboardingComplete: Flow<Boolean> = dataStore.data.map {
        it[ONBOARDING_COMPLETE_KEY] ?: false
    }
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[LOGGED_IN_KEY] ?: false }
    val searchHistory: Flow<List<String>> = dataStore.data.map {
        it[SEARCH_HISTORY_KEY]?.toList().orEmpty()
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_KEY] = mode.toStorageValue() }
    }

    suspend fun setOnboardingComplete(complete: Boolean) = dataStore.edit {
        it[ONBOARDING_COMPLETE_KEY] = complete
    }

    suspend fun setLoggedIn(value: Boolean) = dataStore.edit { it[LOGGED_IN_KEY] = value }

    /** 搜索历史去重后保留最近 10 条；集合仅用于轻量本地建议。 */
    suspend fun addSearchHistory(value: String) = dataStore.edit {
        val old = it[SEARCH_HISTORY_KEY].orEmpty().toList()
        it[SEARCH_HISTORY_KEY] =
            (listOf(value) + old.filterNot { item -> item == value }).take(10).toSet()
    }

    suspend fun clearSearchHistory() = dataStore.edit { it.remove(SEARCH_HISTORY_KEY) }
}

/** 未知或损坏的持久化值安全回退为跟随系统。 */
internal fun themeModeFromStorage(value: String?): ThemeMode = when (value?.lowercase()) {
    "light" -> ThemeMode.LIGHT
    "dark" -> ThemeMode.DARK
    else -> ThemeMode.SYSTEM
}

/** 使用稳定的小写字符串持久化，避免枚举展示名称变化影响兼容性。 */
internal fun ThemeMode.toStorageValue(): String = name.lowercase()
