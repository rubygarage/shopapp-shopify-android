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
class CountryListAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromApiCountryListToCountryList() {
        val list = StorefrontMockInstantiator.newList(StorefrontMockInstantiator.newCountry())
        val countries = CountryListAdapter.adapt(list)
        assertNotNull(countries)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, countries.size)
    }
}