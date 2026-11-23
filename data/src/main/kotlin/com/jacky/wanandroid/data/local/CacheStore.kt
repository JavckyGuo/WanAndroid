package com.jacky.wanandroid.data.local

import com.google.gson.Gson
import com.jacky.wanandroid.data.local.entity.CacheEntity
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
/** 使用 Gson 将不同接口响应统一序列化到 Room 的键值缓存。 */
class CacheStore @Inject constructor(
    private val dao: CacheDao,
    private val gson: Gson
) {
    suspend fun put(key: String, value: Any) =
        dao.put(CacheEntity(key, gson.toJson(value)))

    /** 缓存损坏或模型不兼容时返回 null，让 Repository 继续抛出原始网络异常。 */
    suspend fun <T> get(key: String, type: Type): T? = dao.get(key)?.let {
        runCatching { gson.fromJson<T>(it.json, type) }.getOrNull()
    }
}
