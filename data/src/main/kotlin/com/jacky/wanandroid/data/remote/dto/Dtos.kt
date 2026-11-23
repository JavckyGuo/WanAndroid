package com.jacky.wanandroid.data.remote.dto

data class ApiResponse<T>(val data: T?, val errorCode: Int, val errorMsg: String?)
data class PageDto<T>(
    val curPage: Int = 1,
    val pageCount: Int = 1,
    val total: Int = 0,
    val datas: List<T> = emptyList()
)
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
data class BannerDto(val id: Long = 0, val title: String? = null, val imagePath: String? = null, val url: String? = null)
data class CategoryDto(val id: Long = 0, val name: String? = null, val children: List<CategoryDto>? = null)
data class NavigationDto(val name: String? = null, val articles: List<ArticleDto>? = null)
data class HotKeyDto(val id: Long = 0, val name: String? = null)
data class UserDto(
    val id: Long = 0, val username: String? = null, val nickname: String? = null,
    val email: String? = null, val icon: String? = null
)
data class CoinInfoDto(
    val userId: Long = 0, val username: String? = null, val coinCount: Int = 0,
    val level: Int = 0, val rank: String? = null
)
data class UserInfoDto(val userInfo: UserDto?, val coinInfo: CoinInfoDto?)
