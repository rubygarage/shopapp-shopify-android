package com.shopapp.shopify.api.adapter

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.mock
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopify.buy3.Storefront
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
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

    @Test
    fun shouldReturnNullProductImageListOnNullProductConnection() {
        val product = StorefrontMockInstantiator.newProduct()
        given(product.images).willReturn(null)
        val variant = StorefrontMockInstantiator.newProductVariant()
        given(variant.product).willReturn(product)

        val result = ProductVariantAdapter.adapt(variant)
        assertNull(result.productImage)
    }


    @Test
    fun shouldReturnNullProductImageListOnNullProductEdgeList() {
        val edgesMock: List<Storefront.ImageEdge>? = null
        val connection: Storefront.ImageConnection = mock {
            on { edges } doReturn edgesMock
        }
        val product = StorefrontMockInstantiator.newProduct()
        given(product.images).willReturn(connection)
        val variant = StorefrontMockInstantiator.newProductVariant()
        given(variant.product).willReturn(product)

        val result = ProductVariantAdapter.adapt(variant)
        assertNull(result.productImage)
    }
}