package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import com.shopapp.shopify.api.entity.ApiCountry
import com.shopapp.shopify.api.entity.ApiState
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class StateListAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromApiStateListToStateList() {
        val list = StorefrontMockInstantiator.newList(StorefrontMockInstantiator.newState())
        val states = StateListAdapter.adapt(list)
        assertNotNull(states)
        assertEquals(StorefrontMockInstantiator.DEFAULT_LIST_SIZE, states.size)
    }

    @Test
    fun shouldReturnEmptyList() {
        val list: List<ApiState>? = null
        val states = StateListAdapter.adapt(list)
        assertNotNull(states)
        Assert.assertTrue(states.isEmpty())
    }
}