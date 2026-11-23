package com.jacky.wanandroid.domain.model

data class Article(
    val id: Long,
    val title: String,
    val link: String,
    val author: String,
    val chapterName: String = "",
    val superChapterName: String = "",
    val niceDate: String = "",
    val description: String = "",
    val envelopePic: String = "",
    val collected: Boolean = false,
    val fresh: Boolean = false,
    val top: Boolean = false,
    val originId: Long = -1
)

data class Banner(val id: Long, val title: String, val imageUrl: String, val url: String)

data class Category(
    val id: Long,
    val name: String,
    val children: List<Category> = emptyList()
)

data class NavigationGroup(val name: String, val articles: List<Article>)
data class ProjectCategory(val id: Long, val name: String)
data class HotKey(val id: Long, val name: String)

data class UserProfile(
    val id: Long,
    val username: String,
    val nickname: String = "",
    val email: String = "",
    val icon: String = ""
) {
    val displayName: String get() = nickname.ifBlank { username }
}

data class CoinInfo(
    val userId: Long = 0,
    val username: String = "",
    val coinCount: Int = 0,
    val level: Int = 0,
    val rank: String = "-"
)

data class UserDashboard(val profile: UserProfile, val coin: CoinInfo)

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val pageCount: Int,
    val total: Int,
    val fromCache: Boolean = false,
    val hasMore: Boolean = false
)

data class HomeContent(val banners: List<Banner>, val articles: Page<Article>)

enum class ThemeMode { SYSTEM, LIGHT, DARK }
