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
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    companion object {
        @Provides
        @Singleton
        fun provideApplicationContext(@ApplicationContext context: Context): Context = context
    }
}
