@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.jacky.wanandroid.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jacky.wanandroid.core.BaseViewModel
import com.jacky.wanandroid.core.designsystem.*
import com.jacky.wanandroid.domain.model.Article
import com.jacky.wanandroid.domain.model.HotKey
import com.jacky.wanandroid.domain.repository.DiscoveryRepository
import com.jacky.wanandroid.domain.usecase.SearchArticlesUseCase
import com.jacky.wanandroid.domain.usecase.SearchParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SearchUiState(
    val query: String = "", val searching: Boolean = false, val hotKeys: List<HotKey> = emptyList(),
    val history: List<String> = emptyList(), val results: List<Article> = emptyList(), val page: Int = 0,
    val hasMore: Boolean = false, val error: String? = null
)
@HiltViewModel class SearchViewModel @Inject constructor(
    private val search: SearchArticlesUseCase, private val discovery: DiscoveryRepository,
    dispatchers: com.jacky.wanandroid.core.DispatcherProvider
) : BaseViewModel(dispatchers) {
    private val _state = MutableStateFlow(SearchUiState()); val state = _state.asStateFlow()
    init { launchOnIO { runCatching { discovery.hotKeys() }.onSuccess { value -> _state.update { it.copy(hotKeys = value) } } }; launchOnIO { discovery.searchHistory().collect { value -> _state.update { it.copy(history = value) } } } }
    fun setQuery(value: String) = _state.update { it.copy(query = value) }
    fun submit(value: String = _state.value.query, append: Boolean = false) { val keyword = value.trim(); if (keyword.isBlank()) return
        _state.update { it.copy(query = keyword, searching = true, error = null) }; val page = if (append) _state.value.page + 1 else 0
        launchOnIO { discovery.addSearchHistory(keyword); search(SearchParams(keyword, page)).onSuccess { result -> _state.update { old -> old.copy(
            searching = false, results = if (append) old.results + result.items else result.items, page = page, hasMore = result.hasMore
        ) } }.onFailure { e -> _state.update { it.copy(searching = false, error = e.message) } } }
    }
    fun clearHistory() = launchOnIO { discovery.clearSearchHistory() }
}
@Composable fun SearchScreen(onBack: () -> Unit, onOpenArticle: (Article) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { WanPageHeader("全站搜索", "QUERY · KNOWLEDGE", navigation = { WanBackButton(onBack) }) }) { padding -> Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
        OutlinedTextField(value = state.query, onValueChange = viewModel::setQuery, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = androidx.compose.foundation.shape.CutCornerShape(topEnd = 16.dp), placeholder = { Text("输入关键词，空格可分隔") }, trailingIcon = { FilledTonalButton(onClick = { viewModel.submit() }) { Text("搜索") } }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { viewModel.submit() }))
        if (state.results.isEmpty() && !state.searching) {
            WanSectionLabel("01", "热门信号", modifier = Modifier.padding(horizontal = 0.dp)); FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { state.hotKeys.forEach { key -> SuggestionChip(onClick = { viewModel.submit(key.name) }, label = { Text(key.name) }, shape = androidx.compose.foundation.shape.CutCornerShape(topEnd = 10.dp)) } }
            WanSectionLabel("02", "搜索历史", if (state.history.isEmpty()) "EMPTY" else "${state.history.size} ITEMS", Modifier.padding(horizontal = 0.dp)); state.history.forEachIndexed { index, value -> Text("${(index + 1).toString().padStart(2, '0')}  $value", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().clickable { viewModel.submit(value) }.padding(vertical = 10.dp)) }; if (state.history.isNotEmpty()) TextButton(onClick = viewModel::clearHistory) { Text("清除历史") }
        }
        if (state.searching && state.results.isEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 20.dp)); state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 12.dp)) }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { if (state.results.isNotEmpty()) item { Text("${state.results.size} RESULTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }; items(state.results, key = { it.id }) { article -> WanArticleCard(article, { onOpenArticle(article) }) }; if (state.hasMore) item { OutlinedButton(onClick = { viewModel.submit(append = true) }, modifier = Modifier.fillMaxWidth()) { Text("加载下一组") } } }
    } }
}
