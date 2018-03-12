package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ProductOptionListAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromApiCountryListToCountryList() {
        val list = StorefrontMockInstantiator.newList(StorefrontMockInstantiator.newProductOption())
        val options = ProductOptionListAdapter.adapt(list)
        assertNotNull(options)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, options.size)

        val option = options[0]
        assertEquals(StorefrontMockInstantiator.DEFAULT_ID, option.id)
        assertEquals(StorefrontMockInstantiator.DEFAULT_NAME, option.name)
        assertNotNull(option.values)
        assertEquals(3, option.values.size)
    }

    @Test
    fun shouldReturnEmptyListOnNullConnection() {
        val options = ProductOptionListAdapter.adapt(null)
        assertNotNull(options)
        assertTrue(options.isEmpty())
    }
}