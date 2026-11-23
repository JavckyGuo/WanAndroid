package com.jacky.wanandroid.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jacky.wanandroid.data.local.entity.CacheEntity

@Dao
/** 通用响应缓存表的最小读写接口。 */
interface CacheDao {
    @Query("SELECT * FROM response_cache WHERE cacheKey = :key")
    suspend fun get(key: String): CacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(value: CacheEntity)

    @Query("DELETE FROM response_cache WHERE updatedAt < :cutoff")
    /** 删除指定时间之前的缓存，为后续缓存淘汰策略预留入口。 */
    suspend fun deleteOlderThan(cutoff: Long)
}
