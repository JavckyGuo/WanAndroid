package com.jacky.wanandroid.data.remote.dto

/** WanAndroid 所有接口共用的业务响应信封。 */
data class ApiResponse<T>(
    val data: T?,
    val errorCode: Int = 0,
    val errorMsg: String? = null
)

/** 服务端分页响应；不同接口的页码起点由 Repository 统一适配。 */
data class PageDto<T>(
    val curPage: Int = 0,
    val pageCount: Int = 0,
    val total: Int = 0,
    val datas: List<T> = emptyList()
)

/** 文章网络模型，可空字段只在 Data 层存在。 */
data class ArticleDto(
    val id: Long = 0,
    val originId: Long = -1,
    val title: String? = null,
    val link: String? = null,
    val author: String? = null,
    val shareUser: String? = null,
    val chapterName: String? = null,
    val superChapterName: String? = null,
    val niceDate: String? = null,
    val desc: String? = null,
    val envelopePic: String? = null,
    val collect: Boolean = false,
    val fresh: Boolean = false
)

/** 首页 Banner 网络模型。 */
data class BannerDto(
    val id: Long = 0,
    val title: String? = null,
    val imagePath: String? = null,
    val url: String? = null
)

/** 知识体系或项目分类网络模型。 */
data class CategoryDto(
    val id: Long = 0,
    val name: String? = null,
    val children: List<CategoryDto>? = null
)

/** 导航分组及其文章。 */
data class NavigationDto(
    val name: String? = null,
    val articles: List<ArticleDto>? = null
)

/** 搜索热词网络模型。 */
data class HotKeyDto(val id: Long = 0, val name: String? = null)

/** 用户资料网络模型。 */
data class UserDto(
    val id: Long = 0,
    val username: String? = null,
    val nickname: String? = null,
    val email: String? = null,
    val icon: String? = null
)

/** 用户积分网络模型。 */
data class CoinInfoDto(
    val userId: Long = 0,
    val username: String? = null,
    val coinCount: Int = 0,
    val level: Int = 0,
    val rank: String? = null
)

/** 个人中心接口返回的用户与积分聚合对象。 */
data class UserInfoDto(
    val userInfo: UserDto?,
    val coinInfo: CoinInfoDto?
)
