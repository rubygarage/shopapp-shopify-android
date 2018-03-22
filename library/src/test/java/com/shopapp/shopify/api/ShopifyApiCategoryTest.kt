package com.shopapp.shopify.api

import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.Category
import com.shopapp.gateway.entity.Error
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert
import org.junit.Test

class ShopifyApiCategoryTest : BaseShopifyApiTest() {

    @Test
    fun getCategoryDetailsShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontCollection = StorefrontMockInstantiator.newCollection()
        given(storefrontQueryRoot.node).willReturn(storefrontCollection)
        val storefrontShop = StorefrontMockInstantiator.newShop()
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<Category> = mock()
        api.getCategoryDetails(StorefrontMockInstantiator.DEFAULT_ID, any(), null, null, callback)

        argumentCaptor<Category>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ID, firstValue.id)
            Assert.assertTrue(firstValue.productList.isNotEmpty())
        }
    }

    @Test
    fun getCategoryDetailsShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<Category> = mock()
        api.getCategoryDetails(StorefrontMockInstantiator.DEFAULT_ID, any(), null, null, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getCategoryDetailsShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Category> = mock()
        api.getCategoryDetails(StorefrontMockInstantiator.DEFAULT_ID, any(), null, null, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getCategoryListShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<List<Category>> = mock()
        api.getCategoryList(any(), null, callback)

        argumentCaptor<List<Category>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertTrue(firstValue.isNotEmpty())
            val category = firstValue.first()
            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ID, category.id)
        }
    }

    @Test
    fun getCategoryListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<Category>> = mock()
        api.getCategoryList(any(), null, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getCategoryListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<Category>> = mock()
        api.getCategoryList(any(), null, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
        }
    }
}