package com.shopapp.shopify.api

import com.nhaarman.mockito_kotlin.*
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.Error
import com.shopapp.gateway.entity.Order
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopapp.shopify.constant.Constant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopifyApiOrderTest : BaseShopifyApiTest() {

    @Test
    fun getOrdersShouldReturnOrderList() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontOrderConnection = StorefrontMockInstantiator.newOrderConnection()
        val customer = StorefrontMockInstantiator.newCustomer()
        given(storefrontQueryRoot.customer).willReturn(customer)
        given(customer.orders).willReturn(storefrontOrderConnection)

        mockSession(true)
        val callback: ApiCallback<List<Order>> = mock()
        api.getOrders(any(), anyOrNull(), callback)

        argumentCaptor<List<Order>>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertTrue(firstValue.isNotEmpty())
        }
    }

    @Test
    fun getOrdersShouldReturnNonCriticalError() {
        val graphResponse = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        mockSession(true)
        val callback: ApiCallback<List<Order>> = mock()
        api.getOrders(any(), anyOrNull(), callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getOrdersShouldReturnNonCriticalWhenUserNotAuthorized() {
        mockSession(false)
        val callback: ApiCallback<List<Order>> = mock()
        api.getOrders(any(), anyOrNull(), callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(Constant.UNAUTHORIZED_ERROR, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getOrdersShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        mockSession(true)
        val callback: ApiCallback<List<Order>> = mock()
        api.getOrders(any(), anyOrNull(), callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getOrderShouldReturnOrder() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val storefrontOrder = StorefrontMockInstantiator.newOrder()
        given(storefrontQueryRoot.node).willReturn(storefrontOrder)

        val callback: ApiCallback<Order> = mock()
        api.getOrder(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Order>().apply {
            verify(callback).onResult(capture())
            verify(callback, never()).onFailure(any())

            assertEquals(storefrontOrder.id.toString(), firstValue.id)
        }
    }

    @Test
    fun getOrderShouldReturnNonCriticalError() {
        val graphResponse = mockErrorResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        val callback: ApiCallback<Order> = mock()
        api.getOrder(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertEquals(StorefrontMockInstantiator.DEFAULT_ERROR_MESSAGE, firstValue.message)
            assertTrue(firstValue is Error.NonCritical)
        }
    }

    @Test
    fun getOrderShouldReturnContentError() {
        mockQueryGraphCallWithOnFailure()

        val callback: ApiCallback<Order> = mock()
        api.getOrder(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Content)
        }
    }

    @Test
    fun getOrderShouldReturnCriticalError() {
        val (graphResponse, storefrontQueryRoot) = mockDataResponse()
        mockQueryGraphCallWithOnResponse(graphResponse)

        given(storefrontQueryRoot.node).willReturn(null)
        val callback: ApiCallback<Order> = mock()
        api.getOrder(StorefrontMockInstantiator.DEFAULT_ID, callback)

        argumentCaptor<Error>().apply {
            verify(callback, never()).onResult(any())
            verify(callback).onFailure(capture())

            assertTrue(firstValue is Error.Critical)
        }
    }
}