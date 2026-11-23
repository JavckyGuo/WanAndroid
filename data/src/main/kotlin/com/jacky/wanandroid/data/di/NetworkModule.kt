package com.jacky.wanandroid.data.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import com.jacky.wanandroid.data.remote.EncryptedCookieJar
import com.jacky.wanandroid.data.remote.WanApi
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
/** 组装 WanAndroid 网络栈及统一 JSON 解析器。 */
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(cookieJar: EncryptedCookieJar): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(HttpLoggingInterceptor().apply {
            // BASIC 不记录表单正文，避免登录密码进入 Logcat。
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://wanandroid.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideWanApi(retrofit: Retrofit): WanApi = retrofit.create(WanApi::class.java)
}
