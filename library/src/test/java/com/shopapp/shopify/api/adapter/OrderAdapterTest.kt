package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class OrderAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromOrderStorefrontToOrder() {
        val paginationValue = "pagination_value"
        val result = OrderAdapter.adapt(StorefrontMockInstantiator.newOrder(), paginationValue)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, result.id)
        assertEquals(StorefrontMockInstantiator.DEFAULT_CURRENCY_CODE.name, result.currency)
        assertEquals(StorefrontMockInstantiator.DEFAULT_EMAIL, result.email)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ORDER_NUMBER, result.orderNumber)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, result.totalPrice)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, result.subtotalPrice)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, result.totalShippingPrice)
        assertNotNull(result.address)
        assertEquals(StorefrontMockInstantiator.DEFAULT_DATE.toDate(), result.processedAt)
        assertNotNull(result.orderProducts.first())
        assertEquals(paginationValue, result.paginationValue)
    }
}