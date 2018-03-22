package com.shopapp.shopify.api

import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.Article
import com.shopapp.gateway.entity.Error
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopify.buy3.Storefront
import org.junit.Assert
import org.junit.Test
import org.mockito.BDDMockito

class ShopifyApiBlogTest : BaseShopifyApiTest() {

    @Test
    fun getArticleShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontArticle = StorefrontMockInstantiator.newArticle()
        BDDMockito.given(storefrontQueryRoot.node).willReturn(storefrontArticle)

        val callback: ApiCallback<Pair<Article, String>> = mock()
        api.getArticle(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Pair<Article, String>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            val (article, url) = firstValue
            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ID, article.id)
            Assert.assertEquals(BASE_URL, url)
        }
    }

    @Test
    fun getArticleShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<Pair<Article, String>> = mock()
        api.getArticle(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getArticleShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Pair<Article, String>> = mock()
        api.getArticle(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getArticleListShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontArticleEdges = listOf(StorefrontMockInstantiator.newArticleEdge())
        val storefrontArticleConnection: Storefront.ArticleConnection = mock()
        BDDMockito.given(storefrontArticleConnection.edges).willReturn(storefrontArticleEdges)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        BDDMockito.given(storefrontQueryRoot.shop).willReturn(storefrontShop)
        BDDMockito.given(storefrontShop.articles).willReturn(storefrontArticleConnection)

        val callback: ApiCallback<List<Article>> = mock()
        api.getArticleList(any(), null, null, false, callback)

        argumentCaptor<List<Article>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertTrue(firstValue.isNotEmpty())
            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ID, firstValue.first().id)
        }
    }

    @Test
    fun getArticleListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<Article>> = mock()
        api.getArticleList(any(), null, null, false, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getArticleListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<Article>> = mock()
        api.getArticleList(any(), null, null, false, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
        }
    }
}