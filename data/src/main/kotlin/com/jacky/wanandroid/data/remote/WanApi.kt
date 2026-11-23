package com.jacky.wanandroid.data.remote

import com.jacky.wanandroid.data.remote.dto.*
import retrofit2.http.*

interface WanApi {
    @GET("banner/json") suspend fun banners(): ApiResponse<List<BannerDto>>
    @GET("article/top/json") suspend fun topArticles(): ApiResponse<List<ArticleDto>>
    @GET("article/list/{page}/json") suspend fun articles(
        @Path("page") page: Int, @Query("cid") categoryId: Long? = null, @Query("page_size") pageSize: Int = 20
    ): ApiResponse<PageDto<ArticleDto>>
    @GET("tree/json") suspend fun categories(): ApiResponse<List<CategoryDto>>
    @GET("navi/json") suspend fun navigation(): ApiResponse<List<NavigationDto>>
    @GET("project/tree/json") suspend fun projectCategories(): ApiResponse<List<CategoryDto>>
    @GET("project/list/{page}/json") suspend fun projects(
        @Path("page") page: Int, @Query("cid") categoryId: Long, @Query("page_size") pageSize: Int = 20
    ): ApiResponse<PageDto<ArticleDto>>
    @GET("hotkey/json") suspend fun hotKeys(): ApiResponse<List<HotKeyDto>>
    @FormUrlEncoded @POST("article/query/{page}/json") suspend fun search(
        @Path("page") page: Int, @Field("k") keyword: String, @Query("page_size") pageSize: Int = 20
    ): ApiResponse<PageDto<ArticleDto>>
    @FormUrlEncoded @POST("user/login") suspend fun login(
        @Field("username") username: String, @Field("password") password: String
    ): ApiResponse<UserDto>
    @FormUrlEncoded @POST("user/register") suspend fun register(
        @Field("username") username: String, @Field("password") password: String,
        @Field("repassword") repeatedPassword: String
    ): ApiResponse<UserDto>
    @GET("user/logout/json") suspend fun logout(): ApiResponse<Any?>
    @GET("lg/collect/list/{page}/json") suspend fun collections(
        @Path("page") page: Int, @Query("page_size") pageSize: Int = 20
    ): ApiResponse<PageDto<ArticleDto>>
    @POST("lg/collect/{id}/json") suspend fun collect(@Path("id") articleId: Long): ApiResponse<Any?>
    @POST("lg/uncollect_originId/{id}/json") suspend fun uncollectOrigin(@Path("id") articleId: Long): ApiResponse<Any?>
    @FormUrlEncoded @POST("lg/uncollect/{id}/json") suspend fun uncollectSaved(
        @Path("id") articleId: Long, @Field("originId") originId: Long
    ): ApiResponse<Any?>
    @GET("user/lg/userinfo/json") suspend fun userInfo(): ApiResponse<UserInfoDto>
}
