@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.jacky.wanandroid.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jacky.wanandroid.domain.model.Article

@Composable
fun WanPageHeader(
    title: String,
    kicker: String,
    modifier: Modifier = Modifier,
    navigation: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = if (navigation == null) 20.dp else 4.dp, end = 12.dp, top = 10.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        navigation?.invoke()
        Column(Modifier.weight(1f)) {
            Text(kicker.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        action?.invoke()
    }
}

@Composable
fun WanBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
    }
}

@Composable
fun WanSectionLabel(index: String, title: String, trailing: String? = null, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(index, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        trailing?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun WanArticleCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCollect: (() -> Unit)? = null,
    collectLabel: String = if (article.collected) "取消收藏" else "收藏"
) {
    val signal = when { article.top -> CompileAmber; article.fresh -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.outlineVariant }
    Surface(
        modifier = modifier.fillMaxWidth().clip(CutCornerShape(topEnd = 18.dp)).clickable(role = Role.Button, onClick = onClick),
        shape = CutCornerShape(topEnd = 18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.fillMaxHeight().width(5.dp).padding(vertical = 8.dp).clip(RoundedCornerShape(4.dp)).then(Modifier), contentAlignment = Alignment.Center) {
                Surface(color = signal, modifier = Modifier.fillMaxSize()) {}
            }
            Column(Modifier.weight(1f).padding(start = 15.dp, top = 15.dp, bottom = 15.dp, end = if (onCollect == null) 16.dp else 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val status = when { article.top -> "TOP"; article.fresh -> "NEW"; else -> article.superChapterName.ifBlank { article.chapterName }.uppercase().take(18) }
                    if (status.isNotBlank()) Text(status, style = MaterialTheme.typography.labelSmall, color = if (article.top) CompileAmber else MaterialTheme.colorScheme.primary)
                    if (article.niceDate.isNotBlank()) {
                        Spacer(Modifier.weight(1f))
                        Text(article.niceDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text(article.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (article.author.isNotBlank()) {
                    Spacer(Modifier.height(9.dp))
                    Text("BY ${article.author}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            onCollect?.let {
                IconButton(onClick = it, modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(if (article.collected) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, contentDescription = collectLabel, tint = if (article.collected) CompileAmber else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun WanStatusPane(
    message: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
        Text(if (loading) "SYNC" else "STATUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(10.dp))
        Text(message, style = MaterialTheme.typography.titleLarge)
        if (loading) {
            Spacer(Modifier.height(20.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}
