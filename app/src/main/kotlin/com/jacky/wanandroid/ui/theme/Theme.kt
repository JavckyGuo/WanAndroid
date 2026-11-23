package com.jacky.wanandroid.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import com.jacky.wanandroid.core.designsystem.WanTheme
import com.jacky.wanandroid.domain.model.ThemeMode

/** 根据持久化主题模式选择浅色或深色方案，并应用到整个 Compose 树。 */
@Composable
fun WanAndroidTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    WanTheme(darkTheme = darkTheme, content = content)
}
