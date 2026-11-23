package com.jacky.wanandroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "response_cache")
/** 以稳定业务键保存 JSON 响应及其更新时间。 */
data class CacheEntity(
    @PrimaryKey val cacheKey: String,
    val json: String,
    val updatedAt: Long = System.currentTimeMillis()
)
