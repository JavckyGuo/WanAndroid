package com.jacky.wanandroid.feature.profile

import androidx.compose.runtime.*
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.jacky.wanandroid.core.designsystem.WanTheme
import com.jacky.wanandroid.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** 验证未登录用户仍能打开主题弹层并选择深色模式。 */
class ProfileThemeUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loggedOutUserCanOpenThemeSelectorAndChooseDark() {
        var selected = ThemeMode.SYSTEM
        composeRule.setContent {
            var showSheet by remember { mutableStateOf(false) }
            var mode by remember { mutableStateOf(ThemeMode.SYSTEM) }
            selected = mode
            WanTheme(darkTheme = mode == ThemeMode.DARK) {
                ProfileContent(
                    state = ProfileUiState(themeMode = mode),
                    onLogin = {},
                    onCollections = {},
                    onThemeClick = { showSheet = true },
                    onRetry = {},
                    onLogout = {},
                )
                if (showSheet) {
                    ThemeModeSheet(
                        selected = mode,
                        onSelect = {
                            mode = it
                            showSheet = false
                        },
                        onDismiss = { showSheet = false },
                    )
                }
            }
        }

        composeRule.onNodeWithTag("theme_setting").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("theme_dark").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(ThemeMode.DARK, selected) }
    }
}
