@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.jacky.wanandroid.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.jacky.wanandroid.core.BaseViewModel
import com.jacky.wanandroid.core.designsystem.*
import com.jacky.wanandroid.domain.model.Article
import com.jacky.wanandroid.domain.model.Banner
import com.jacky.wanandroid.domain.repository.CollectionRepository
import com.jacky.wanandroid.domain.repository.SessionExpiredException
import com.jacky.wanandroid.domain.usecase.LoadHomeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** 首页聚合内容、分页进度与缓存来源状态。 */
data class HomeUiState(
    val isLoading: Boolean = true, val isLoadingMore: Boolean = false,
    val banners: List<Banner> = emptyList(), val articles: List<Article> = emptyList(),
    val page: Int = 0, val hasMore: Boolean = true, val fromCache: Boolean = false,
    val errorMessage: String? = null
)
/** 首页需要消费一次的提示或登录跳转事件。 */
sealed interface HomeUiEvent {
    data class Message(val value: String) : HomeUiEvent
    data object RequestLogin : HomeUiEvent
}

/** 协调首页刷新、加载更多及收藏状态同步。 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val loadHome: LoadHomeUseCase,
    private val collectionRepository: CollectionRepository,
    dispatchers: com.jacky.wanandroid.core.DispatcherProvider
) : BaseViewModel(dispatchers) {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private val events = Channel<HomeUiEvent>()
    val uiEvent = events.receiveAsFlow()
    init { refresh() }
    fun refresh() = load(0, false)
    fun loadMore() {
        val state = _uiState.value
        if (!state.isLoadingMore && state.hasMore) load(state.page + 1, true)
    }
    fun toggleCollection(articleId: Long, collected: Boolean) = launchOnIO {
        runCatching { if (collected) collectionRepository.uncollect(articleId) else collectionRepository.collect(articleId) }
            .onSuccess { _uiState.update { state -> state.copy(articles = state.articles.map { if (it.id == articleId) it.copy(collected = !collected) else it }) } }
            .onFailure { if (it is SessionExpiredException) events.send(HomeUiEvent.RequestLogin) else events.send(HomeUiEvent.Message(it.message ?: "收藏操作失败")) }
    }
    private fun load(page: Int, append: Boolean) = launchOnIO {
        _uiState.update { it.copy(isLoading = !append, isLoadingMore = append, errorMessage = null) }
        loadHome(page).onSuccess { content -> _uiState.update { old -> old.copy(
            isLoading = false, isLoadingMore = false, banners = if (append) old.banners else content.banners,
            articles = if (append) old.articles + content.articles.items else content.articles.items,
            page = page, hasMore = content.articles.hasMore, fromCache = content.articles.fromCache
        ) } }.onFailure { error -> _uiState.update { it.copy(isLoading = false, isLoadingMore = false, errorMessage = error.message ?: "加载失败") } }
    }
}

/** 展示 Banner、缓存提示和可分页的文章流。 */
@Composable
fun HomeScreen(onSearch: () -> Unit, onLogin: () -> Unit, onOpenArticle: (Article) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { viewModel.uiEvent.collect { if (it is HomeUiEvent.RequestLogin) onLogin() else snackbar.showSnackbar((it as HomeUiEvent.Message).value) } }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, snackbarHost = { SnackbarHost(snackbar) }, topBar = {
        WanPageHeader(title = "WanAndroid", kicker = "DAILY · ANDROID SIGNAL", action = {
            IconButton(onClick = onSearch) { Icon(Icons.Rounded.Search, contentDescription = "搜索") }
        })
    }) { padding -> when {
        state.isLoading && state.articles.isEmpty() -> WanStatusPane("正在同步今日内容", Modifier.padding(padding), loading = true)
        state.errorMessage != null && state.articles.isEmpty() -> WanStatusPane(state.errorMessage!!, Modifier.padding(padding), actionLabel = "重新连接", onAction = viewModel::refresh)
        else -> LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.fromCache) item { AssistChip(onClick = viewModel::refresh, label = { Text("OFFLINE · 点击刷新缓存") }, modifier = Modifier.padding(horizontal = 20.dp)) }
            if (state.banners.isNotEmpty()) {
                item { WanSectionLabel("01", "今日雷达", "${state.banners.size} SIGNALS") }
                item { LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.banners, key = { it.id }) { banner -> SignalBanner(banner) { onOpenArticle(Article(banner.id, banner.title, banner.url, "")) } }
                } }
            }
            item { WanSectionLabel("02", "最新文章", "${state.articles.size} ITEMS") }
            items(state.articles, key = { "${it.id}_${it.top}" }) { article ->
                WanArticleCard(article = article, onClick = { onOpenArticle(article) }, onCollect = { viewModel.toggleCollection(article.id, article.collected) }, modifier = Modifier.padding(horizontal = 20.dp))
            }
            if (state.hasMore) item { OutlinedButton(onClick = viewModel::loadMore, enabled = !state.isLoadingMore, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { Text(if (state.isLoadingMore) "SYNCING…" else "加载下一组") } }
        }
    } }
}

@Composable private fun SignalBanner(banner: Banner, onClick: () -> Unit) {
    Box(Modifier.width(300.dp).height(172.dp).clip(CutCornerShape(topEnd = 30.dp)).clickable(onClick = onClick)) {
        AsyncImage(model = banner.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.secondary.copy(alpha = .12f), MaterialTheme.colorScheme.secondary.copy(alpha = .94f)))))
        Text("RADAR", style = MaterialTheme.typography.labelSmall, color = SignalCyan, modifier = Modifier.align(Alignment.TopStart).padding(18.dp))
        Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
            Text(banner.title, style = MaterialTheme.typography.titleLarge, color = androidx.compose.ui.graphics.Color.White, maxLines = 2)
            Spacer(Modifier.height(7.dp))
            Text("OPEN SIGNAL  →", style = MaterialTheme.typography.labelSmall, color = CompileAmber)
        }
    }
}
