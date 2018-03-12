package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}