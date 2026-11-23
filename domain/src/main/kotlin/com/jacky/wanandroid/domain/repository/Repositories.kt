package com.jacky.wanandroid.domain.repository

import com.jacky.wanandroid.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    suspend fun home(page: Int = 0): HomeContent
    suspend fun articles(categoryId: Long, page: Int = 0): Page<Article>
    suspend fun projects(categoryId: Long, page: Int = 1): Page<Article>
    suspend fun search(keyword: String, page: Int = 0): Page<Article>
}

interface DiscoveryRepository {
    suspend fun categories(): List<Category>
    suspend fun navigation(): List<NavigationGroup>
    suspend fun projectCategories(): List<ProjectCategory>
    suspend fun hotKeys(): List<HotKey>
    fun searchHistory(): Flow<List<String>>
    suspend fun addSearchHistory(keyword: String)
    suspend fun clearSearchHistory()
}

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(username: String, password: String): UserProfile
    suspend fun register(username: String, password: String, repeatedPassword: String): UserProfile
    suspend fun logout()
}

interface CollectionRepository {
    suspend fun collections(page: Int = 0): Page<Article>
    suspend fun collect(articleId: Long)
    suspend fun uncollect(articleId: Long, originId: Long = -1)
}

interface UserRepository {
    suspend fun dashboard(): UserDashboard
}

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}

class ApiException(val code: Int, override val message: String) : RuntimeException(message)
class SessionExpiredException(message: String = "登录状态已失效") : RuntimeException(message)
