@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.jacky.wanandroid.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jacky.wanandroid.core.BaseViewModel
import com.jacky.wanandroid.core.designsystem.WanPageHeader
import com.jacky.wanandroid.domain.model.ThemeMode
import com.jacky.wanandroid.domain.model.UserDashboard
import com.jacky.wanandroid.domain.repository.AuthRepository
import com.jacky.wanandroid.domain.usecase.LoadDashboardUseCase
import com.jacky.wanandroid.domain.usecase.NoParams
import com.jacky.wanandroid.domain.usecase.ObserveThemeModeUseCase
import com.jacky.wanandroid.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** 个人中心的账号概览、积分数据与设备主题设置。 */
data class ProfileUiState(
    val loggedIn: Boolean = false,
    val loading: Boolean = false,
    val dashboard: UserDashboard? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val error: String? = null
)

/** 个人中心发给导航或 Snackbar 的一次性事件。 */
sealed interface ProfileEvent {
    data object LoggedOut : ProfileEvent
    data class Message(val value: String) : ProfileEvent
}

/** 同时观察登录态和主题设置，并处理退出登录及主题切换。 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val loadDashboard: LoadDashboardUseCase,
    observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase,
    dispatchers: com.jacky.wanandroid.core.DispatcherProvider
) : BaseViewModel(dispatchers) {
    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()
    private val events = Channel<ProfileEvent>()
    val event = events.receiveAsFlow()

    init {
        launchOnIO {
            auth.isLoggedIn.collect { loggedIn ->
                _state.update { it.copy(loggedIn = loggedIn, dashboard = if (loggedIn) it.dashboard else null, error = null) }
                if (loggedIn) refresh()
            }
        }
        launchOnIO {
            observeThemeMode().collect { mode -> _state.update { it.copy(themeMode = mode) } }
        }
    }

    fun refresh() = launchOnIO {
        _state.update { it.copy(loading = true, error = null) }
        loadDashboard(NoParams)
            .onSuccess { dashboard -> _state.update { it.copy(loading = false, dashboard = dashboard) } }
            .onFailure { error -> _state.update { it.copy(loading = false, error = error.message ?: "个人资料加载失败") } }
    }

    fun selectTheme(mode: ThemeMode) = launchOnIO {
        setThemeMode(mode).onFailure { events.send(ProfileEvent.Message(it.message ?: "主题设置保存失败")) }
    }

    fun logout() = launchOnIO {
        runCatching { auth.logout() }
            .onSuccess { events.send(ProfileEvent.LoggedOut) }
            .onFailure { events.send(ProfileEvent.Message(it.message ?: "退出失败")) }
    }
}

/** 个人中心入口，负责连接 ViewModel 状态和页面事件。 */
@Composable
fun ProfileScreen(
    onLogin: () -> Unit,
    onCollections: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showThemeSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            if (event is ProfileEvent.Message) snackbar.showSnackbar(event.value)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { WanPageHeader("个人信号台", "PROFILE · DASHBOARD") }
    ) { padding ->
        ProfileContent(
            state = state,
            onLogin = onLogin,
            onCollections = onCollections,
            onThemeClick = { showThemeSheet = true },
            onRetry = viewModel::refresh,
            onLogout = viewModel::logout,
            modifier = Modifier.padding(padding)
        )
    }

    if (showThemeSheet) {
        ThemeModeSheet(
            selected = state.themeMode,
            onSelect = { mode ->
                viewModel.selectTheme(mode)
                showThemeSheet = false
            },
            onDismiss = { showThemeSheet = false }
        )
    }
}

/** 可独立测试的个人中心内容，未登录时仍保留通用设置。 */
@Composable
internal fun ProfileContent(
    state: ProfileUiState,
    onLogin: () -> Unit,
    onCollections: () -> Unit,
    onThemeClick: () -> Unit,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight().widthIn(max = 760.dp).testTag("profile_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { AccountOverview(state, onLogin) }

            if (state.loggedIn && state.loading && state.dashboard == null) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }

            state.dashboard?.let { dashboard ->
                item { SectionTitle("ACTIVITY SNAPSHOT", "账号数据") }
                item { ActivitySnapshot(dashboard) }
                item { SectionTitle("ACCOUNT", "账号功能") }
                item {
                    ProfileActionRow(
                        icon = { Icon(Icons.Rounded.BookmarkBorder, contentDescription = null) },
                        title = "我的收藏",
                        description = "管理已经保存的文章",
                        onClick = onCollections,
                        modifier = Modifier.testTag("collections_setting")
                    )
                }
            }

            item { SectionTitle("GENERAL", "通用设置") }
            item {
                ProfileActionRow(
                    icon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                    title = "主题模式",
                    description = "调整整个应用的明暗外观",
                    value = state.themeMode.displayName,
                    onClick = onThemeClick,
                    modifier = Modifier.testTag("theme_setting")
                )
            }

            state.error?.let { message ->
                item {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = CutCornerShape(topEnd = 14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(message, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                            TextButton(onClick = onRetry) { Text("重试") }
                        }
                    }
                }
            }

            if (state.loggedIn) {
                item {
                    TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("logout_action")) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("退出登录", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun AccountOverview(state: ProfileUiState, onLogin: () -> Unit) {
    val dashboard = state.dashboard
    Surface(
        color = if (dashboard == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(topEnd = 24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("account_overview")
    ) {
        if (!state.loggedIn) {
            Column(Modifier.padding(24.dp)) {
                Text("GUEST ACCESS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(10.dp))
                Text("保存你的\n每一次发现", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(10.dp))
                Text("登录后同步收藏、积分与个人资料。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(22.dp))
                Button(onClick = onLogin, shape = CutCornerShape(topEnd = 14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("登录 / 注册  →") }
            }
        } else if (dashboard == null) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, CutCornerShape(topEnd = 18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("…", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("账号资料", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        if (state.loading) "正在同步个人数据" else "已登录 · 可下拉或重试刷新",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, CutCornerShape(topEnd = 18.dp)), contentAlignment = Alignment.Center) {
                    Text(dashboard.profile.displayName.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(dashboard.profile.displayName, style = MaterialTheme.typography.headlineMedium)
                    Text("USER / ${dashboard.profile.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ActivitySnapshot(dashboard: UserDashboard) {
    Surface(shape = CutCornerShape(topEnd = 22.dp), color = MaterialTheme.colorScheme.secondary, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Metric("积分", dashboard.coin.coinCount.toString())
            Metric("等级", dashboard.coin.level.toString())
            Metric("排名", dashboard.coin.rank)
        }
    }
}

@Composable
private fun SectionTitle(kicker: String, title: String) {
    Column(Modifier.padding(top = 10.dp)) {
        Text(kicker, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun ProfileActionRow(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null
) {
    Surface(
        shape = CutCornerShape(topEnd = 16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CutCornerShape(topEnd = 10.dp), modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) { CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) { icon() } }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            value?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(8.dp))
            Text("→", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 主题模式单选底部弹层；关闭弹层不会修改当前设置。 */
@Composable
internal fun ThemeModeSheet(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text("THEME MODE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text("选择应用外观", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            ThemeMode.entries.forEach { mode ->
                val description = when (mode) {
                    ThemeMode.SYSTEM -> "自动匹配设备的浅色或深色模式"
                    ThemeMode.LIGHT -> "始终使用清爽的阅读纸配色"
                    ThemeMode.DARK -> "始终使用低亮度深色配色"
                }
                val icon = when (mode) {
                    ThemeMode.SYSTEM -> Icons.Rounded.SettingsBrightness
                    ThemeMode.LIGHT -> Icons.Rounded.LightMode
                    ThemeMode.DARK -> Icons.Rounded.DarkMode
                }
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable { onSelect(mode) }.testTag("theme_${mode.name.lowercase()}").padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(mode.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    RadioButton(selected = selected == mode, onClick = { onSelect(mode) })
                }
            }
        }
    }
}

private val ThemeMode.displayName: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "跟随系统"
        ThemeMode.LIGHT -> "浅色"
        ThemeMode.DARK -> "深色"
    }

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSecondary)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondary.copy(alpha = .7f))
    }
}
