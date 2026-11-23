package com.jacky.wanandroid.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.jacky.wanandroid.data.repository.*
import com.jacky.wanandroid.domain.repository.*
import dagger.Binds
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/** 将 Domain Repository 接口绑定到 Data 层的单例实现。 */
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindArticleRepository(impl: ArticleRepositoryImpl): ArticleRepository

    @Binds
    @Singleton
    abstract fun bindDiscoveryRepository(impl: DiscoveryRepositoryImpl): DiscoveryRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
