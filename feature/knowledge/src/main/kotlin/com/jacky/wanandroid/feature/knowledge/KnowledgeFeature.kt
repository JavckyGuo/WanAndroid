@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.jacky.wanandroid.feature.knowledge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jacky.wanandroid.core.BaseViewModel
import com.jacky.wanandroid.core.designsystem.*
import com.jacky.wanandroid.domain.model.Article
import com.jacky.wanandroid.domain.model.Category
import com.jacky.wanandroid.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** 知识体系分类选择及其文章分页状态。 */
data class KnowledgeUiState(
    val loading: Boolean = true, val categories: List<Category> = emptyList(),
    val selected: Category? = null, val articles: List<Article> = emptyList(),
    val page: Int = 0, val hasMore: Boolean = false, val error: String? = null
)
/** 先加载分类树，再按选中的二级分类加载从 0 开始的文章页。 */
@HiltViewModel class KnowledgeViewModel @Inject constructor(
    private val loadCategories: LoadCategoriesUseCase, private val loadArticles: LoadCategoryArticlesUseCase,
    dispatchers: com.jacky.wanandroid.core.DispatcherProvider
) : BaseViewModel(dispatchers) {
    private val _state = MutableStateFlow(KnowledgeUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }
    fun refresh() = launchOnIO { _state.update { it.copy(loading = true, error = null) }; loadCategories(NoParams)
        .onSuccess { list -> _state.update { it.copy(loading = false, categories = list) }; list.firstOrNull()?.children?.firstOrNull()?.let(::select) }
        .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } } }
    fun select(category: Category) {
        _state.update { it.copy(selected = category, articles = emptyList(), page = 0) }
        loadPage(0, false)
    }

    fun loadMore() {
        val current = _state.value
        if (current.hasMore) loadPage(current.page + 1, true)
    }
    private fun loadPage(page: Int, append: Boolean) = launchOnIO { val id = _state.value.selected?.id ?: return@launchOnIO
        loadArticles(CategoryPageParams(id, page)).onSuccess { result -> _state.update { old -> old.copy(
            articles = if (append) old.articles + result.items else result.items, page = page, hasMore = result.hasMore, error = null
        ) } }.onFailure { e -> _state.update { it.copy(error = e.message) } } }
}

/** 左侧分类树与右侧文章列表组成的知识体系页面。 */
@Composable fun KnowledgeScreen(onOpenArticle: (Article) -> Unit, viewModel: KnowledgeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { WanPageHeader(state.selected?.name ?: "知识体系", "KNOWLEDGE · MAP") }) { padding ->
        when { state.loading -> WanStatusPane("正在构建知识图谱", Modifier.padding(padding), loading = true); state.error != null && state.categories.isEmpty() -> WanStatusPane(state.error!!, Modifier.padding(padding), actionLabel = "重试", onAction = viewModel::refresh)
            else -> Row(Modifier.fillMaxSize().padding(padding)) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f), modifier = Modifier.width(126.dp).fillMaxHeight()) { LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) { items(state.categories, key = { it.id }) { parent ->
                    Text(parent.name.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                    parent.children.forEach { child -> NavigationDrawerItem(label = { Text(child.name, maxLines = 2, style = MaterialTheme.typography.bodyMedium) }, selected = state.selected?.id == child.id, onClick = { viewModel.select(child) }, shape = androidx.compose.foundation.shape.CutCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)) }
                } } }
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item { Text("${state.articles.size.toString().padStart(2, '0')} ARTICLES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                    items(state.articles, key = { it.id }) { article -> WanArticleCard(article, { onOpenArticle(article) }) }
                    if (state.hasMore) item { OutlinedButton(onClick = viewModel::loadMore, modifier = Modifier.fillMaxWidth()) { Text("加载下一组") } }
                } }
        }
    }
}
