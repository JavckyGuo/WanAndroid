package com.jacky.wanandroid.data.local

import com.google.gson.Gson
import com.jacky.wanandroid.data.local.entity.CacheEntity
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheStore @Inject constructor(private val dao: CacheDao, private val gson: Gson) {
    suspend fun put(key: String, value: Any) = dao.put(CacheEntity(key, gson.toJson(value)))
    suspend fun <T> get(key: String, type: Type): T? = dao.get(key)?.let { runCatching { gson.fromJson<T>(it.json, type) }.getOrNull() }
}
