package com.shopapp.shopify.api.adapter

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopify.buy3.Storefront
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class OrderListAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromOrderListStorefrontToOrderList() {
        val result = OrderListAdapter.adapt(StorefrontMockInstantiator.newOrderConnection())
        assertEquals(1, result.size)
        assertNotNull(result.first())
    }

    @Test
    fun shouldReturnEmptyListOnNullConnection() {
        val result = OrderListAdapter.adapt(null)
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun shouldReturnEmptyListOnNullEdges() {
        val edgesMock: List<Storefront.OrderEdge>? = null
        val connection: Storefront.OrderConnection = mock {
            on { edges } doReturn edgesMock
        }
        val result = OrderListAdapter.adapt(connection)
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }
}