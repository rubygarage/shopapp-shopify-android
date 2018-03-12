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
class CheckoutAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromCheckoutStorefrontToCheckout() {
        val srcCheckout = StorefrontMockInstantiator.newCheckout()
        val checkout = CheckoutAdapter.adapt(srcCheckout)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, checkout.checkoutId)
        assertEquals(StorefrontMockInstantiator.DEFAULT_URL, checkout.webUrl)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, checkout.subtotalPrice)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, checkout.totalPrice)
        assertEquals(StorefrontMockInstantiator.DEFAULT_TAX, checkout.taxPrice)
        assertEquals(StorefrontMockInstantiator.DEFAULT_CURRENCY_CODE.toString(), checkout.currency)
        assertFalse(checkout.requiresShipping)
        assertNotNull(checkout.address)
    }

    @Test
    fun shouldAdaptShippingRate() {
        val srcCheckout = StorefrontMockInstantiator.newCheckout()
        val checkout = CheckoutAdapter.adapt(srcCheckout)
        assertNotNull(checkout.shippingRate)
    }

    @Test
    fun shouldSetNullIfShippingIsNull() {
        val srcCheckout = StorefrontMockInstantiator.newCheckout()
        given(srcCheckout.shippingLine).willReturn(null)
        val checkout = CheckoutAdapter.adapt(srcCheckout)
        assertNull(checkout.shippingRate)
    }


    @Test
    fun shouldAdaptAddress() {
        val srcCheckout = StorefrontMockInstantiator.newCheckout()
        val checkout = CheckoutAdapter.adapt(srcCheckout)
        assertNotNull(checkout.shippingRate)
    }

    @Test
    fun shouldSetNullifAddressIsNull() {
        val srcCheckout = StorefrontMockInstantiator.newCheckout()
        given(srcCheckout.shippingAddress).willReturn(null)
        val checkout = CheckoutAdapter.adapt(srcCheckout)
        assertNull(checkout.address)
    }
}