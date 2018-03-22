package com.shopapp.shopify.api

import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.Error
import com.shopapp.gateway.entity.Product
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert
import org.junit.Test
import org.mockito.BDDMockito

class ShopifyApiProductTest : BaseShopifyApiTest() {

    @Test
    fun getProductShouldReturnProduct() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        BDDMockito.given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val storefrontProduct = StorefrontMockInstantiator.newProduct()
        BDDMockito.given(storefrontQueryRoot.node).willReturn(storefrontProduct)

        val callback: ApiCallback<Product> = mock()
        api.getProduct(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Product>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertEquals(storefrontProduct.id.toString(), firstValue.id)
            Assert.assertEquals(storefrontShop.paymentSettings.currencyCode.name, firstValue.currency)
        }
    }

    @Test
    fun getProductShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<Product> = mock()
        api.getProduct(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getProductShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Product> = mock()
        api.getProduct(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getProductListWithFullArgumentsShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        val storefrontProducts = StorefrontMockInstantiator.newProductConnection()
        BDDMockito.given(storefrontShop.products).willReturn(storefrontProducts)
        BDDMockito.given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<List<Product>> = mock()
        api.getProductList(any(), null, null, null, null, callback)

        argumentCaptor<List<Product>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertTrue(firstValue.isNotEmpty())
            val product = firstValue.first()
            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ID, product.id)
            Assert.assertEquals(storefrontShop.paymentSettings.currencyCode.name, product.currency)
        }
    }

    @Test
    fun getProductListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<Product>> = mock()
        api.getProductList(any(), null, null, "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getProductListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<Product>> = mock()
        api.getProductList(any(), null, null, "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun searchProductListShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        val storefrontProducts = StorefrontMockInstantiator.newProductConnection()
        BDDMockito.given(storefrontShop.products).willReturn(storefrontProducts)
        BDDMockito.given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<List<Product>> = mock()
        api.searchProductList(any(), null, "", callback)

        argumentCaptor<List<Product>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            Assert.assertTrue(firstValue.isNotEmpty())
            val product = firstValue.first()
            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ID, product.id)
            Assert.assertEquals(storefrontShop.paymentSettings.currencyCode.name, product.currency)
        }
    }

    @Test
    fun searchProductListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<Product>> = mock()
        api.searchProductList(any(), null, "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            Assert.assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun searchProductListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<Product>> = mock()
        api.searchProductList(any(), null, "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            Assert.assertTrue(firstValue is Error.Content)
        }
    }
}