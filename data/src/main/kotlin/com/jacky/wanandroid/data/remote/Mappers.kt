package com.jacky.wanandroid.data.remote

import com.jacky.wanandroid.data.remote.dto.*
import com.jacky.wanandroid.domain.model.*
import com.jacky.wanandroid.domain.repository.ApiException
import com.jacky.wanandroid.domain.repository.SessionExpiredException

/**
 * 解开 WanAndroid 响应信封。
 *
 * errorCode 只有 0 表示成功；-1001 单独映射为会话失效，便于 Repository 清理登录态。
 */
fun <T> ApiResponse<T>.requireData(): T {
    if (errorCode == -1001) throw SessionExpiredException(errorMsg.orEmpty())
    if (errorCode != 0) {
        throw ApiException(errorCode, errorMsg.orEmpty().ifBlank { "请求失败" })
    }
    return data ?: throw ApiException(-1, "服务器未返回数据")
}

/** 校验没有有效数据体的操作型接口，如收藏和退出登录。 */
fun ApiResponse<*>.requireSuccess() {
    if (errorCode == -1001) throw SessionExpiredException(errorMsg.orEmpty())
    if (errorCode != 0) {
        throw ApiException(errorCode, errorMsg.orEmpty().ifBlank { "请求失败" })
    }
}

/** 将可空网络文章转换为字段安全的领域模型。 */
fun ArticleDto.toDomain(top: Boolean = false) = Article(
    id = id,
    originId = originId,
    title = title.orEmpty(),
    link = link.orEmpty(),
    // author 优先，分享文章则回退 shareUser。
    author = author.orEmpty().ifBlank { shareUser.orEmpty() },
    chapterName = chapterName.orEmpty(),
    superChapterName = superChapterName.orEmpty(),
    niceDate = niceDate.orEmpty(),
    description = desc.orEmpty(),
    envelopePic = envelopePic.orEmpty(),
    collected = collect,
    fresh = fresh,
    top = top
)

fun BannerDto.toDomain() = Banner(
    id,
    title.orEmpty(),
    imagePath.orEmpty(),
    url.orEmpty()
)

fun CategoryDto.toDomain(): Category = Category(
    id,
    name.orEmpty(),
    children.orEmpty().map { it.toDomain() }
)

fun UserDto.toDomain() = UserProfile(
    id,
    username.orEmpty(),
    nickname.orEmpty(),
    email.orEmpty(),
    icon.orEmpty()
)

fun CoinInfoDto.toDomain() = CoinInfo(
    userId,
    username.orEmpty(),
    coinCount,
    level,
    rank.orEmpty().ifBlank { "-" }
)

/**
 * 将分页 DTO 转换为领域分页。
 *
 * 项目等 1 起始页接口使用 requestedPage < pageCount；首页、搜索等 0 起始页接口
 * 需要减去最后一页的偏移，避免额外请求空页。
 */
fun PageDto<ArticleDto>.toDomain(
    requestedPage: Int,
    oneBased: Boolean,
    fromCache: Boolean = false
) = Page(
    items = datas.map { it.toDomain() },
    page = requestedPage,
    pageCount = pageCount,
    total = total,
    fromCache = fromCache,
    hasMore = if (oneBased) {
        requestedPage < pageCount
    } else {
        requestedPage < pageCount - 1
    }
)
