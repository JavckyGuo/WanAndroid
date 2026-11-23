package com.jacky.wanandroid.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.jacky.wanandroid.data.repository.*
import com.jacky.wanandroid.domain.repository.*
import dagger.Binds
import javax.inject.Singleton

// Add @Binds @Singleton abstract bindings here as you create repository implementations.
// Example:
//
// @Module
// @InstallIn(SingletonComponent::class)
// abstract class RepositoryModule {
//     @Binds @Singleton
//     abstract fun bindFooRepository(impl: FooRepositoryImpl): FooRepository
// }

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindArticleRepository(impl: ArticleRepositoryImpl): ArticleRepository
    @Binds @Singleton abstract fun bindDiscoveryRepository(impl: DiscoveryRepositoryImpl): DiscoveryRepository
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository
    @Binds @Singleton abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
