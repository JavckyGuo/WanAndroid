package com.jacky.wanandroid.navigation

import kotlinx.serialization.Serializable

/** 应用类型安全导航的顶级目的地。 */
@Serializable
data object Home

@Serializable
data object Knowledge

@Serializable
data object Navigation

@Serializable
data object Projects

@Serializable
data object Profile

/** 从首页工具栏进入的全站搜索页。 */
@Serializable
data object Search

/** 登录与注册共用的账号入口。 */
@Serializable
data object Auth

/** 当前用户的文章收藏列表。 */
@Serializable
data object Collections

/**
 * 应用内文章阅读器路由。
 *
 * 标题在创建路由前已转为纯文本，避免搜索结果中的 HTML 高亮标签进入 WebView 顶栏。
 */
@Serializable
data class WebArticle(
    val title: String,
    val url: String,
    val articleId: Long = 0
)
