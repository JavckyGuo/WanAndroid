package com.jacky.wanandroid.data.remote

import com.google.gson.Gson
import com.jacky.wanandroid.data.remote.dto.ApiResponse
import com.jacky.wanandroid.data.remote.dto.ArticleDto
import com.jacky.wanandroid.data.remote.dto.PageDto
import com.jacky.wanandroid.domain.repository.ApiException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** 验证网络响应解包、分页 URL 和服务端业务错误映射。 */
class WanApiTest {
    @Test
    fun `home page keeps zero based request and unwraps envelope`() = runTest {
        // 准备一个可记录请求路径的本地 HTTP 服务。
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(
                MockResponse()
                    .setBody(
                        """{"data":{"curPage":1,"pageCount":2,"total":21,"datas":[{"id":9,"title":"Hello","link":"https://example.com"}]},"errorCode":0,"errorMsg":""}"""
                    )
                    .setHeader("Content-Type", "application/json")
            )
            val api = Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .build()
                .create(WanApi::class.java)

            // 执行第 0 页请求并验证信封数据与实际 URL。
            val page = api.articles(0).requireData()
            assertEquals("Hello", page.datas.single().title)
            assertEquals("/article/list/0/json?page_size=20", server.takeRequest().path)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `page adapter handles both paging origins`() {
        val dto = PageDto(
            pageCount = 2,
            total = 1,
            datas = listOf(ArticleDto(id = 1, title = "A", link = "u"))
        )

        // 验证 0 起始和 1 起始接口都在各自最后一页停止继续加载。
        assertTrue(dto.toDomain(0, oneBased = false).hasMore)
        assertFalse(dto.toDomain(1, oneBased = false).hasMore)
        assertTrue(dto.toDomain(1, oneBased = true).hasMore)
        assertFalse(dto.toDomain(2, oneBased = true).hasMore)
    }

    @Test
    fun `non zero error code throws domain error`() {
        val response = ApiResponse<String>(null, -1, "bad")

        val error = runCatching { response.requireData() }.exceptionOrNull()

        assertTrue(error is ApiException)
        assertEquals("bad", error?.message)
    }
}
