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

@RunWith(MockitoJUnitRunner.Silent::class)
class ProductListAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromApiCountryListToCountryList() {
        val products = ProductListAdapter.adapt(StorefrontMockInstantiator.newShop(), StorefrontMockInstantiator.newProductConnection())
        assertNotNull(products)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, products.size)
    }

    @Test
    fun shouldReturnEmptyListOnNullConnection() {
        val products = ProductListAdapter.adapt(StorefrontMockInstantiator.newShop(), null)
        assertNotNull(products)
        assertTrue(products.isEmpty())
    }

    @Test
    fun shouldReturnEmptyListOnNullEdges() {
        val edgesMock: List<Storefront.ProductEdge>? = null
        val connection: Storefront.ProductConnection = mock {
            on { edges } doReturn edgesMock
        }
        val products = ProductListAdapter.adapt(StorefrontMockInstantiator.newShop(), connection)
        assertNotNull(products)
        assertTrue(products.isEmpty())
    }
}