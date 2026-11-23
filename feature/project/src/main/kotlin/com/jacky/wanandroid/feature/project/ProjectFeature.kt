@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.jacky.wanandroid.feature.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.jacky.wanandroid.core.BaseViewModel
import com.jacky.wanandroid.core.designsystem.*
import com.jacky.wanandroid.domain.model.Article
import com.jacky.wanandroid.domain.model.ProjectCategory
import com.jacky.wanandroid.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ProjectUiState(
    val loading: Boolean = true, val categories: List<ProjectCategory> = emptyList(), val selected: ProjectCategory? = null,
    val projects: List<Article> = emptyList(), val page: Int = 1, val hasMore: Boolean = false, val error: String? = null
)
@HiltViewModel class ProjectViewModel @Inject constructor(
    private val loadCategories: LoadProjectCategoriesUseCase, private val loadProjects: LoadProjectArticlesUseCase,
    dispatchers: com.jacky.wanandroid.core.DispatcherProvider
) : BaseViewModel(dispatchers) {
    private val _state = MutableStateFlow(ProjectUiState()); val state = _state.asStateFlow()
    init { refresh() }
    fun refresh() = launchOnIO { _state.update { it.copy(loading = true) }; loadCategories(NoParams).onSuccess { list ->
        _state.update { it.copy(categories = list, loading = false) }; list.firstOrNull()?.let(::select)
    }.onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } } }
    fun select(value: ProjectCategory) { _state.update { it.copy(selected = value, projects = emptyList(), page = 1) }; loadPage(1, false) }
    fun loadMore() { val s = _state.value; if (s.hasMore) loadPage(s.page + 1, true) }
    private fun loadPage(page: Int, append: Boolean) = launchOnIO { val id = _state.value.selected?.id ?: return@launchOnIO
        loadProjects(CategoryPageParams(id, page)).onSuccess { result -> _state.update { old -> old.copy(
            projects = if (append) old.projects + result.items else result.items, page = page, hasMore = result.hasMore, error = null
        ) } }.onFailure { e -> _state.update { it.copy(error = e.message) } } }
    }
@Composable fun ProjectScreen(onOpenArticle: (Article) -> Unit, viewModel: ProjectViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle(); Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { WanPageHeader("开源项目", "BUILD · SHOWCASE") }) { padding -> Column(Modifier.fillMaxSize().padding(padding)) {
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(state.categories, key = { it.id }) { item -> FilterChip(selected = item.id == state.selected?.id, onClick = { viewModel.select(item) }, label = { Text(item.name) }, shape = androidx.compose.foundation.shape.CutCornerShape(topEnd = 12.dp)) } }
        when { state.loading -> WanStatusPane("正在拉取项目", Modifier.weight(1f), loading = true); state.error != null && state.projects.isEmpty() -> WanStatusPane(state.error!!, Modifier.weight(1f), actionLabel = "重试", onAction = viewModel::refresh)
            else -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(state.projects, key = { it.id }) { project -> Surface(shape = androidx.compose.foundation.shape.CutCornerShape(topEnd = 22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth().clickable { onOpenArticle(project) }) { Row(Modifier.height(IntrinsicSize.Min)) {
                Box(Modifier.width(5.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary)); if (project.envelopePic.isNotBlank()) AsyncImage(model = project.envelopePic, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.width(104.dp).height(150.dp).padding(12.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))); Column(Modifier.weight(1f).padding(16.dp)) { Text(state.selected?.name?.uppercase() ?: "PROJECT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(7.dp)); Text(project.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis); if (project.description.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(project.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis) }; Spacer(Modifier.height(10.dp)); Text("BY ${project.author}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } } }; if (state.hasMore) item { OutlinedButton(onClick = viewModel::loadMore, modifier = Modifier.fillMaxWidth()) { Text("加载下一组") } } }
        }
    } }
}
