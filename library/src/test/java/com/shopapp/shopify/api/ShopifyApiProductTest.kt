package com.shopapp.shopify.api

import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.Error
import com.shopapp.gateway.entity.Product
import com.shopapp.gateway.entity.ProductVariant
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopifyApiProductTest : BaseShopifyApiTest() {

    @Test
    fun getProductShouldReturnProduct() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val storefrontProduct = StorefrontMockInstantiator.newProduct()
        given(storefrontQueryRoot.node).willReturn(storefrontProduct)

        val callback: ApiCallback<Product> = mock()
        api.getProduct(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Product>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontProduct.id.toString(), firstValue.id)
            assertEquals(storefrontShop.paymentSettings.currencyCode.name, firstValue.currency)
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

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
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

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getProductShouldReturnCriticalError() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)
        given(storefrontQueryRoot.node).willReturn(null)

        val callback: ApiCallback<Product> = mock()
        api.getProduct(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Critical)
        }
    }

    @Test
    fun getProductVariantListWithFullArgumentsShouldReturnProductVariantList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        val storefrontProductVariantList = listOf(StorefrontMockInstantiator.newProductVariant())
        given(storefrontQueryRoot.nodes).willReturn(storefrontProductVariantList)
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<List<ProductVariant>> = mock()
        api.getProductVariants(listOf(), callback)

        argumentCaptor<List<ProductVariant>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
            val productVariant = firstValue.first()
            assertEquals(StorefrontMockInstantiator.DEFAULT_ID, productVariant.id)
            assertEquals(storefrontProductVariantList.first().title, productVariant.title)
        }
    }

    @Test
    fun getProductVariantListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<ProductVariant>> = mock()
        api.getProductVariants(listOf(), callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getProductVariantListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<ProductVariant>> = mock()
        api.getProductVariants(listOf(), callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getProductListWithFullArgumentsShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        val storefrontProducts = StorefrontMockInstantiator.newProductConnection()
        given(storefrontShop.products).willReturn(storefrontProducts)
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<List<Product>> = mock()
        api.getProducts(any(), null, null, null, null, callback)

        argumentCaptor<List<Product>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
            val product = firstValue.first()
            assertEquals(StorefrontMockInstantiator.DEFAULT_ID, product.id)
            assertEquals(storefrontShop.paymentSettings.currencyCode.name, product.currency)
        }
    }

    @Test
    fun getProductListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<Product>> = mock()
        api.getProducts(any(), null, null, "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getProductListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<Product>> = mock()
        api.getProducts(any(), null, null, "", "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun searchProductListShouldReturnProductList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontShop = StorefrontMockInstantiator.newShop()
        val storefrontProducts = StorefrontMockInstantiator.newProductConnection()
        given(storefrontShop.products).willReturn(storefrontProducts)
        given(storefrontQueryRoot.shop).willReturn(storefrontShop)

        val callback: ApiCallback<List<Product>> = mock()
        api.searchProducts(any(), null, "", callback)

        argumentCaptor<List<Product>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
            val product = firstValue.first()
            assertEquals(StorefrontMockInstantiator.DEFAULT_ID, product.id)
            assertEquals(storefrontShop.paymentSettings.currencyCode.name, product.currency)
        }
    }

    @Test
    fun searchProductListShouldReturnNonCriticalError() {
        val response = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(response)

        val callback: ApiCallback<List<Product>> = mock()
        api.searchProducts(any(), null, "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun searchProductListShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<List<Product>> = mock()
        api.searchProducts(any(), null, "", callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }
}