@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.jacky.wanandroid.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jacky.wanandroid.core.BaseViewModel
import com.jacky.wanandroid.core.DispatcherProvider
import com.jacky.wanandroid.core.designsystem.WanBackButton
import com.jacky.wanandroid.core.designsystem.WanPageHeader
import com.jacky.wanandroid.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/** 登录与注册共用的表单状态。 */
data class AuthUiState(
    val register: Boolean = false,
    val username: String = "",
    val password: String = "",
    val repeatedPassword: String = "",
    val loading: Boolean = false,
)

/** 账号操作产生的一次性页面事件。 */
sealed interface AuthEvent {
    data object Success : AuthEvent
    data class Error(val message: String) : AuthEvent
}

/** 负责登录、注册表单校验及提交。 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val login: LoginUseCase,
    private val register: RegisterUseCase,
    dispatchers: DispatcherProvider,
) : BaseViewModel(dispatchers) {
    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()
    private val events = Channel<AuthEvent>()
    val event = events.receiveAsFlow()

    fun setUsername(value: String) = _state.update { it.copy(username = value) }
    fun setPassword(value: String) = _state.update { it.copy(password = value) }
    fun setRepeat(value: String) = _state.update { it.copy(repeatedPassword = value) }

    fun toggleMode() = _state.update {
        it.copy(register = !it.register, repeatedPassword = "")
    }

    fun submit() = launchOnIO {
        val current = _state.value
        if (current.username.isBlank() || current.password.isBlank()) {
            events.send(AuthEvent.Error("请输入用户名和密码"))
            return@launchOnIO
        }
        if (current.register && current.password != current.repeatedPassword) {
            events.send(AuthEvent.Error("两次输入的密码不一致"))
            return@launchOnIO
        }

        _state.update { it.copy(loading = true) }
        val result = if (current.register) {
            register(RegisterParams(current.username, current.password, current.repeatedPassword))
        } else {
            login(LoginParams(current.username, current.password))
        }
        _state.update { it.copy(loading = false) }
        result
            .onSuccess { events.send(AuthEvent.Success) }
            .onFailure { events.send(AuthEvent.Error(it.message ?: "操作失败")) }
    }
}

/** 登录与注册页面，成功后通过回调交由导航层处理。 */
@Composable
fun AuthScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.event.collect { current ->
            when (current) {
                AuthEvent.Success -> onSuccess()
                is AuthEvent.Error -> snackbar.showSnackbar(current.message)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            WanPageHeader(
                title = if (state.register) "创建账号" else "欢迎回来",
                kicker = "IDENTITY · ${if (state.register) "REGISTER" else "LOGIN"}",
                navigation = { WanBackButton(onBack) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                if (state.register) "建立你的\n阅读坐标" else "继续你的\nAndroid 探索",
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "收藏、积分与阅读记录会在账号间同步。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(28.dp))
            val fieldShape = CutCornerShape(topEnd = 14.dp)
            OutlinedTextField(
                state.username,
                viewModel::setUsername,
                label = { Text("用户名") },
                singleLine = true,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                state.password,
                viewModel::setPassword,
                label = { Text("密码") },
                singleLine = true,
                shape = fieldShape,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.register) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    state.repeatedPassword,
                    viewModel::setRepeat,
                    label = { Text("确认密码") },
                    singleLine = true,
                    shape = fieldShape,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = viewModel::submit,
                enabled = !state.loading,
                shape = fieldShape,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(
                    if (state.loading) "SYNCING…"
                    else if (state.register) "注册并登录  →"
                    else "进入信号台  →",
                )
            }
            TextButton(onClick = viewModel::toggleMode) {
                Text(if (state.register) "已有账号？去登录" else "没有账号？去注册")
            }
        }
    }
}
