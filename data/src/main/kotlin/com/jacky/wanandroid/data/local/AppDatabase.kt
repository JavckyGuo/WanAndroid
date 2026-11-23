package com.jacky.wanandroid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jacky.wanandroid.data.local.entity.CacheEntity

/**
 * 应用 Room 数据库。
 *
 * 当前只保存通用响应缓存；新增实体或修改表结构时必须提升版本并提供迁移。
 */
@Database(
    entities = [CacheEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    /** 提供响应缓存表的数据访问对象。 */
    abstract fun cacheDao(): CacheDao
}
