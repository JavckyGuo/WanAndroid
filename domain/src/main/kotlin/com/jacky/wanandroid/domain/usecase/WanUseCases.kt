package com.jacky.wanandroid.domain.usecase

import com.jacky.wanandroid.domain.model.*
import com.jacky.wanandroid.domain.repository.*
import javax.inject.Inject

/** 加载首页 Banner、置顶文章和普通文章。 */
class LoadHomeUseCase @Inject constructor(
    private val repository: ArticleRepository
) : BaseUseCase<Int, HomeContent>() {
    override suspend fun execute(params: Int) = repository.home(params)
}

/** 加载知识体系的一级与二级分类。 */
class LoadCategoriesUseCase @Inject constructor(
    private val repository: DiscoveryRepository
) : BaseUseCase<NoParams, List<Category>>() {
    override suspend fun execute(params: NoParams) = repository.categories()
}

/** 加载常用导航分组。 */
class LoadNavigationUseCase @Inject constructor(
    private val repository: DiscoveryRepository
) : BaseUseCase<NoParams, List<NavigationGroup>>() {
    override suspend fun execute(params: NoParams) = repository.navigation()
}

/** 加载项目频道分类。 */
class LoadProjectCategoriesUseCase @Inject constructor(
    private val repository: DiscoveryRepository
) : BaseUseCase<NoParams, List<ProjectCategory>>() {
    override suspend fun execute(params: NoParams) = repository.projectCategories()
}

/** 分类文章与项目列表共用的分页参数。 */
data class CategoryPageParams(val categoryId: Long, val page: Int)

/** 按知识体系分类加载文章。 */
class LoadCategoryArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) : BaseUseCase<CategoryPageParams, Page<Article>>() {
    override suspend fun execute(params: CategoryPageParams) =
        repository.articles(params.categoryId, params.page)
}

/** 按项目分类加载文章，页码起点由 Repository 层处理。 */
class LoadProjectArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) : BaseUseCase<CategoryPageParams, Page<Article>>() {
    override suspend fun execute(params: CategoryPageParams) =
        repository.projects(params.categoryId, params.page)
}

/** 搜索关键字和从 0 开始的请求页码。 */
data class SearchParams(val keyword: String, val page: Int = 0)

/** 分页搜索站内文章。 */
class SearchArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) : BaseUseCase<SearchParams, Page<Article>>() {
    override suspend fun execute(params: SearchParams) =
        repository.search(params.keyword, params.page)
}

/** 登录用例参数；密码只在当前请求内存中使用。 */
data class LoginParams(val username: String, val password: String)

/** 使用服务端 Cookie 会话登录。 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) : BaseUseCase<LoginParams, UserProfile>() {
    override suspend fun execute(params: LoginParams) =
        repository.login(params.username, params.password)
}

/** 注册参数，包含服务端要求的重复密码。 */
data class RegisterParams(
    val username: String,
    val password: String,
    val repeatedPassword: String
)

/** 注册账号并建立登录会话。 */
class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) : BaseUseCase<RegisterParams, UserProfile>() {
    override suspend fun execute(params: RegisterParams) =
        repository.register(params.username, params.password, params.repeatedPassword)
}

/** 加载当前用户的收藏文章。 */
class LoadCollectionsUseCase @Inject constructor(
    private val repository: CollectionRepository
) : BaseUseCase<Int, Page<Article>>() {
    override suspend fun execute(params: Int) = repository.collections(params)
}

/** 加载个人中心的资料与积分聚合信息。 */
class LoadDashboardUseCase @Inject constructor(
    private val repository: UserRepository
) : BaseUseCase<NoParams, UserDashboard>() {
    override suspend fun execute(params: NoParams) = repository.dashboard()
}

/** 持续观察设备级主题偏好。 */
class ObserveThemeModeUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke() = repository.themeMode
}

/** 持久化新的主题模式。 */
class SetThemeModeUseCase @Inject constructor(
    private val repository: SettingsRepository
) : BaseUseCase<ThemeMode, Unit>() {
    override suspend fun execute(params: ThemeMode) = repository.setThemeMode(params)
}
