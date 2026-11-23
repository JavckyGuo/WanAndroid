package com.jacky.wanandroid.data.remote

import com.jacky.wanandroid.data.remote.dto.*
import com.jacky.wanandroid.domain.model.*
import com.jacky.wanandroid.domain.repository.ApiException
import com.jacky.wanandroid.domain.repository.SessionExpiredException

fun <T> ApiResponse<T>.requireData(): T {
    if (errorCode == -1001) throw SessionExpiredException(errorMsg.orEmpty())
    if (errorCode != 0) throw ApiException(errorCode, errorMsg.orEmpty().ifBlank { "请求失败" })
    return data ?: throw ApiException(-1, "服务器未返回数据")
}
fun ApiResponse<*>.requireSuccess() {
    if (errorCode == -1001) throw SessionExpiredException(errorMsg.orEmpty())
    if (errorCode != 0) throw ApiException(errorCode, errorMsg.orEmpty().ifBlank { "请求失败" })
}
fun ArticleDto.toDomain(top: Boolean = false) = Article(
    id = id, originId = originId, title = title.orEmpty(), link = link.orEmpty(),
    author = author.orEmpty().ifBlank { shareUser.orEmpty() }, chapterName = chapterName.orEmpty(),
    superChapterName = superChapterName.orEmpty(), niceDate = niceDate.orEmpty(),
    description = desc.orEmpty(), envelopePic = envelopePic.orEmpty(), collected = collect, fresh = fresh, top = top
)
fun BannerDto.toDomain() = Banner(id, title.orEmpty(), imagePath.orEmpty(), url.orEmpty())
fun CategoryDto.toDomain(): Category = Category(id, name.orEmpty(), children.orEmpty().map { it.toDomain() })
fun UserDto.toDomain() = UserProfile(id, username.orEmpty(), nickname.orEmpty(), email.orEmpty(), icon.orEmpty())
fun CoinInfoDto.toDomain() = CoinInfo(userId, username.orEmpty(), coinCount, level, rank.orEmpty().ifBlank { "-" })
fun PageDto<ArticleDto>.toDomain(requestedPage: Int, oneBased: Boolean, fromCache: Boolean = false) = Page(
    items = datas.map { it.toDomain() }, page = requestedPage, pageCount = pageCount, total = total,
    fromCache = fromCache, hasMore = if (oneBased) requestedPage < pageCount else requestedPage < pageCount - 1
)
