package com.jacky.wanandroid.feature.home

import com.jacky.wanandroid.core.util.FakeDispatcherProvider
import com.jacky.wanandroid.core.util.MainDispatcherRule
import com.jacky.wanandroid.domain.model.*
import com.jacky.wanandroid.domain.repository.ArticleRepository
import com.jacky.wanandroid.domain.repository.CollectionRepository
import com.jacky.wanandroid.domain.usecase.LoadHomeUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun `initial load exposes articles and cache state`() = runTest {
        val articleRepository = mockk<ArticleRepository>()
        val collectionRepository = mockk<CollectionRepository>(relaxed = true)
        coEvery { articleRepository.home(0) } returns HomeContent(
            banners = listOf(Banner(1, "Banner", "image", "url")),
            articles = Page(listOf(Article(7, "Title", "link", "author")), 0, 2, 21, fromCache = true, hasMore = true)
        )
        val viewModel = HomeViewModel(LoadHomeUseCase(articleRepository), collectionRepository, FakeDispatcherProvider())
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Title", state.articles.single().title)
        assertTrue(state.fromCache)
        assertTrue(state.hasMore)
    }

    @Test fun `refresh error is represented in state`() = runTest {
        val articleRepository = mockk<ArticleRepository>()
        coEvery { articleRepository.home(0) } throws IllegalStateException("offline")
        val viewModel = HomeViewModel(LoadHomeUseCase(articleRepository), mockk(relaxed = true), FakeDispatcherProvider())
        assertEquals("offline", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
