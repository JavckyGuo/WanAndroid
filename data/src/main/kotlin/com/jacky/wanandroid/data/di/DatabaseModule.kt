package com.jacky.wanandroid.data.di

import android.content.Context
import androidx.room.Room
import com.jacky.wanandroid.data.local.AppDatabase
import com.jacky.wanandroid.data.local.CacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/** 创建 Room 数据库及其 DAO，生命周期与应用进程一致。 */
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wanandroid_db")
            .build()

    @Provides
    fun provideCacheDao(db: AppDatabase): CacheDao = db.cacheDao()
}
