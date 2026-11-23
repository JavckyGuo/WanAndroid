package com.jacky.wanandroid.domain.model

/**
 * UI 与业务层使用的文章模型。
 *
 * [title] 保留服务端 HTML，以便搜索结果渲染命中词；阅读器标题会在导航时转换为纯文本。
 * [originId] 用于区分站内文章与收藏列表返回的收藏记录。
 */
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

/** 首页轮播条目。 */
data class Banner(val id: Long, val title: String, val imageUrl: String, val url: String)

/** 知识体系分类；一级分类通过 [children] 持有二级分类。 */
data class Category(
    val id: Long,
    val name: String,
    val children: List<Category> = emptyList()
)

/** 导航页中同一主题下的文章集合。 */
data class NavigationGroup(val name: String, val articles: List<Article>)

/** 项目频道分类。 */
data class ProjectCategory(val id: Long, val name: String)

/** 搜索页热词。 */
data class HotKey(val id: Long, val name: String)

/** 当前登录用户的非敏感资料摘要。 */
data class UserProfile(
    val id: Long,
    val username: String,
    val nickname: String = "",
    val email: String = "",
    val icon: String = ""
) {
    val displayName: String get() = nickname.ifBlank { username }
}

/** 用户积分、等级与站内排名。 */
data class CoinInfo(
    val userId: Long = 0,
    val username: String = "",
    val coinCount: Int = 0,
    val level: Int = 0,
    val rank: String = "-"
)

/** 个人中心一次展示所需的用户资料和积分信息。 */
data class UserDashboard(val profile: UserProfile, val coin: CoinInfo)

/**
 * 统一分页模型。
 *
 * [page] 始终表示调用方请求的页码，[fromCache] 用于提示离线回退，
 * [hasMore] 已由 Data 层根据不同接口的 0/1 起始页规则计算。
 */
data class Page<T>(
    val items: List<T>,
    val page: Int,
    val pageCount: Int,
    val total: Int,
    val fromCache: Boolean = false,
    val hasMore: Boolean = false
)

/** 首页轮播与文章流的聚合结果。 */
data class HomeContent(val banners: List<Banner>, val articles: Page<Article>)

/** 设备级主题偏好。 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }
