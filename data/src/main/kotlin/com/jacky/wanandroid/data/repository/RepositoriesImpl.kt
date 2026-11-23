package com.jacky.wanandroid.data.repository

import com.google.gson.reflect.TypeToken
import com.jacky.wanandroid.data.local.CacheStore
import com.jacky.wanandroid.data.local.preferences.PreferencesManager
import com.jacky.wanandroid.data.remote.*
import com.jacky.wanandroid.data.remote.dto.*
import com.jacky.wanandroid.domain.model.*
import com.jacky.wanandroid.domain.repository.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
/** 将主题偏好映射到 Domain 设置接口。 */
class SettingsRepositoryImpl @Inject constructor(
    private val preferences: PreferencesManager
) : SettingsRepository {
    override val themeMode = preferences.themeMode
    override suspend fun setThemeMode(mode: ThemeMode) = preferences.setThemeMode(mode)
}

private const val PAGE_SIZE = 20

@Singleton
/**
 * 文章数据仓库。
 *
 * 在线请求成功后写入 Room；网络或解析失败时按相同业务键读取缓存。
 */
class ArticleRepositoryImpl @Inject constructor(
    private val api: WanApi,
    private val cache: CacheStore
) : ArticleRepository {
    override suspend fun home(page: Int): HomeContent {
        val pageKey = "home_$page"
        return try {
            // 首页首屏聚合 Banner、置顶文章和第 0 页普通文章。
            val banners = api.banners().requireData()
            val tops = if (page == 0) api.topArticles().requireData() else emptyList()
            val response = api.articles(page, pageSize = PAGE_SIZE).requireData()
            cache.put("banners", banners)
            cache.put("tops", tops)
            cache.put(pageKey, response)
            val articles = response.toDomain(page, oneBased = false).let {
                it.copy(items = tops.map { top -> top.toDomain(top = true) } + it.items)
            }
            HomeContent(banners.map { it.toDomain() }, articles)
        } catch (error: Throwable) {
            // 只有缓存页存在时才回退；否则继续抛出原始错误供 UI 展示。
            val pageType = TypeToken.getParameterized(PageDto::class.java, ArticleDto::class.java).type
            val bannerType = TypeToken.getParameterized(List::class.java, BannerDto::class.java).type
            val articleType = TypeToken.getParameterized(List::class.java, ArticleDto::class.java).type
            val cachedPage = cache.get<PageDto<ArticleDto>>(pageKey, pageType) ?: throw error
            val cachedBanners = cache.get<List<BannerDto>>("banners", bannerType).orEmpty()
            val tops = if (page == 0) cache.get<List<ArticleDto>>("tops", articleType).orEmpty() else emptyList()
            val articles = cachedPage.toDomain(page, false, true).let {
                it.copy(items = tops.map { top -> top.toDomain(top = true) } + it.items)
            }
            HomeContent(cachedBanners.map { it.toDomain() }, articles)
        }
    }

    override suspend fun articles(categoryId: Long, page: Int): Page<Article> = cachedArticlePage(
        key = "articles_${categoryId}_$page", page = page, oneBased = false
    ) { api.articles(page, categoryId, PAGE_SIZE).requireData() }

    override suspend fun projects(categoryId: Long, page: Int): Page<Article> = cachedArticlePage(
        // 项目接口从 1 开始，和首页/体系文章的 0 起始页不同。
        key = "projects_${categoryId}_$page", page = page, oneBased = true
    ) { api.projects(page, categoryId, PAGE_SIZE).requireData() }

    override suspend fun search(keyword: String, page: Int): Page<Article> =
        api.search(page, keyword, PAGE_SIZE).requireData().toDomain(page, oneBased = false)

    private suspend fun cachedArticlePage(
        key: String, page: Int, oneBased: Boolean, request: suspend () -> PageDto<ArticleDto>
    ): Page<Article> = try {
        request().also { cache.put(key, it) }.toDomain(page, oneBased)
    } catch (error: Throwable) {
        val type = TypeToken.getParameterized(PageDto::class.java, ArticleDto::class.java).type
        (cache.get<PageDto<ArticleDto>>(key, type) ?: throw error).toDomain(page, oneBased, true)
    }
}

@Singleton
/** 提供体系、导航、项目分类、热词与本地搜索历史。 */
class DiscoveryRepositoryImpl @Inject constructor(
    private val api: WanApi,
    private val cache: CacheStore,
    private val preferences: PreferencesManager
) : DiscoveryRepository {
    override suspend fun categories(): List<Category> = cachedList("categories", CategoryDto::class.java) {
        api.categories().requireData()
    }.map { it.toDomain() }
    override suspend fun navigation(): List<NavigationGroup> = cachedList("navigation", NavigationDto::class.java) {
        api.navigation().requireData()
    }.map { NavigationGroup(it.name.orEmpty(), it.articles.orEmpty().map { item -> item.toDomain() }) }
    override suspend fun projectCategories(): List<ProjectCategory> = cachedList("project_categories", CategoryDto::class.java) {
        api.projectCategories().requireData()
    }.map { ProjectCategory(it.id, it.name.orEmpty()) }
    override suspend fun hotKeys(): List<HotKey> = cachedList("hot_keys", HotKeyDto::class.java) {
        api.hotKeys().requireData()
    }.map { HotKey(it.id, it.name.orEmpty()) }
    override fun searchHistory(): Flow<List<String>> = preferences.searchHistory
    override suspend fun addSearchHistory(keyword: String) { preferences.addSearchHistory(keyword.trim()) }
    override suspend fun clearSearchHistory() { preferences.clearSearchHistory() }

    private suspend fun <T> cachedList(key: String, itemClass: Class<T>, request: suspend () -> List<T>): List<T> = try {
        request().also { cache.put(key, it) }
    } catch (error: Throwable) {
        val type = TypeToken.getParameterized(List::class.java, itemClass).type
        cache.get<List<T>>(key, type) ?: throw error
    }
}

@Singleton
/** 管理服务端认证请求、本地登录摘要和加密 Cookie 会话。 */
class AuthRepositoryImpl @Inject constructor(
    private val api: WanApi,
    private val preferences: PreferencesManager,
    private val cookieJar: EncryptedCookieJar
) : AuthRepository {
    override val isLoggedIn: Flow<Boolean> = preferences.isLoggedIn
    override suspend fun login(username: String, password: String): UserProfile =
        api.login(username, password).requireData().toDomain().also { preferences.setLoggedIn(true) }
    override suspend fun register(username: String, password: String, repeatedPassword: String): UserProfile =
        api.register(username, password, repeatedPassword).requireData().toDomain().also { preferences.setLoggedIn(true) }
    override suspend fun logout() {
        // 服务端退出失败也必须清理本地会话，避免用户无法退出当前设备。
        runCatching { api.logout().requireSuccess() }
        cookieJar.clear()
        preferences.setLoggedIn(false)
    }
}

@Singleton
/** 管理收藏列表及收藏状态变更。 */
class CollectionRepositoryImpl @Inject constructor(
    private val api: WanApi,
    private val preferences: PreferencesManager
) : CollectionRepository {
    override suspend fun collections(page: Int): Page<Article> = sessionAware {
        api.collections(page, PAGE_SIZE).requireData().toDomain(page, oneBased = false)
    }
    override suspend fun collect(articleId: Long) = sessionAware { api.collect(articleId).requireSuccess() }
    override suspend fun uncollect(articleId: Long, originId: Long) = sessionAware {
        // 收藏列表返回 originId，需要调用与原始文章列表不同的取消收藏接口。
        if (originId >= 0) api.uncollectSaved(articleId, originId).requireSuccess()
        else api.uncollectOrigin(articleId).requireSuccess()
    }
    private suspend fun <T> sessionAware(block: suspend () -> T): T = try { block() } catch (error: SessionExpiredException) {
        // -1001 表示 Cookie 已失效，同步更新登录摘要使 UI 返回未登录状态。
        preferences.setLoggedIn(false)
        throw error
    }
}

@Singleton
/** 加载个人中心聚合数据，并在会话失效时清理本地登录态。 */
class UserRepositoryImpl @Inject constructor(
    private val api: WanApi,
    private val preferences: PreferencesManager
) : UserRepository {
    override suspend fun dashboard(): UserDashboard = try {
        api.userInfo().requireData().let {
            UserDashboard(it.userInfo?.toDomain() ?: UserProfile(0, ""), it.coinInfo?.toDomain() ?: CoinInfo())
        }
    } catch (error: SessionExpiredException) {
        preferences.setLoggedIn(false)
        throw error
    }
}
