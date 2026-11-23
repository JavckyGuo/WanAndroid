package com.jacky.wanandroid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jacky.wanandroid.data.local.entity.CacheEntity

// Room's KSP processor rejects an empty entities array ("@Database annotation must specify
// list of entities"), so the project would not compile. PlaceholderEntity keeps the database
// valid and building from day one. Add your real entities to the array and delete
// PlaceholderEntity once you have them. Increment version on schema changes; add a migration.
@Database(
    entities = [CacheEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
