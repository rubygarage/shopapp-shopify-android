package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class StateAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromApiStateToState() {
        val state = StateAdapter.adapt(StorefrontMockInstantiator.newState())
        assertEquals(StorefrontMockInstantiator.DEFAULT_NUMBER_ID, state.id)
        assertEquals(StorefrontMockInstantiator.DEFAULT_NUMBER_ID, state.countryId)
        assertEquals(StorefrontMockInstantiator.DEFAULT_STATE_CODE, state.code)
        assertEquals(StorefrontMockInstantiator.DEFAULT_STATE, state.name)
    }
}