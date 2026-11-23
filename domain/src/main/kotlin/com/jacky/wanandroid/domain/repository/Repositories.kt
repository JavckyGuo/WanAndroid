package com.jacky.wanandroid.domain.repository

import com.jacky.wanandroid.domain.model.*
import kotlinx.coroutines.flow.Flow

/** 提供首页、分类、项目和搜索文章，屏蔽各接口分页差异。 */
interface ArticleRepository {
    suspend fun home(page: Int = 0): HomeContent
    suspend fun articles(categoryId: Long, page: Int = 0): Page<Article>
    suspend fun projects(categoryId: Long, page: Int = 1): Page<Article>
    suspend fun search(keyword: String, page: Int = 0): Page<Article>
}

/** 提供知识体系、导航、热词与本地搜索历史。 */
interface DiscoveryRepository {
    suspend fun categories(): List<Category>
    suspend fun navigation(): List<NavigationGroup>
    suspend fun projectCategories(): List<ProjectCategory>
    suspend fun hotKeys(): List<HotKey>
    fun searchHistory(): Flow<List<String>>
    suspend fun addSearchHistory(keyword: String)
    suspend fun clearSearchHistory()
}

/** 管理登录、注册、退出以及可持续观察的登录状态。 */
interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(username: String, password: String): UserProfile
    suspend fun register(username: String, password: String, repeatedPassword: String): UserProfile
    suspend fun logout()
}

/** 以服务端为最终事实来源管理文章收藏。 */
interface CollectionRepository {
    suspend fun collections(page: Int = 0): Page<Article>
    suspend fun collect(articleId: Long)
    suspend fun uncollect(articleId: Long, originId: Long = -1)
}

/** 加载个人中心所需的用户资料与积分数据。 */
interface UserRepository {
    suspend fun dashboard(): UserDashboard
}

/** 持久化不依赖登录账号的设备级应用设置。 */
interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}

/** WanAndroid 业务响应中 errorCode 非零时抛出的领域异常。 */
class ApiException(val code: Int, override val message: String) : RuntimeException(message)

/** 服务端返回 -1001 时使用，Data 层会同步清理本地登录摘要。 */
class SessionExpiredException(message: String = "登录状态已失效") : RuntimeException(message)
