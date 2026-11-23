package com.jacky.wanandroid.di

import android.content.Context
import com.jacky.wanandroid.core.DefaultDispatcherProvider
import com.jacky.wanandroid.core.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/** 提供跨模块共享的应用级基础依赖。 */
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    companion object {
        /** 显式暴露 Application Context，避免业务对象持有 Activity。 */
        @Provides
        @Singleton
        fun provideApplicationContext(@ApplicationContext context: Context): Context = context
    }
}
