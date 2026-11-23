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
class SettingsRepositoryImpl @Inject constructor(
    private val preferences: PreferencesManager
) : SettingsRepository {
    override val themeMode = preferences.themeMode
    override suspend fun setThemeMode(mode: ThemeMode) = preferences.setThemeMode(mode)
}

private const val PAGE_SIZE = 20

@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val api: WanApi,
    private val cache: CacheStore
) : ArticleRepository {
    override suspend fun home(page: Int): HomeContent {
        val pageKey = "home_$page"
        return try {
            val banners = api.banners().requireData()
            val tops = if (page == 0) api.topArticles().requireData() else emptyList()
            val response = api.articles(page, pageSize = PAGE_SIZE).requireData()
            cache.put("banners", banners); cache.put("tops", tops); cache.put(pageKey, response)
            val articles = response.toDomain(page, oneBased = false).let {
                it.copy(items = tops.map { top -> top.toDomain(top = true) } + it.items)
            }
            HomeContent(banners.map { it.toDomain() }, articles)
        } catch (error: Throwable) {
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
        runCatching { api.logout().requireSuccess() }
        cookieJar.clear(); preferences.setLoggedIn(false)
    }
}

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val api: WanApi,
    private val preferences: PreferencesManager
) : CollectionRepository {
    override suspend fun collections(page: Int): Page<Article> = sessionAware {
        api.collections(page, PAGE_SIZE).requireData().toDomain(page, oneBased = false)
    }
    override suspend fun collect(articleId: Long) = sessionAware { api.collect(articleId).requireSuccess() }
    override suspend fun uncollect(articleId: Long, originId: Long) = sessionAware {
        if (originId >= 0) api.uncollectSaved(articleId, originId).requireSuccess()
        else api.uncollectOrigin(articleId).requireSuccess()
    }
    private suspend fun <T> sessionAware(block: suspend () -> T): T = try { block() } catch (error: SessionExpiredException) {
        preferences.setLoggedIn(false); throw error
    }
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: WanApi,
    private val preferences: PreferencesManager
) : UserRepository {
    override suspend fun dashboard(): UserDashboard = try {
        api.userInfo().requireData().let {
            UserDashboard(it.userInfo?.toDomain() ?: UserProfile(0, ""), it.coinInfo?.toDomain() ?: CoinInfo())
        }
    } catch (error: SessionExpiredException) {
        preferences.setLoggedIn(false); throw error
    }
}
