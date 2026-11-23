package com.jacky.wanandroid.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacky.wanandroid.data.local.entity.CacheEntity

@Dao
interface CacheDao {
    @Query("SELECT * FROM response_cache WHERE cacheKey = :key")
    suspend fun get(key: String): CacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(value: CacheEntity)

    @Query("DELETE FROM response_cache WHERE updatedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
