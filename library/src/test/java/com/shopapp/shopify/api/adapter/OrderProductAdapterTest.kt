package com.shopapp.shopify.api.adapter

import com.nhaarman.mockito_kotlin.given
import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class OrderProductAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromOrderLineItemStorefrontToOrderProduct() {
        val result = OrderProductAdapter.adapt(StorefrontMockInstantiator.newOrderLineItem())
        assertEquals(StorefrontMockInstantiator.DEFAULT_QUANTITY, result.quantity)
        assertEquals(StorefrontMockInstantiator.DEFAULT_TITLE, result.title)
        assertNotNull(result.productVariant)
    }

    @Test
    fun shouldAdaptFromOrderLineItemStorefrontToOrderProductWhenProductVariantIsNull() {
        val adaptee = StorefrontMockInstantiator.newOrderLineItem()
        given(adaptee.variant).willReturn(null)
        val result = OrderProductAdapter.adapt(adaptee)
        assertEquals(StorefrontMockInstantiator.DEFAULT_QUANTITY, result.quantity)
        assertEquals(StorefrontMockInstantiator.DEFAULT_TITLE, result.title)
        assertNull(result.productVariant)
    }
}