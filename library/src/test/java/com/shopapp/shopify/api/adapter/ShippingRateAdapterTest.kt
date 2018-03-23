package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ShippingRateAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromShippingRateStorefrontToShippingRate() {
        val rate = ShippingRateAdapter.adapt(StorefrontMockInstantiator.newShippingRate())
        assertEquals(StorefrontMockInstantiator.DEFAULT_TITLE, rate.title)
        assertEquals(StorefrontMockInstantiator.DEFAULT_HANDLE, rate.handle)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, rate.price)
    }
}