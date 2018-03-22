package com.shopapp.shopify.api

import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.Error
import com.shopapp.gateway.entity.Shop
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert
import org.junit.Test

class ShopifyApiShopTest : BaseShopifyApiTest() {

    @Test
    fun getShopInfoShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<Shop> = mock()
        api.getShopInfo(callback)

        argumentCaptor<Shop>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_SHOP_NAME, firstValue.name)
        }
    }

    @Test
    fun getShopInfoShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<Shop> = mock()
        api.getShopInfo(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getShopInfoShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Shop> = mock()
        api.getShopInfo(callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
        }
    }
}