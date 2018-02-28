package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ProductVariantAdapterTest {

    @Test
    fun shouldAdaptFromProductVariantStorefrontToProductVariant() {
        val result = ProductVariantAdapter.adapt(StorefrontMockInstantiator.newProductVariant())
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, result.id)
        assertEquals(StorefrontMockInstantiator.DEFAULT_TITLE, result.title)
        assertEquals(StorefrontMockInstantiator.DEFAULT_PRICE, result.price)
        assertEquals(true, result.isAvailable)
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, result.productId)
        assertNotNull(result.selectedOptions.first())
        assertNotNull(result.image)
        assertNotNull(result.productImage)
    }
}