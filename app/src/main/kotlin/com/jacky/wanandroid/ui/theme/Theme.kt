package com.jacky.wanandroid.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import com.jacky.wanandroid.core.designsystem.WanTheme
import com.jacky.wanandroid.domain.model.ThemeMode

@Composable
fun WanAndroidTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    WanTheme(darkTheme = darkTheme, content = content)
}
