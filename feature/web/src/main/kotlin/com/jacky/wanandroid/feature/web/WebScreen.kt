@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.jacky.wanandroid.feature.web

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jacky.wanandroid.core.designsystem.WanBackButton
import com.jacky.wanandroid.core.designsystem.WanPageHeader
import kotlinx.coroutines.delay

/** 阅读器内可恢复的失败信息。 */
private data class ReaderFailure(val title: String, val detail: String)

/** 将明文 HTTP 链接优先升级为 HTTPS，其他格式保持原样交给校验层。 */
internal fun normalizeReaderUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    return if (trimmed.startsWith("http://", ignoreCase = true)) {
        "https://${trimmed.substringAfter("://")}"
    } else trimmed
}

/** 阅读器仅允许 HTTP(S)，避免 intent、file 等 Scheme 自动拉起外部能力。 */
internal fun isWebUrl(rawUrl: String): Boolean = rawUrl.substringBefore(':', missingDelimiterValue = "").lowercase() in setOf("http", "https")

/** 使用网站原始排版展示文章，并提供明确的加载、超时和失败反馈。 */
@Composable
fun WebScreen(title: String, url: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val initialUrl = remember(url) { normalizeReaderUrl(url) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var contentVisible by remember { mutableStateOf(false) }
    var slowLoading by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<ReaderFailure?>(null) }
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var canGoForward by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var loadAttempt by remember { mutableIntStateOf(0) }
    var rendererGeneration by remember { mutableIntStateOf(0) }

    fun updateHistory(view: WebView?) {
        canGoForward = view?.canGoForward() == true
        currentUrl = view?.url?.takeIf { it.isNotBlank() } ?: currentUrl
    }

    fun retry() {
        failure = null
        slowLoading = false
        contentVisible = false
        loading = true
        progress = 0
        loadAttempt++
        if (webView == null) rendererGeneration++ else webView?.loadUrl(currentUrl.ifBlank { initialUrl })
    }

    fun openExternally() {
        val target = currentUrl.ifBlank { initialUrl }
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target))) }
            .onFailure { failure = ReaderFailure("无法打开外部浏览器", it.message ?: "设备上没有可处理此链接的应用") }
    }

    // 页面迟迟没有提交可见内容时给出提示，避免用户只看到无反馈的白屏。
    LaunchedEffect(loadAttempt, loading, contentVisible) {
        if (loading && !contentVisible) {
            delay(12_000)
            if (loading && !contentVisible && failure == null) slowLoading = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.webChromeClient = null
            webView?.webViewClient = WebViewClient()
            webView?.destroy()
            webView = null
        }
    }

    BackHandler { if (webView?.canGoBack() == true) webView?.goBack() else onBack() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            WanPageHeader(
                title = title,
                kicker = "READER · WEB",
                navigation = { WanBackButton { if (webView?.canGoBack() == true) webView?.goBack() else onBack() } },
                action = {
                    Row {
                        IconButton(onClick = { webView?.goForward() }, enabled = canGoForward) { Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "前进") }
                        IconButton(onClick = ::retry) { Icon(Icons.Rounded.Refresh, contentDescription = "刷新") }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = "更多操作") }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(text = { Text("复制链接") }, onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("文章链接", currentUrl))
                                    menuExpanded = false
                                })
                                DropdownMenuItem(
                                    text = { Text("在浏览器中打开") },
                                    onClick = {
                                        menuExpanded = false
                                        openExternally()
                                    },
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            key(rendererGeneration) { AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        setBackgroundColor(android.graphics.Color.WHITE)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        // 关闭本地文件、内容 URI、混合内容及多窗口，缩小第三方页面的能力边界。
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.javaScriptCanOpenWindowsAutomatically = false
                        settings.setSupportMultipleWindows(false)
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) settings.safeBrowsingEnabled = true

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress.coerceIn(0, 100)
                                if (newProgress >= 100 && failure == null) loading = false
                                updateHistory(view)
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, target: String?, favicon: Bitmap?) {
                                loading = true
                                contentVisible = false
                                slowLoading = false
                                failure = null
                                progress = 0
                                target?.let { currentUrl = it }
                            }

                            override fun onPageCommitVisible(view: WebView?, target: String?) {
                                contentVisible = true
                                slowLoading = false
                                target?.let { currentUrl = it }
                                updateHistory(view)
                            }

                            override fun onPageFinished(view: WebView?, target: String?) {
                                loading = false
                                target?.let { currentUrl = it }
                                updateHistory(view)
                            }

                            override fun doUpdateVisitedHistory(view: WebView?, target: String?, isReload: Boolean) {
                                super.doUpdateVisitedHistory(view, target, isReload)
                                target?.let { currentUrl = it }
                                updateHistory(view)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val target = request?.url ?: return true
                                if (isWebUrl(target.toString())) return false
                                failure = ReaderFailure("已拦截非网页链接", "为避免自动拉起第三方应用，阅读器不会打开 ${target.scheme ?: "未知"}:// 链接。")
                                loading = false
                                return true
                            }

                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                if (request?.isForMainFrame != true) return
                                loading = false
                                failure = ReaderFailure("文章加载失败", error?.description?.toString()?.takeIf { it.isNotBlank() } ?: "请检查网络后重试")
                            }

                            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, response: WebResourceResponse?) {
                                if (request?.isForMainFrame != true || (response?.statusCode ?: 0) < 400) return
                                loading = false
                                failure = ReaderFailure("网页返回 ${response?.statusCode}", response?.reasonPhrase?.takeIf { it.isNotBlank() } ?: "服务器暂时无法提供这篇文章")
                            }

                            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                handler?.cancel()
                                loading = false
                                failure = ReaderFailure("无法安全连接", "目标网站的安全证书无效或已过期，已停止加载。")
                            }

                            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                loading = false
                                failure = ReaderFailure("阅读器意外停止", "网页占用资源过多或渲染进程异常，请重新加载。")
                                view?.destroy()
                                webView = null
                                return true
                            }
                        }
                        loadUrl(initialUrl)
                        webView = this
                    }
                },
                update = { webView = it },
                modifier = Modifier.fillMaxSize()
            ) }

            if (loading && progress in 1..99) LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))

            when {
                failure != null -> ReaderMessage("LOAD ERROR", failure!!.title, failure!!.detail, "重新加载", ::retry, "浏览器打开", ::openExternally)
                slowLoading && !contentVisible -> ReaderMessage(
                    "SLOW CONNECTION",
                    "加载时间有点长",
                    "目标网站响应较慢，你可以继续等待、重新加载，或交给系统浏览器打开。",
                    "继续等待",
                    {
                        slowLoading = false
                        contentVisible = true
                    },
                    "浏览器打开",
                    ::openExternally,
                )
                loading && !contentVisible -> ReaderLoading(Uri.parse(currentUrl).host.orEmpty())
            }
        }
    }
}

@Composable
private fun ReaderLoading(host: String) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(32.dp), verticalArrangement = Arrangement.Center) {
            Text("CONNECTING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Text("正在打开文章", style = MaterialTheme.typography.headlineMedium)
            if (host.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    host,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(22.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ReaderMessage(eyebrow: String, title: String, detail: String, primaryLabel: String, onPrimary: () -> Unit, secondaryLabel: String, onSecondary: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(32.dp), verticalArrangement = Arrangement.Center) {
            Text(eyebrow, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(10.dp))
            Text(detail, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onPrimary) { Text(primaryLabel) }
                OutlinedButton(onClick = onSecondary) { Text(secondaryLabel) }
            }
        }
    }
}
