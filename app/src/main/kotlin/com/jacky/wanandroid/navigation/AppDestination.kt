package com.jacky.wanandroid.navigation

import kotlinx.serialization.Serializable

@Serializable data object Home
@Serializable data object Knowledge
@Serializable data object Navigation
@Serializable data object Projects
@Serializable data object Profile
@Serializable data object Search
@Serializable data object Auth
@Serializable data object Collections
@Serializable data class WebArticle(val title: String, val url: String, val articleId: Long = 0)
