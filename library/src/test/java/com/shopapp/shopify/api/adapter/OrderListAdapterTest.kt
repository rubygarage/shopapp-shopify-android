package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
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
    fun shouldReturnEmptyList() {
        val result = OrderListAdapter.adapt(null)
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }
}