@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.jacky.wanandroid.feature.collection

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
import com.jacky.wanandroid.domain.repository.CollectionRepository
import com.jacky.wanandroid.domain.repository.SessionExpiredException
import com.jacky.wanandroid.domain.usecase.LoadCollectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CollectionUiState(val loading: Boolean = true, val articles: List<Article> = emptyList(), val page: Int = 0, val hasMore: Boolean = false, val error: String? = null)
sealed interface CollectionEvent { data object LoginRequired : CollectionEvent; data class Message(val value: String) : CollectionEvent }
@HiltViewModel class CollectionViewModel @Inject constructor(private val load: LoadCollectionsUseCase, private val repository: CollectionRepository, dispatchers: com.jacky.wanandroid.core.DispatcherProvider) : BaseViewModel(dispatchers) {
    private val _state = MutableStateFlow(CollectionUiState()); val state = _state.asStateFlow(); private val events = Channel<CollectionEvent>(); val event = events.receiveAsFlow()
    init { refresh() }; fun refresh() = loadPage(0, false); fun loadMore() { val s = _state.value; if (s.hasMore) loadPage(s.page + 1, true) }
    fun remove(article: Article) = launchOnIO { runCatching { repository.uncollect(article.id, article.originId) }.onSuccess { _state.update { it.copy(articles = it.articles.filterNot { item -> item.id == article.id }) }; events.send(CollectionEvent.Message("已取消收藏")) }.onFailure { if (it is SessionExpiredException) events.send(CollectionEvent.LoginRequired) else events.send(CollectionEvent.Message(it.message ?: "操作失败")) } }
    private fun loadPage(page: Int, append: Boolean) = launchOnIO { _state.update { it.copy(loading = !append, error = null) }; load(page).onSuccess { result -> _state.update { old -> old.copy(loading = false, articles = if (append) old.articles + result.items else result.items, page = page, hasMore = result.hasMore) } }.onFailure { e -> _state.update { it.copy(loading = false, error = e.message) }; if (e is SessionExpiredException) events.send(CollectionEvent.LoginRequired) } }
}
@Composable fun CollectionScreen(onBack: () -> Unit, onLogin: () -> Unit, onOpenArticle: (Article) -> Unit, viewModel: CollectionViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); val snackbar = remember { SnackbarHostState() }; LaunchedEffect(Unit) { viewModel.event.collect { if (it is CollectionEvent.LoginRequired) onLogin() else snackbar.showSnackbar((it as CollectionEvent.Message).value) } }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, snackbarHost = { SnackbarHost(snackbar) }, topBar = { WanPageHeader("我的收藏", "LIBRARY · SAVED", navigation = { WanBackButton(onBack) }) }) { padding -> when {
        state.loading -> WanStatusPane("正在同步收藏夹", Modifier.padding(padding), loading = true); state.error != null && state.articles.isEmpty() -> WanStatusPane(state.error!!, Modifier.padding(padding), actionLabel = "重试", onAction = viewModel::refresh)
        state.articles.isEmpty() -> WanStatusPane("收藏夹还是空的", Modifier.padding(padding), actionLabel = "刷新", onAction = viewModel::refresh)
        else -> LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { Text("${state.articles.size} SAVED ITEMS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }; items(state.articles, key = { it.id }) { article -> WanArticleCard(article.copy(collected = true), { onOpenArticle(article) }, onCollect = { viewModel.remove(article) }, collectLabel = "取消收藏") }; if (state.hasMore) item { OutlinedButton(onClick = viewModel::loadMore, modifier = Modifier.fillMaxWidth()) { Text("加载下一组") } } }
    } }
}
