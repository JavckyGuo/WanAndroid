@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.jacky.wanandroid.feature.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import com.jacky.wanandroid.domain.model.NavigationGroup
import com.jacky.wanandroid.domain.usecase.LoadNavigationUseCase
import com.jacky.wanandroid.domain.usecase.NoParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** 导航分组的加载状态。 */
data class NavigationUiState(val loading: Boolean = true, val groups: List<NavigationGroup> = emptyList(), val error: String? = null)
/** 获取站点导航并将异常转换为可重试页面状态。 */
@HiltViewModel class NavigationViewModel @Inject constructor(private val load: LoadNavigationUseCase, dispatchers: com.jacky.wanandroid.core.DispatcherProvider) : BaseViewModel(dispatchers) {
    private val _state = MutableStateFlow(NavigationUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = launchOnIO {
        _state.update { it.copy(loading = true) }
        load(NoParams)
        .onSuccess { value -> _state.value = NavigationUiState(false, value) }
        .onFailure { e -> _state.value = NavigationUiState(false, error = e.message ?: "加载失败") } }
}
/** 按服务端分组展示快捷文章入口。 */
@Composable fun NavigationScreen(onOpenArticle: (Article) -> Unit, viewModel: NavigationViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { WanPageHeader("快捷导航", "DISCOVERY · DIRECTORY") }) { padding ->
        when { state.loading -> WanStatusPane("正在索引站点", Modifier.padding(padding), loading = true); state.error != null -> WanStatusPane(state.error!!, Modifier.padding(padding), actionLabel = "重试", onAction = viewModel::refresh)
            else -> LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                items(state.groups, key = { it.name }) { group -> Column {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Box(Modifier.size(8.dp).background(CompileAmber)); Spacer(Modifier.width(10.dp)); Text(group.name, style = MaterialTheme.typography.titleLarge); Spacer(Modifier.weight(1f)); Text("${group.articles.size.toString().padStart(2, '0')}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(Modifier.height(10.dp)); androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { group.articles.forEach { article -> SuggestionChip(onClick = { onOpenArticle(article) }, label = { Text(article.title, maxLines = 1) }, shape = androidx.compose.foundation.shape.CutCornerShape(topEnd = 10.dp)) } }
                } }
            }
        }
    }
}
