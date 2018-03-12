package com.shopapp.shopify.api.adapter

import com.shopapp.shopify.JodaTimeAndroidRule
import com.shopapp.shopify.StorefrontMockInstantiator
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PolicyAdapterTest {

    @Rule
    @JvmField
    var jodaTimeAndroidRule = JodaTimeAndroidRule()

    @Test
    fun shouldAdaptFromPolicyStorefrontToPolicy() {
        val policy = PolicyAdapter.adapt(StorefrontMockInstantiator.newPolicy())!!
        assertEquals(StorefrontMockInstantiator.DEFAULT_TITLE, policy.title)
        assertEquals(StorefrontMockInstantiator.DEFAULT_BODY, policy.body)
        assertEquals(StorefrontMockInstantiator.DEFAULT_URL, policy.url)
    }
}