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
class CountryAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromApiCountryToCountry() {
        val country = CountryAdapter.adapt(StorefrontMockInstantiator.newCountry())
        assertEquals(StorefrontMockInstantiator.DEFAULT_NUMBER_ID, country.id)
        assertEquals(StorefrontMockInstantiator.DEFAULT_COUNTRY, country.name)
        assertEquals(StorefrontMockInstantiator.DEFAULT_COUNTRY_CODE, country.code)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, country.states?.size)
        assertNotNull(country.states)
    }
}