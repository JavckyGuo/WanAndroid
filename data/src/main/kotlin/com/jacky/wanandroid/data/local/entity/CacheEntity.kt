package com.jacky.wanandroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "response_cache")
data class CacheEntity(
    @PrimaryKey val cacheKey: String,
    val json: String,
    val updatedAt: Long = System.currentTimeMillis()
)
