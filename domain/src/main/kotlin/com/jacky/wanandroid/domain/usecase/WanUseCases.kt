package com.jacky.wanandroid.domain.usecase

import com.jacky.wanandroid.domain.model.*
import com.jacky.wanandroid.domain.repository.*
import javax.inject.Inject

class LoadHomeUseCase @Inject constructor(private val repository: ArticleRepository) : BaseUseCase<Int, HomeContent>() {
    override suspend fun execute(params: Int) = repository.home(params)
}
class LoadCategoriesUseCase @Inject constructor(private val repository: DiscoveryRepository) : BaseUseCase<NoParams, List<Category>>() {
    override suspend fun execute(params: NoParams) = repository.categories()
}
class LoadNavigationUseCase @Inject constructor(private val repository: DiscoveryRepository) : BaseUseCase<NoParams, List<NavigationGroup>>() {
    override suspend fun execute(params: NoParams) = repository.navigation()
}
class LoadProjectCategoriesUseCase @Inject constructor(private val repository: DiscoveryRepository) : BaseUseCase<NoParams, List<ProjectCategory>>() {
    override suspend fun execute(params: NoParams) = repository.projectCategories()
}
data class CategoryPageParams(val categoryId: Long, val page: Int)
class LoadCategoryArticlesUseCase @Inject constructor(private val repository: ArticleRepository) : BaseUseCase<CategoryPageParams, Page<Article>>() {
    override suspend fun execute(params: CategoryPageParams) = repository.articles(params.categoryId, params.page)
}
class LoadProjectArticlesUseCase @Inject constructor(private val repository: ArticleRepository) : BaseUseCase<CategoryPageParams, Page<Article>>() {
    override suspend fun execute(params: CategoryPageParams) = repository.projects(params.categoryId, params.page)
}
data class SearchParams(val keyword: String, val page: Int = 0)
class SearchArticlesUseCase @Inject constructor(private val repository: ArticleRepository) : BaseUseCase<SearchParams, Page<Article>>() {
    override suspend fun execute(params: SearchParams) = repository.search(params.keyword, params.page)
}
data class LoginParams(val username: String, val password: String)
class LoginUseCase @Inject constructor(private val repository: AuthRepository) : BaseUseCase<LoginParams, UserProfile>() {
    override suspend fun execute(params: LoginParams) = repository.login(params.username, params.password)
}
data class RegisterParams(val username: String, val password: String, val repeatedPassword: String)
class RegisterUseCase @Inject constructor(private val repository: AuthRepository) : BaseUseCase<RegisterParams, UserProfile>() {
    override suspend fun execute(params: RegisterParams) = repository.register(params.username, params.password, params.repeatedPassword)
}
class LoadCollectionsUseCase @Inject constructor(private val repository: CollectionRepository) : BaseUseCase<Int, Page<Article>>() {
    override suspend fun execute(params: Int) = repository.collections(params)
}
class LoadDashboardUseCase @Inject constructor(private val repository: UserRepository) : BaseUseCase<NoParams, UserDashboard>() {
    override suspend fun execute(params: NoParams) = repository.dashboard()
}

class ObserveThemeModeUseCase @Inject constructor(private val repository: SettingsRepository) {
    operator fun invoke() = repository.themeMode
}

class SetThemeModeUseCase @Inject constructor(private val repository: SettingsRepository) : BaseUseCase<ThemeMode, Unit>() {
    override suspend fun execute(params: ThemeMode) = repository.setThemeMode(params)
}
